<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<head>
    <title>Page Not Found</title>
</head>

<main class="auth-shell">

    <header class="auth-header">
        <h1 class="auth-header__title">Page not found</h1>
        <p class="auth-header__subtitle">
            The page you are looking for does not exist or may have been moved.
        </p>
    </header>

    <div class="auth-actions">
        <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">
            Back to Home
        </a>
    </div>

</main>
