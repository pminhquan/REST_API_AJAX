<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<head>
    <title>Forgot Password</title>
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
        <h1 class="auth-header__title">Forgot Password</h1>
        <c:choose>
            <c:when test="${empty sessionScope.pendingResetEmail}">
                <p class="auth-header__subtitle">
                    Enter your email address to receive a secure 6-digit recovery code.
                </p>
            </c:when>
            <c:otherwise>
                <p class="auth-header__subtitle">
                    Enter the 6-digit verification code sent to your email to continue.
                </p>
            </c:otherwise>
        </c:choose>
    </header>

    <c:if test="${empty sessionScope.pendingResetEmail}">
        <div class="alert alert-info d-flex align-items-start gap-2 mb-4" role="note">
            <svg class="flex-shrink-0 mt-1" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            <div>
                <strong>Password Recovery:</strong> Provide the email registered with your account. If an account exists, a 6-digit reset code valid for 10 minutes will be delivered to your inbox.
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

    <c:if test="${not empty message}">
        <div class="alert alert-success d-flex align-items-start gap-2 mb-4" role="status">
            <svg class="flex-shrink-0 mt-1" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
            <div>
                <c:out value="${message}"/>
            </div>
        </div>
    </c:if>

    <c:if test="${empty sessionScope.pendingResetEmail}">

        <p class="auth-steps">
            <span class="auth-steps__current">Step 1 of 2</span>
            &middot; Request recovery code
        </p>

        <form class="auth-form" action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="_csrf" value="${csrfToken}" />
            <input type="hidden" name="action" value="request"/>

            <div class="form-field">
                <label class="form-label label-required" for="email">
                    Email Address
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
                        required
                        autofocus>
                <span class="form-hint" id="email-hint">
                    Enter the email address associated with your SpringBoot Store account.
                </span>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary d-inline-flex align-items-center justify-content-center gap-2">
                    <span>Send Reset Code</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <line x1="22" y1="2" x2="11" y2="13"></line>
                        <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                    </svg>
                </button>
            </div>

        </form>

        <div class="auth-links">
            <a href="${pageContext.request.contextPath}/login" class="d-inline-flex align-items-center gap-1">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <line x1="19" y1="12" x2="5" y2="12"></line>
                    <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
                <span>Remember password? Login</span>
            </a>
            <span>
                New user?
                <a href="${pageContext.request.contextPath}/register">Create account</a>
            </span>
        </div>

    </c:if>

    <c:if test="${not empty sessionScope.pendingResetEmail}">

        <p class="auth-steps">
            <span class="auth-steps__current">Step 2 of 2</span>
            &middot; Verify reset code
        </p>

        <form class="auth-form" action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="_csrf" value="${csrfToken}" />
            <input type="hidden" name="action" value="verify"/>

            <div class="otp-field${not empty error ? ' otp-field--error' : ''}" data-otp>
                <label class="form-label label-required" for="otp">
                    Verification Code
                </label>
                <div class="otp-boxes">
                    <div class="otp-input" aria-hidden="true">
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                        <span class="otp-digit"></span>
                    </div>
                    <input
                            type="text"
                            id="otp"
                            name="otp"
                            class="form-control otp-real"
                            maxlength="6"
                            pattern="[0-9]{6}"
                            placeholder="123456"
                            inputmode="numeric"
                            autocomplete="one-time-code"
                            aria-describedby="otp-hint"
                            required
                            autofocus>
                </div>
                <span class="form-hint" id="otp-hint">
                    <c:choose>
                        <c:when test="${not empty sessionScope.pendingResetEmail}">
                            Enter the 6-digit numeric code sent to
                            <strong><c:out value="${sessionScope.pendingResetEmail}"/></strong>. Code expires in 10 minutes.
                        </c:when>
                        <c:otherwise>
                            Enter the 6-digit code from your recovery email.
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary d-inline-flex align-items-center justify-content-center gap-2">
                    <span>Verify Code &amp; Continue</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                        <polyline points="12 5 19 12 12 19"></polyline>
                    </svg>
                </button>
            </div>

        </form>

        <div class="auth-links">
            <a href="${pageContext.request.contextPath}/forgot-password?action=cancel" class="d-inline-flex align-items-center gap-1">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <line x1="19" y1="12" x2="5" y2="12"></line>
                    <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
                <span>Change email address</span>
            </a>
            <span>
                Return to
                <a href="${pageContext.request.contextPath}/login">Login</a>
            </span>
        </div>

    </c:if>

</main>

<script src="${pageContext.request.contextPath}/assets/js/otp.js"></script>
