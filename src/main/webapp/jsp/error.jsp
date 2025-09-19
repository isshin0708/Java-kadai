<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>エラー</title></head>
<body>
<h2>エラーが発生しました</h2>
<p style="color:red">${errorMessage}</p>
<c:if test="${not empty exception}">
    <pre>${exception}</pre>
</c:if>
<a href="attendance">戻る</a>
</body>
</html>
