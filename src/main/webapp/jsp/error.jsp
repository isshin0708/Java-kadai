<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isErrorPage="true" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <meta charset="UTF-8">
    <title>エラー</title>
    <link rel="stylesheet"href="${pageContext.request.contextPath}/style.css"> 
</head>
<body>
    <h1>エラーが発生しました</h1>
    <p>申し訳ありませんが、処理中にエラーが発生しました。</p>
    <p>エラーメッセージ: <%= exception.getMessage() %></p>
    <a href="../login.jsp">ログインページに戻る</a>
</body>
</html>
