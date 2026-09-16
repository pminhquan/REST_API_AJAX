<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="/WEB-INF/views/fragments/document-head.jsp" />
    <title>403 Forbidden - Access Denied</title>
</head>
<body class="auth-layout">

<main class="auth-shell">

    <div class="d-flex align-items-center justify-content-between mb-4">
        <a href="${pageContext.request.contextPath}/home" class="auth-brand mb-0" aria-label="SpringBoot Store Home">
            <img class="auth-brand__mark" src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="" aria-hidden="true" width="28" height="28" />
            <span class="auth-brand__text">SpringBoot Store</span>
        </a>
        <a href="${pageContext.request.contextPath}/home" class="btn btn-ghost btn-sm d-inline-flex align-items-center gap-1" aria-label="Back to home">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <line x1="19" y1="12" x2="5" y2="12"></line>
                <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
            <span>Home</span>
        </a>
    </div>

    <header class="auth-header">
        <h1 class="auth-header__title">403 - Access Denied</h1>
        <p class="auth-header__subtitle">
            You do not have permission to access this resource. If you believe this is an error, please return to the homepage or sign in with an authorized account.
        </p>
    </header>

    <div class="auth-actions">
        <a href="${pageContext.request.contextPath}/home" class="btn btn-primary d-inline-flex align-items-center justify-content-center gap-2">
            <span>Back to Home</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
        </a>
    </div>

    <div class="auth-links mt-4">
        <c:choose>
            <c:when test="${empty sessionScope.authenticatedUserId}">
                <span>
                    Need to switch accounts?
                    <a href="${pageContext.request.contextPath}/login">Sign in</a>
                </span>
            </c:when>
            <c:otherwise>
                <span>
                    Signed in as <strong><c:out value="${sessionScope.account != null ? sessionScope.account.username : 'User'}" /></strong>.
                    <a href="${pageContext.request.contextPath}/logout">Sign out</a>
                </span>
            </c:otherwise>
        </c:choose>
    </div>

</main>

<jsp:include page="/WEB-INF/views/fragments/shared-scripts.jsp" />
</body>
</html>
