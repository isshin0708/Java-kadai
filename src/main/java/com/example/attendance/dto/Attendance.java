package com.example.attendance.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Attendance {
    private String userId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public Attendance(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    // 勤務時間（分）
    public long getWorkMinutes() {
        if (checkInTime == null || checkOutTime == null) return 0;
        return Duration.between(checkInTime, checkOutTime).toMinutes();
    }

    // 勤務時間表示
    public String getWorkHoursDisplay() {
        long minutes = getWorkMinutes();
        if (minutes == 0) return "-";
        return (minutes / 60) + "時間" + (minutes % 60) + "分";
    }

    // 残業時間（17時以降）
    public long getOvertimeMinutes() {
        if (checkInTime == null || checkOutTime == null) return 0;
        LocalDate date = checkInTime.toLocalDate();
        LocalDateTime regularEnd = LocalDateTime.of(date, LocalTime.of(17, 0));
        LocalDateTime overtimeStart = checkInTime.isAfter(regularEnd) ? checkInTime : regularEnd;
        if (checkOutTime.isAfter(overtimeStart)) {
            return Duration.between(overtimeStart, checkOutTime).toMinutes();
        }
        return 0;
    }

    // 残業時間表示
    public String getOvertimeDisplay() {
        long minutes = getOvertimeMinutes();
        if (minutes < 1) return "-";
        return (minutes / 60) + "時間" + (minutes % 60) + "分";
    }

    // 出勤・退勤フォーマット
    public String getFormattedCheckInTime() {
        if (checkInTime == null) return "-";
        return checkInTime.withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getFormattedCheckOutTime() {
        if (checkOutTime == null) return "-";
        return checkOutTime.withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // 月（yyyy-MM）キー
    public String getMonthKey() {
        if (checkInTime == null) return "-";
        return checkInTime.getYear() + "-" + String.format("%02d", checkInTime.getMonthValue());
    }

    // 直近7日間かどうか
    public boolean isWithinLastWeek() {
        if (checkInTime == null) return false;
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        return checkInTime.isAfter(oneWeekAgo);
    }
}
