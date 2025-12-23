<%@page contentType="text/html; charset=UTF-8" language="java" %>
<html>
<head>
    <title> Registration on totalizator </title>
</head>
<body>

<form action="/registration" method="post">
    <label for="name">Name:
        <input type="text" name="name" id="name">
    </label><br/>
    <label for="birthday">Birthday:
        <input type="date" name="birthday" id="birthday">
    </label><br/>
    <label for="email">Email:
        <input type="text" name="email" id="email">
    </label><br/>
    <label for="pwd">Password:
        <input type="password" name="pwd" id="pwd">
    </label><br/>
    <c:forEach var="role" items="${requestScope.roles}">
        <option label="${role}"> ${role}</option><br>
    </c:forEach>
    <br/>
    <c:forEach var="gender" items="${requestScope.genders}">
        <input type="radio" name="gender" VALUE="${gender}"> ${gender}
        <br/>
    </c:forEach>
    <input type="submit" value="Send">
</form>
</body>
</html>
