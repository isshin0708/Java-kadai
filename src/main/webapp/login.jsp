<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <meta charset="UTF-8">
    <title>勤怠管理システム - ログイン</title>
    <link rel="stylesheet"href="${pageContext.request.contextPath}/style.css"> 
</head>
<body>
    <div class="container">
        <h1>勤怠管理システム</h1>
        <form action="login" method="post">
            <p>
                <label for="username">ユーザーID:</label>
                <input type="text" id="username" name="username" required>
            </p>
            <p>
                <label for="password">パスワード:</label>
                <input type="password" id="password" name="password" required>
            </p>
            <div class="button-group">
                <input type="submit" value="ログイン">
            </div>
        </form>

        <!-- エラーメッセージ表示 -->
        <p class="error-message">
            <c:out value="${errorMessage}" />
        </p>

        <!-- 成功メッセージ表示 -->
        <c:if test="${not empty sessionScope.successMessage}">
            <p class="success-message">
                <c:out value="${sessionScope.successMessage}" />
            </p>
            <c:remove var="successMessage" scope="session" />
        </c:if>
    </div>
</body>
</html>
