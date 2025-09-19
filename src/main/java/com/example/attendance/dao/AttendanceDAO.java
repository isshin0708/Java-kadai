package com.example.attendance.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.example.attendance.dto.Attendance;

public class AttendanceDAO {
    private static final List<Attendance> attendanceRecords = new CopyOnWriteArrayList<>();

    public void checkIn(String userId) {
        Attendance attendance = new Attendance(userId);
        attendance.setCheckInTime(LocalDateTime.now());
        attendanceRecords.add(attendance);
    }

    public void checkOut(String userId) {
        attendanceRecords.stream()
            .filter(att -> userId.equals(att.getUserId()) && att.getCheckOutTime() == null)
            .findFirst()
            .ifPresent(att -> att.setCheckOutTime(LocalDateTime.now()));
    }

    public List<Attendance> findByUserId(String userId) {
        return attendanceRecords.stream()
            .filter(att -> userId.equals(att.getUserId()))
            .collect(Collectors.toList());
    }

    public List<Attendance> findAll() {
        return new ArrayList<>(attendanceRecords);
    }

    public List<Attendance> findFilteredRecords(String userId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecords.stream()
            .filter(att -> (userId == null || userId.isEmpty() || att.getUserId().equals(userId)))
            .filter(att -> (startDate == null || (att.getCheckInTime() != null && !att.getCheckInTime().toLocalDate().isBefore(startDate))))
            .filter(att -> (endDate == null || (att.getCheckInTime() != null && !att.getCheckInTime().toLocalDate().isAfter(endDate))))
            .collect(Collectors.toList());
    }

    public void addManualAttendance(String userId, LocalDateTime checkIn, LocalDateTime checkOut) {
        Attendance newRecord = new Attendance(userId);
        newRecord.setCheckInTime(checkIn);
        newRecord.setCheckOutTime(checkOut);
        attendanceRecords.add(newRecord);
    }

    public boolean updateManualAttendance(String userId, LocalDateTime oldCheckIn, LocalDateTime oldCheckOut, LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
        for (Attendance att : attendanceRecords) {
            if (att.getUserId().equals(userId) && att.getCheckInTime().equals(oldCheckIn)
                && (att.getCheckOutTime() == null ? oldCheckOut == null : att.getCheckOutTime().equals(oldCheckOut))) {
                att.setCheckInTime(newCheckIn);
                att.setCheckOutTime(newCheckOut);
                return true;
            }
        }
        return false;
    }

    public boolean deleteManualAttendance(String userId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return attendanceRecords.removeIf(att -> att.getUserId().equals(userId)
            && att.getCheckInTime().equals(checkIn)
            && (att.getCheckOutTime() == null ? checkOut == null : att.getCheckOutTime().equals(checkOut)));
    }

    // 月別合計労働時間（時間単位）
    public Map<String, Long> getMonthlyWorkingHours(String userId) {
        return attendanceRecords.stream()
            .filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
            .filter(att -> att.getCheckInTime() != null && att.getCheckOutTime() != null)
            .collect(Collectors.groupingBy(
                Attendance::getMonthKey,
                Collectors.summingLong(att -> att.getWorkMinutes() / 60)
            ));
    }

    // 月別出勤日数
    public Map<String, Long> getMonthlyCheckInCounts(String userId) {
        return attendanceRecords.stream()
            .filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
            .filter(att -> att.getCheckInTime() != null)
            .collect(Collectors.groupingBy(
                Attendance::getMonthKey,
                Collectors.counting()
            ));
    }

    // 月別残業時間（時間単位）
    public Map<String, Long> getMonthlyOvertimeHours(String userId) {
        return attendanceRecords.stream()
            .filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
            .filter(att -> att.getCheckInTime() != null && att.getCheckOutTime() != null)
            .collect(Collectors.groupingBy(
                Attendance::getMonthKey,
                Collectors.summingLong(att -> att.getOvertimeMinutes() / 60)
            ));
    }
}
