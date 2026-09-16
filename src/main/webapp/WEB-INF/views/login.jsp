<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<head>
    <title>Login</title>
</head>

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
        <h1 class="auth-header__title">Login</h1>
        <p class="auth-header__subtitle">
            Enter your username or email and password to continue.
        </p>
    </header>

    <c:if test="${not empty param.message}">
        <div class="alert alert-success d-flex align-items-start gap-2 mb-4" role="status">
            <svg class="flex-shrink-0 mt-1" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
            <div>
                <c:out value="${param.message}"/>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger d-flex align-items-start gap-2 mb-4" role="alert">
            <svg class="flex-shrink-0 mt-1" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <div>
                <c:out value="${error}"/>
            </div>
        </div>
    </c:if>

    <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post">
        <input type="hidden" name="_csrf" value="${csrfToken}" />

        <div class="form-field">
            <label class="form-label label-required" for="identifier">
                Username or Email
            </label>
            <input
                    type="text"
                    id="identifier"
                    name="identifier"
                    class="form-control"
                    value="${fn:escapeXml(param.identifier)}"
                    placeholder="e.g. user1 or user@example.com"
                    autocomplete="username"
                    aria-describedby="identifier-hint"
                    required
                    autofocus>
            <span class="form-hint" id="identifier-hint">
                Enter your registered username or email address.
            </span>
        </div>

        <div class="form-field">
            <label class="form-label label-required" for="password">
                Password
            </label>
            <div class="password-field" data-password-field>
                <input
                        type="password"
                        id="password"
                        name="password"
                        class="form-control"
                        placeholder="Enter your password"
                        autocomplete="current-password"
                        aria-describedby="password-hint"
                        required>
                <button
                        type="button"
                        class="password-toggle"
                        data-password-toggle
                        hidden
                        aria-pressed="false"
                        aria-label="Toggle password visibility">
                    Show
                </button>
            </div>
            <span class="form-hint" id="password-hint">
                Enter your account password.
            </span>
        </div>

        <div class="form-check my-3 d-flex align-items-center gap-2">
            <input class="form-check-input mt-0"
                   type="checkbox"
                   id="rememberMe"
                   name="rememberMe"
                   value="true">
            <label class="form-check-label small text-secondary mb-0" for="rememberMe">
                Remember me on this device
            </label>
        </div>

        <div class="auth-actions">
            <button type="submit" class="btn btn-primary d-inline-flex align-items-center justify-content-center gap-2">
                <span>Login</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                    <polyline points="10 17 15 12 10 7"></polyline>
                    <line x1="15" y1="12" x2="3" y2="12"></line>
                </svg>
            </button>
        </div>

    </form>

    <div class="auth-links">
        <a href="${pageContext.request.contextPath}/forgot-password">
            Forgot password?
        </a>
        <span>
            New here?
            <a href="${pageContext.request.contextPath}/register">Create account</a>
        </span>
    </div>

</main>

<script src="${pageContext.request.contextPath}/assets/js/password-toggle.js"></script>
