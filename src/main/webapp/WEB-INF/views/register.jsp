<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<head>
    <title>Register Account</title>
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
        <h1 class="auth-header__title">Register Account</h1>
        <p class="auth-header__subtitle">
            Create your account to get started. You will verify your email with an OTP code in the next step.
        </p>
    </header>

    <p class="auth-steps">
        <span class="auth-steps__current">Step 1 of 2</span>
        &middot; Account details
    </p>

    <div class="alert alert-info d-flex align-items-start gap-2 mb-4" role="note">
        <svg class="flex-shrink-0 mt-1" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="16" x2="12" y2="12"></line>
            <line x1="12" y1="8" x2="12.01" y2="8"></line>
        </svg>
        <div>
            <strong>Email Verification Notice:</strong> A 6-digit one-time code (OTP) will be sent to your email to verify and activate your account.
        </div>
    </div>

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

    <form class="auth-form" action="${pageContext.request.contextPath}/register" method="post">
        <input type="hidden" name="_csrf" value="${csrfToken}" />

        <div class="form-field">
            <label class="form-label label-required" for="username">
                Username
            </label>
            <input
                    type="text"
                    id="username"
                    name="username"
                    class="form-control"
                    value="${fn:escapeXml(param.username)}"
                    placeholder="e.g. johndoe"
                    autocomplete="username"
                    aria-describedby="username-hint"
                    required
                    autofocus>
            <span class="form-hint" id="username-hint">
                Choose a unique username for signing in to your account.
            </span>
        </div>

        <div class="form-field">
            <label class="form-label label-required" for="email">
                Email
            </label>
            <input
                    type="email"
                    id="email"
                    name="email"
                    class="form-control"
                    value="${fn:escapeXml(param.email)}"
                    placeholder="e.g. john@example.com"
                    autocomplete="email"
                    aria-describedby="email-hint"
                    required>
            <span class="form-hint" id="email-hint">
                A valid email address where your 6-digit verification code will be sent.
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
                        autocomplete="new-password"
                        aria-describedby="password-hint"
                        minlength="6"
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
                Must be at least 6 characters (recommended: include uppercase, numbers, or symbols).
            </span>
        </div>

        <div class="form-field">
            <label class="form-label label-required" for="confirmPassword">
                Confirm Password
            </label>
            <div class="password-field" data-password-field>
                <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        class="form-control"
                        placeholder="Re-enter your password"
                        autocomplete="new-password"
                        aria-describedby="confirm-hint"
                        minlength="6"
                        required>
                <button
                        type="button"
                        class="password-toggle"
                        data-password-toggle
                        hidden
                        aria-pressed="false"
                        aria-label="Toggle confirm password visibility">
                    Show
                </button>
            </div>
            <span class="form-hint" id="confirm-hint">
                Ensure this matches the password entered above.
            </span>
        </div>

        <div class="auth-actions">
            <button type="submit" class="btn btn-primary d-inline-flex align-items-center justify-content-center gap-2">
                <span>Register</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                    <circle cx="8.5" cy="7" r="4"></circle>
                    <line x1="20" y1="8" x2="20" y2="14"></line>
                    <line x1="23" y1="11" x2="17" y2="11"></line>
                </svg>
            </button>
        </div>

    </form>

    <div class="auth-links auth-links--center">
        <span>
            Already have an account?
            <a href="${pageContext.request.contextPath}/login">Login</a>
        </span>
    </div>

</main>

<script src="${pageContext.request.contextPath}/assets/js/password-toggle.js"></script>
