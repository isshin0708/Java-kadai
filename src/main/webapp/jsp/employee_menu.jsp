<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>従業員メニュー</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
    <style>
        .warning-message { color: orange; font-weight: bold; }
        .danger-message { color: red; font-weight: bold; }
        .summary-table, table { border-collapse: collapse; width: 100%; }
        .summary-table th, .summary-table td, table th, table td { border: 1px solid #ccc; padding: 6px; text-align: center; }
        .button { padding: 4px 8px; margin: 2px; cursor: pointer; }
        .check-in { background-color: #4CAF50; color: white; border: none; cursor: pointer; }
        .check-out { background-color: #2196F3; color: white; border: none; cursor: pointer; }
        .secondary { background-color: #ccc; border: none; cursor: pointer; }
        .success-message { color: green; font-weight: bold; }
        .danger-message, .warning-message { font-weight: bold; }
    </style>
</head>
<body>
<div class="container">
    <h1>従業員メニュー</h1>
    <p>ようこそ, ${user.username}さん</p>

    <!-- 成功メッセージ -->
    <c:if test="${not empty sessionScope.successMessage}">
        <p class="success-message"><c:out value="${sessionScope.successMessage}" /></p>
        <c:remove var="successMessage" scope="session" />
    </c:if>

    <!-- 出勤・退勤ボタン -->
    <div class="button-group">
        <form action="attendance" method="post" style="display:inline;">
            <input type="hidden" name="action" value="check_in">
            <input type="submit" value="🕒 出勤" class="button check-in">
        </form>
        <form action="attendance" method="post" style="display:inline;">
            <input type="hidden" name="action" value="check_out">
            <input type="submit" value="🏁 退勤" class="button check-out">
        </form>
    </div>

    <!-- 今月の残業警告 -->
    <c:if test="${myOvertimeHours != null}">
        <c:choose>
            <c:when test="${myOvertimeHours >= 45}">
                <p class="danger-message">
                    あなたの今月の残業時間は <strong>${myOvertimeHours}</strong> 時間です。<br>
                    月の上限 (45時間) を超えています！
                </p>
            </c:when>
            <c:when test="${myOvertimeHours >= 40 && myOvertimeHours < 45}">
                <p class="warning-message">
                    あなたの今月の残業時間は ${myOvertimeHours} 時間です。<br>
                    あと <strong>${45 - myOvertimeHours}</strong> 時間で月の上限に達します！
                </p>
            </c:when>
            <c:otherwise>
                <p>今月の残業時間: ${myOvertimeHours} 時間</p>
            </c:otherwise>
        </c:choose>
    </c:if>

    <!-- 月別勤怠グラフ -->
    <h2>月別勤怠グラフ</h2>

    <h4>月別合計労働時間</h4>
    <pre>
<c:if test="${not empty monthlyWorkingHours}">
    <c:forEach var="entry" items="${monthlyWorkingHours}">
${entry.key}: <c:forEach begin="1" end="${entry.value / 5}">*</c:forEach> ${entry.value}時間
    </c:forEach>
</c:if>
<c:if test="${empty monthlyWorkingHours}">データがありません。</c:if>
    </pre>

    <h4>月別出勤日数</h4>
    <pre>
<c:if test="${not empty monthlyCheckInCounts}">
    <c:forEach var="entry" items="${monthlyCheckInCounts}">
${entry.key}: <c:forEach begin="1" end="${entry.value}">■</c:forEach> ${entry.value}日
    </c:forEach>
</c:if>
<c:if test="${empty monthlyCheckInCounts}">データがありません。</c:if>
    </pre>

    <h4>月別残業時間</h4>
    <pre>
<c:if test="${not empty monthlyOvertimeHours}">
    <c:forEach var="entry" items="${monthlyOvertimeHours}">
${entry.key}: <c:forEach begin="1" end="${entry.value / 5}">*</c:forEach> ${entry.value}時間
    </c:forEach>
</c:if>
<c:if test="${empty monthlyOvertimeHours}">データがありません。</c:if>
    </pre>

    <!-- 勤怠履歴テーブル -->
    <h2>あなたの勤怠履歴</h2>
    <table>
        <thead>
            <tr>
                <th>出勤時刻</th>
                <th>退勤時刻</th>
                <th>勤務時間</th>
                <th>残業時間</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="att" items="${attendanceRecords}">
                <tr>
                    <td>${att.formattedCheckInTime}</td>
                    <td>
                        <c:choose>
                            <c:when test="${att.checkOutTime != null}">
                                ${att.formattedCheckOutTime}
                            </c:when>
                            <c:otherwise>-</c:otherwise>
                        </c:choose>
                    </td>
                    <td>${att.workHoursDisplay}</td>
                    <td>
                        ${att.overtimeDisplay}
                        <c:choose>
                            <c:when test="${att.overtimeMinutes >= 45*60}">
                                <span class="danger-message">（上限超過）</span>
                            </c:when>
                            <c:when test="${att.overtimeMinutes >= 40*60 && att.overtimeMinutes < 45*60}">
                                <span class="warning-message">（上限に近い）</span>
                            </c:when>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty attendanceRecords}">
                <tr><td colspan="4">勤怠記録がありません。</td></tr>
            </c:if>
        </tbody>
    </table>

    <!-- ログアウト -->
    <div class="button-group">
        <a href="logout" class="button secondary">ログアウト</a>
    </div>
</div>
</body>
</html>
