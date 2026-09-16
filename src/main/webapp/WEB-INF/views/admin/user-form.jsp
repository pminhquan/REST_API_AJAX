<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
    <title>${action == 'add' ? 'Add User' : 'Edit User'} - Admin</title>
</head>

<main class="container py-4 py-md-5">

    <div class="row justify-content-center">
        <div class="col-12 col-lg-8">

            <div class="page-header mb-4 d-flex justify-content-between align-items-center">
                <div>
                    <h1 class="page-header__title h3 fw-bold mb-1">${action == 'add' ? 'Add User' : 'Edit User'}</h1>
                    <p class="page-header__subtitle text-secondary mb-0">
                        ${action == 'add' ? 'Create a new user account with role and credentials.' : 'Update user details, role, and active status.'}
                    </p>
                </div>
                <div class="page-header__actions">
                    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-ghost">
                        &larr; Back to Users
                    </a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">
                    <c:out value="${error}" />
                </div>
            </c:if>

            <div class="card shadow-sm border-0">
                <div class="card-body p-4">
                    <form method="post" action="${pageContext.request.contextPath}/admin/users/${action}" enctype="multipart/form-data">
                        <input type="hidden" name="_csrf" value="${csrfToken}" />
                        <c:if test="${action == 'edit'}">
                            <input type="hidden" name="id" value="${user.id}">
                        </c:if>

                        <div class="mb-3">
                            <label for="username" class="form-label fw-semibold">Username <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="username" name="username" value="${fn:escapeXml(user.username)}" required maxlength="50">
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label fw-semibold">Email <span class="text-danger">*</span></label>
                            <input type="email" class="form-control" id="email" name="email" value="${fn:escapeXml(user.email)}" required maxlength="100">
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label fw-semibold">
                                Password ${action == 'add' ? '<span class="text-danger">*</span>' : '<small class="text-secondary">(leave blank to keep current)</small>'}
                            </label>
                            <input type="password" class="form-control" id="password" name="password" ${action == 'add' ? 'required' : ''} minlength="6" placeholder="${action == 'add' ? 'At least 6 characters' : 'Enter new password to change'}">
                        </div>

                        <div class="row">
                            <div class="col-12 col-md-6 mb-3">
                                <label for="fullname" class="form-label fw-semibold">Full Name</label>
                                <input type="text" class="form-control" id="fullname" name="fullname" value="${fn:escapeXml(user.fullname)}" maxlength="100">
                            </div>
                            <div class="col-12 col-md-6 mb-3">
                                <label for="phone" class="form-label fw-semibold">Phone</label>
                                <input type="text" class="form-control" id="phone" name="phone" value="${fn:escapeXml(user.phone)}" maxlength="30">
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-12 col-md-6 mb-3">
                                <label for="role" class="form-label fw-semibold">Role</label>
                                <select class="form-select" id="role" name="role">
                                    <option value="CUSTOMER" ${user.role == 'CUSTOMER' ? 'selected' : ''}>CUSTOMER</option>
                                    <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                                </select>
                            </div>
                            <div class="col-12 col-md-6 mb-3 d-flex align-items-center pt-md-4">
                                <div class="form-check form-switch">
                                    <input class="form-check-input" type="checkbox" id="active" name="active" value="true" ${user.active or action == 'add' ? 'checked' : ''}>
                                    <label class="form-check-label fw-semibold" for="active">Account Active</label>
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="image" class="form-label fw-semibold">Avatar Image</label>
                            <input type="file" class="form-control" id="image" name="image" accept=".jpg,.jpeg,.png,.webp">
                            <div class="form-text">Supported formats: JPG, JPEG, PNG, WEBP. Max size: 5 MB.</div>
                            <c:if test="${not empty user.images}">
                                <div class="mt-2 text-secondary small">
                                    Current avatar: <code><c:out value="${user.images}"/></code>
                                </div>
                            </c:if>
                        </div>

                        <div class="form-actions">
                            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">Cancel</a>
                            <button type="submit" class="btn btn-primary">${action == 'add' ? 'Create User' : 'Save Changes'}</button>
                        </div>
                    </form>
                </div>
            </div>

        </div>
    </div>

</main>
