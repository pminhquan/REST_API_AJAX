<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<head>
    <title>Verify OTP</title>
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
        <h1 class="auth-header__title">Verify OTP</h1>
        <c:choose>
            <c:when test="${not empty success}">
                <p class="auth-header__subtitle">
                    Account verification completed successfully.
                </p>
            </c:when>
            <c:otherwise>
                <p class="auth-header__subtitle">
                    Enter the 6-digit verification code sent to your email to activate your account.
                </p>
            </c:otherwise>
        </c:choose>
    </header>

    <c:if test="${empty success}">
        <p class="auth-steps">
            <span class="auth-steps__current">Step 2 of 2</span>
            &middot; Email verification
        </p>
    </c:if>

    <c:if test="${empty success && not empty sessionScope.pendingVerifyEmail}">
        <div class="alert alert-info d-flex align-items-start gap-2 mb-4" role="note">
            <svg class="flex-shrink-0 mt-1" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            <div>
                <strong>Code Sent:</strong> A 6-digit verification code was sent to <strong><c:out value="${sessionScope.pendingVerifyEmail}"/></strong>. Codes expire after 10 minutes.
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

    <c:if test="${not empty success}">
        <div class="alert alert-success d-flex align-items-start gap-2 mb-4" role="status">
            <svg class="flex-shrink-0 mt-1" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
            <div>
                <c:out value="${success}"/>
            </div>
        </div>
        <div class="auth-actions">
            <a href="${pageContext.request.contextPath}/login" class="btn btn-primary d-inline-flex align-items-center justify-content-center gap-2">
                <span>Proceed to Login</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
            </a>
        </div>
        <div class="auth-links auth-links--center">
            <a href="${pageContext.request.contextPath}/home">Return to Home</a>
        </div>
    </c:if>

    <c:if test="${empty success}">

        <form class="auth-form" action="${pageContext.request.contextPath}/verify-otp" method="post">
            <input type="hidden" name="_csrf" value="${csrfToken}" />

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
                        <c:when test="${not empty sessionScope.pendingVerifyEmail}">
                            Enter the 6-digit numeric code sent to
                            <strong><c:out value="${sessionScope.pendingVerifyEmail}"/></strong>.
                        </c:when>
                        <c:otherwise>
                            Enter the 6-digit numeric code from your verification email.
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <div class="auth-actions">
                <button type="submit" class="btn btn-primary d-inline-flex align-items-center justify-content-center gap-2">
                    <span>Verify &amp; Activate Account</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                </button>
            </div>

        </form>

        <div class="auth-links">
            <a href="${pageContext.request.contextPath}/register" class="d-inline-flex align-items-center gap-1">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <line x1="19" y1="12" x2="5" y2="12"></line>
                    <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
                <span>Re-register or change email</span>
            </a>
            <span>
                Already activated?
                <a href="${pageContext.request.contextPath}/login">Login</a>
            </span>
        </div>

    </c:if>

</main>

<script src="${pageContext.request.contextPath}/assets/js/otp.js"></script>
