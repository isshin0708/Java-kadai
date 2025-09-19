<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>管理者メニュー</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
    <style>
        .warning-message { color: orange; font-weight: bold; }
        .danger-message { color: red; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; }
        table, th, td { border: 1px solid #ccc; padding: 6px; text-align: center; }
        th { background-color: #f2f2f2; }
        .button { padding: 4px 8px; margin: 2px; cursor: pointer; }
        .danger { background-color: #f44336; color: white; border: none; }
        .success-message { color: green; font-weight: bold; }
        .error-message { color: red; font-weight: bold; }
        .filter-form div { margin-bottom: 6px; }
        .button-group { margin-top: 10px; }
    </style>
</head>
<body>
<div class="container">
    <h1>管理者メニュー</h1>
    <p>ようこそ, ${user.username}さん (管理者)</p>
    <div class="main-nav">
        <a href="attendance?action=filter">勤怠履歴管理</a>
        <a href="users?action=list">ユーザー管理</a>
        <a href="logout">ログアウト</a>
    </div>

    <!-- 成功メッセージ -->
    <c:if test="${not empty sessionScope.successMessage}">
        <p class="success-message"><c:out value="${sessionScope.successMessage}" /></p>
        <c:remove var="successMessage" scope="session" />
    </c:if>

    <!-- エラーメッセージ -->
    <c:if test="${not empty errorMessage}">
        <p class="error-message"><c:out value="${errorMessage}" /></p>
    </c:if>

    <!-- フィルタフォーム -->
    <h2>勤怠履歴</h2>
    <form action="attendance" method="get" class="filter-form">
        <input type="hidden" name="action" value="filter">
        <div>
            <label for="filterUserId">ユーザーID:</label>
            <input type="text" id="filterUserId" name="filterUserId" value="${param.filterUserId}">
        </div>
        <div>
            <label for="startDate">開始日:</label>
            <input type="date" id="startDate" name="startDate" value="${param.startDate}">
        </div>
        <div>
            <label for="endDate">終了日:</label>
            <input type="date" id="endDate" name="endDate" value="${param.endDate}">
        </div>
        <button type="submit" class="button">フィルタ</button>
    </form>

    <!-- CSVエクスポート -->
    <a href="attendance?action=export_csv&filterUserId=${param.filterUserId}&startDate=${param.startDate}&endDate=${param.endDate}" class="button">
        勤怠履歴を CSV エクスポート
    </a>

    <!-- 勤怠サマリー -->
    <h3>勤怠サマリー (合計労働時間)</h3>
    <table class="summary-table">
        <thead>
            <tr>
                <th>ユーザーID</th>
                <th>合計労働時間 (時間)</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="entry" items="${totalHoursByUser}">
                <tr>
                    <td>${entry.key}</td>
                    <td>${entry.value}</td>
                </tr>
            </c:forEach>
            <c:if test="${empty totalHoursByUser}">
                <tr><td colspan="2">データがありません。</td></tr>
            </c:if>
        </tbody>
    </table>

    <!-- 残業サマリー -->
    <h3>残業サマリー (合計残業時間)</h3>
    <table class="summary-table">
        <thead>
            <tr>
                <th>ユーザーID</th>
                <th>合計残業時間 (時間)</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="entry" items="${totalOvertimeHoursByUser}">
                <tr>
                    <td>${entry.key}</td>
                    <td>
                        ${entry.value}
                        <c:choose>
                            <c:when test="${entry.value >= 45}">
                                <span class="danger-message">🚨 残業時間上限超過</span>
                            </c:when>
                            <c:when test="${entry.value >= 40 && entry.value < 45}">
                                <span class="warning-message">⚠ 月上限に近い</span>
                            </c:when>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty totalOvertimeHoursByUser}">
                <tr><td colspan="2">データがありません。</td></tr>
            </c:if>
        </tbody>
    </table>

    <!-- 月別勤怠グラフ -->
    <h3>月別勤怠グラフ</h3>
    <h4>月別合計労働時間</h4>
    <pre>
<c:forEach var="entry" items="${monthlyWorkingHours}">
${entry.key}: <c:forEach begin="1" end="${entry.value / 5}">*</c:forEach> ${entry.value}時間
</c:forEach>
<c:if test="${empty monthlyWorkingHours}">データがありません。</c:if>
    </pre>

    <h4>月別出勤日数</h4>
    <pre>
<c:forEach var="entry" items="${monthlyCheckInCounts}">
${entry.key}: <c:forEach begin="1" end="${entry.value}">■</c:forEach> ${entry.value}日
</c:forEach>
<c:if test="${empty monthlyCheckInCounts}">データがありません。</c:if>
    </pre>

    <!-- 詳細勤怠履歴 -->
    <h3>詳細勤怠履歴</h3>
    <table>
        <thead>
            <tr>
                <th>従業員 ID</th>
                <th>出勤時刻</th>
                <th>退勤時刻</th>
                <th>勤務時間</th>
                <th>残業時間</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="att" items="${allAttendanceRecords}">
                <tr>
                    <td>${att.userId}</td>
                    <td>${att.formattedCheckInTime}</td>
                    <td>${att.formattedCheckOutTime}</td>
                    <td>${att.workHoursDisplay}</td>
                    <td>
                        ${att.overtimeDisplay}
                        <c:choose>
                            <c:when test="${att.overtimeMinutes >= 45*60}">
                                <span class="danger-message">🚨</span>
                            </c:when>
                            <c:when test="${att.overtimeMinutes >= 40*60 && att.overtimeMinutes < 45*60}">
                                <span class="warning-message">⚠</span>
                            </c:when>
                        </c:choose>
                    </td>
                    <td class="table-actions">
                        <form action="attendance" method="post" style="display:inline;">
                            <input type="hidden" name="action" value="delete_manual">
                            <input type="hidden" name="userId" value="${att.userId}">
                            <input type="hidden" name="checkInTime" value="${att.formattedCheckInTime}">
                            <input type="hidden" name="checkOutTime" value="${att.formattedCheckOutTime}">
                            <input type="submit" value="削除" class="button danger"
                                   onclick="return confirm('本当にこの勤怠記録を削除しますか？');">
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty allAttendanceRecords}">
                <tr><td colspan="6">データがありません。</td></tr>
            </c:if>
        </tbody>
    </table>

    <!-- 勤怠記録の手動追加 -->
    <h2>勤怠記録の手動追加</h2>
    <form action="attendance" method="post">
        <input type="hidden" name="action" value="add_manual">
        <p>
            <label for="manualUserId">ユーザーID:</label>
            <input type="text" id="manualUserId" name="userId" required>
        </p>
        <p>
            <label for="manualCheckInTime">出勤時刻:</label>
            <input type="datetime-local" id="manualCheckInTime" name="checkInTime" required>
        </p>
        <p>
            <label for="manualCheckOutTime">退勤時刻 (任意):</label>
            <input type="datetime-local" id="manualCheckOutTime" name="checkOutTime">
        </p>
        <div class="button-group">
            <input type="submit" value="追加">
        </div>
    </form>
</div>
</body>
</html>
