package com.example.attendance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.User;

@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    // 入力用フォーマット (datetime-local)
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    // 表示/CSV用フォーマット
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        if (user == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        // 成功メッセージをリクエスト属性にセット
        String message = (String) session.getAttribute("successMessage");
        if (message != null) {
            req.setAttribute("successMessage", message);
            session.removeAttribute("successMessage");
        }

        if ("export_csv".equals(action) && "admin".equals(user.getRole())) {
            exportCsv(req, resp);
        } else if ("filter".equals(action) && "admin".equals(user.getRole())) {
            handleAdminFilter(req, resp);
        } else {
            handleDefault(req, resp, user);
        }
    }

    private void handleAdminFilter(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String filterUserId = req.getParameter("filterUserId");
        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");

        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            if (startDateStr != null && !startDateStr.isEmpty()) startDate = LocalDate.parse(startDateStr);
            if (endDateStr != null && !endDateStr.isEmpty()) endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            req.setAttribute("errorMessage", "日付の形式が不正です。");
        }

        List<Attendance> filteredRecords = attendanceDAO.findFilteredRecords(filterUserId, startDate, endDate);
        req.setAttribute("allAttendanceRecords", filteredRecords);

        // 総勤務時間
        Map<String, Long> totalHoursByUser = filteredRecords.stream()
                .collect(Collectors.groupingBy(
                        Attendance::getUserId,
                        Collectors.summingLong(att -> {
                            if (att.getCheckInTime() != null && att.getCheckOutTime() != null) {
                                return java.time.temporal.ChronoUnit.HOURS.between(att.getCheckInTime(), att.getCheckOutTime());
                            }
                            return 0L;
                        })
                ));
        req.setAttribute("totalHoursByUser", totalHoursByUser);

        // 総残業時間
        Map<String, Long> totalOvertimeHoursByUser = filteredRecords.stream()
                .collect(Collectors.groupingBy(
                        Attendance::getUserId,
                        Collectors.summingLong(att -> att.getOvertimeMinutes() / 60)
                ));
        req.setAttribute("totalOvertimeHoursByUser", totalOvertimeHoursByUser);

        // 月別労働時間
        req.setAttribute("monthlyWorkingHours", attendanceDAO.getMonthlyWorkingHours(filterUserId));
        req.setAttribute("monthlyCheckInCounts", attendanceDAO.getMonthlyCheckInCounts(filterUserId));

        // 月別残業時間
        Map<String, Long> monthlyOvertimeHours = filteredRecords.stream()
                .filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() != null)
                .collect(Collectors.groupingBy(
                        Attendance::getMonthKey,
                        Collectors.summingLong(a -> a.getOvertimeMinutes() / 60)
                ));
        req.setAttribute("monthlyOvertimeHours", monthlyOvertimeHours);

        RequestDispatcher rd = req.getRequestDispatcher("/jsp/admin_menu.jsp");
        rd.forward(req, resp);
    }

    private void handleDefault(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        if ("admin".equals(user.getRole())) {
            List<Attendance> allRecords = attendanceDAO.findAll();
            req.setAttribute("allAttendanceRecords", allRecords);

            Map<String, Long> totalHoursByUser = allRecords.stream()
                    .collect(Collectors.groupingBy(
                            Attendance::getUserId,
                            Collectors.summingLong(att -> {
                                if (att.getCheckInTime() != null && att.getCheckOutTime() != null) {
                                    return java.time.temporal.ChronoUnit.HOURS.between(att.getCheckInTime(), att.getCheckOutTime());
                                }
                                return 0L;
                            })
                    ));
            req.setAttribute("totalHoursByUser", totalHoursByUser);

            Map<String, Long> totalOvertimeHoursByUser = allRecords.stream()
                    .collect(Collectors.groupingBy(
                            Attendance::getUserId,
                            Collectors.summingLong(att -> att.getOvertimeMinutes() / 60)
                    ));
            req.setAttribute("totalOvertimeHoursByUser", totalOvertimeHoursByUser);

            req.setAttribute("monthlyWorkingHours", attendanceDAO.getMonthlyWorkingHours(null));
            req.setAttribute("monthlyCheckInCounts", attendanceDAO.getMonthlyCheckInCounts(null));

            // 月別残業時間
            Map<String, Long> monthlyOvertimeHours = allRecords.stream()
                    .filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() != null)
                    .collect(Collectors.groupingBy(
                            Attendance::getMonthKey,
                            Collectors.summingLong(a -> a.getOvertimeMinutes() / 60)
                    ));
            req.setAttribute("monthlyOvertimeHours", monthlyOvertimeHours);

            RequestDispatcher rd = req.getRequestDispatcher("/jsp/admin_menu.jsp");
            rd.forward(req, resp);

        } else {
            List<Attendance> records = attendanceDAO.findByUserId(user.getUsername());
            req.setAttribute("attendanceRecords", records);

            long totalOvertimeMinutes = records.stream()
                    .filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() != null)
                    .mapToLong(Attendance::getOvertimeMinutes)
                    .sum();
            req.setAttribute("myOvertimeHours", totalOvertimeMinutes / 60);

            req.setAttribute("monthlyWorkingHours", attendanceDAO.getMonthlyWorkingHours(user.getUsername()));
            req.setAttribute("monthlyCheckInCounts", attendanceDAO.getMonthlyCheckInCounts(user.getUsername()));

            // 月別残業時間
            Map<String, Long> monthlyOvertimeHours = records.stream()
                    .filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() != null)
                    .collect(Collectors.groupingBy(
                            Attendance::getMonthKey,
                            Collectors.summingLong(a -> a.getOvertimeMinutes() / 60)
                    ));
            req.setAttribute("monthlyOvertimeHours", monthlyOvertimeHours);

            RequestDispatcher rd = req.getRequestDispatcher("/jsp/employee_menu.jsp");
            rd.forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        if (user == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        String action = req.getParameter("action");

        switch (action) {
            case "check_in":
                attendanceDAO.checkIn(user.getUsername());
                session.setAttribute("successMessage", "出勤を記録しました。");
                break;

            case "check_out":
                attendanceDAO.checkOut(user.getUsername());
                session.setAttribute("successMessage", "退勤を記録しました。");
                break;

            case "add_manual":
                if ("admin".equals(user.getRole())) handleAddManual(req, session);
                break;

            case "update_manual":
                if ("admin".equals(user.getRole())) handleUpdateManual(req, session);
                break;

            case "delete_manual":
                if ("admin".equals(user.getRole())) handleDeleteManual(req, session);
                break;
        }

        if ("admin".equals(user.getRole())) {
            resp.sendRedirect("attendance?action=filter&filterUserId=" +
                    (req.getParameter("filterUserId") != null ? req.getParameter("filterUserId") : "") +
                    "&startDate=" + (req.getParameter("startDate") != null ? req.getParameter("startDate") : "") +
                    "&endDate=" + (req.getParameter("endDate") != null ? req.getParameter("endDate") : ""));
        } else {
            resp.sendRedirect("attendance");
        }
    }

    private void handleAddManual(HttpServletRequest req, HttpSession session) {
        String userId = req.getParameter("userId");
        String checkInStr = req.getParameter("checkInTime");
        String checkOutStr = req.getParameter("checkOutTime");

        try {
            LocalDateTime checkIn = LocalDateTime.parse(checkInStr, inputFormatter);
            LocalDateTime checkOut = (checkOutStr != null && !checkOutStr.isEmpty())
                    ? LocalDateTime.parse(checkOutStr, inputFormatter)
                    : null;

            attendanceDAO.addManualAttendance(userId, checkIn, checkOut);
            session.setAttribute("successMessage", "勤怠記録を手動で追加しました。");
        } catch (DateTimeParseException e) {
            session.setAttribute("errorMessage", "日付/時刻の形式が不正です。");
        }
    }

    private void handleUpdateManual(HttpServletRequest req, HttpSession session) {
        String userId = req.getParameter("userId");
        try {
            LocalDateTime oldCheckIn = LocalDateTime.parse(req.getParameter("oldCheckInTime"), inputFormatter);
            LocalDateTime oldCheckOut = req.getParameter("oldCheckOutTime") != null && !req.getParameter("oldCheckOutTime").isEmpty()
                    ? LocalDateTime.parse(req.getParameter("oldCheckOutTime"), inputFormatter)
                    : null;
            LocalDateTime newCheckIn = LocalDateTime.parse(req.getParameter("newCheckInTime"), inputFormatter);
            LocalDateTime newCheckOut = req.getParameter("newCheckOutTime") != null && !req.getParameter("newCheckOutTime").isEmpty()
                    ? LocalDateTime.parse(req.getParameter("newCheckOutTime"), inputFormatter)
                    : null;

            if (attendanceDAO.updateManualAttendance(userId, oldCheckIn, oldCheckOut, newCheckIn, newCheckOut)) {
                session.setAttribute("successMessage", "勤怠記録を手動で更新しました。");
            } else {
                session.setAttribute("errorMessage", "勤怠記録の更新に失敗しました。");
            }
        } catch (DateTimeParseException e) {
            session.setAttribute("errorMessage", "日付/時刻の形式が不正です。");
        }
    }

    private void handleDeleteManual(HttpServletRequest req, HttpSession session) {
        String userId = req.getParameter("userId");
        try {
            LocalDateTime checkIn = LocalDateTime.parse(req.getParameter("checkInTime"), inputFormatter);
            LocalDateTime checkOut = req.getParameter("checkOutTime") != null && !req.getParameter("checkOutTime").isEmpty()
                    ? LocalDateTime.parse(req.getParameter("checkOutTime"), inputFormatter)
                    : null;

            if (attendanceDAO.deleteManualAttendance(userId, checkIn, checkOut)) {
                session.setAttribute("successMessage", "勤怠記録を削除しました。");
            } else {
                session.setAttribute("errorMessage", "勤怠記録の削除に失敗しました。");
            }
        } catch (DateTimeParseException e) {
            session.setAttribute("errorMessage", "日付/時刻の形式が不正です。");
        }
    }

    private void exportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"attendance_records.csv\"");

        PrintWriter writer = resp.getWriter();
        writer.append("User ID,Check-in Time,Check-out Time\n");

        String filterUserId = req.getParameter("filterUserId");
        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");

        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            if (startDateStr != null && !startDateStr.isEmpty()) startDate = LocalDate.parse(startDateStr);
            if (endDateStr != null && !endDateStr.isEmpty()) endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format for CSV export: " + e.getMessage());
        }

        List<Attendance> records = attendanceDAO.findFilteredRecords(filterUserId, startDate, endDate);

        for (Attendance record : records) {
            writer.append(String.format("%s,%s,%s\n",
                    record.getUserId(),
                    record.getCheckInTime() != null ? record.getCheckInTime().format(outputFormatter) : "",
                    record.getCheckOutTime() != null ? record.getCheckOutTime().format(outputFormatter) : ""));
        }

        writer.flush();
    }
}
