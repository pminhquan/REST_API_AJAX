<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sitemesh" uri="http://www.sitemesh.org/sitemesh3" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/views/fragments/document-head.jsp" />
    <sitemesh:write property='head'/>
</head>
<body class="admin-layout">

<jsp:include page="/WEB-INF/views/fragments/navbar.jsp" />

<sitemesh:write property='body'/>

<jsp:include page="/WEB-INF/views/fragments/footer.jsp" />

<jsp:include page="/WEB-INF/views/fragments/shared-scripts.jsp" />
</body>
</html>
