<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
    <title>User Management - Admin</title>
</head>

<main class="container py-4 py-md-5">

    <div class="page-header d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div class="page-header__text">
            <h1 class="page-header__title h3 fw-bold mb-1">User Management</h1>
            <p class="page-header__subtitle text-secondary mb-0">
                <c:choose>
                    <c:when test="${totalUsers == 1}">1 user in total</c:when>
                    <c:otherwise>${totalUsers} users in total</c:otherwise>
                </c:choose>
            </p>
        </div>
        <div class="page-header__actions">
            <a href="${pageContext.request.contextPath}/admin/users/add" class="btn btn-primary d-inline-flex align-items-center gap-1">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <line x1="12" y1="5" x2="12" y2="19"></line>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                <span>Add User</span>
            </a>
        </div>
    </div>

    <!-- Alert Messages -->
    <c:if test="${param.message == 'add_success'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            User created successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${param.message == 'update_success'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            User updated successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${param.message == 'delete_success'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            User deleted successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${param.error == 'cannot_delete_self'}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            You cannot delete your own logged-in account.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${param.error == 'cannot_delete_last_admin'}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            Cannot delete the last active administrator account.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${param.error == 'delete_failed'}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            Failed to delete the user.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <!-- Search Toolbar -->
    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body p-3">
            <form method="get" action="${pageContext.request.contextPath}/admin/users" class="row g-2 align-items-center">
                <div class="col-12 col-md-6 col-lg-5">
                    <div class="input-group">
                        <span class="input-group-text bg-light border-end-0">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <circle cx="11" cy="11" r="8"></circle>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            </svg>
                        </span>
                        <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" class="form-control border-start-0" placeholder="Search by username, email, or fullname..." aria-label="Search users by username, email, or fullname">
                    </div>
                </div>
                <div class="col-auto">
                    <button type="submit" class="btn btn-primary">Search</button>
                    <c:if test="${not empty keyword}">
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-outline-secondary ms-1">Clear</a>
                    </c:if>
                </div>
            </form>
        </div>
    </div>

    <!-- Users Table -->
    <div class="card shadow-sm border-0 card--flush">
        <div class="card-body p-0 table-wrap table-responsive">
            <table class="table table-hover table--data align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" style="width: 60px;">#</th>
                        <th scope="col">User</th>
                        <th scope="col">Email</th>
                        <th scope="col">Phone</th>
                        <th scope="col" style="width: 110px;">Role</th>
                        <th scope="col" style="width: 100px;">Status</th>
                        <th scope="col" class="col-actions text-end" style="width: 140px;">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty users}">
                            <tr>
                                <td colspan="7" class="text-center py-5 text-secondary">
                                    <div class="empty-state">
                                        <p class="empty-state__title">No users found.</p>
                                        <p class="empty-state__text">
                                            <c:choose>
                                                <c:when test="${not empty keyword}">No users match your search keyword &ldquo;<c:out value="${keyword}"/>&rdquo;. Try clearing your filter.</c:when>
                                                <c:otherwise>No users registered in the system yet.</c:otherwise>
                                            </c:choose>
                                        </p>
                                    </div>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="u" items="${users}">
                                <tr>
                                    <td>
                                        <span class="cell-id">
                                            <c:out value="${u.id}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <span class="site-nav__avatar" style="width: 36px; height: 36px;">
                                                <c:choose>
                                                    <c:when test="${not empty u.images}">
                                                        <c:choose>
                                                            <c:when test="${fn:startsWith(u.images, 'http://') or fn:startsWith(u.images, 'https://')}">
                                                                <c:set var="avatarSrc" value="${u.images}" />
                                                            </c:when>
                                                            <c:when test="${fn:startsWith(u.images, '/')}">
                                                                <c:set var="avatarSrc" value="${pageContext.request.contextPath}${u.images}" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:set var="avatarSrc" value="${pageContext.request.contextPath}/uploads/${u.images}" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <img src="${fn:escapeXml(avatarSrc)}" alt="${fn:escapeXml(u.username)}" class="site-nav__avatar-img" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
                                                        <span class="site-nav__avatar-fallback" style="display:none;">${fn:toUpperCase(fn:substring(u.username, 0, 1))}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="site-nav__avatar-fallback">${fn:toUpperCase(fn:substring(u.username, 0, 1))}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                            <div>
                                                <div class="cell-strong"><c:out value="${u.username}" /></div>
                                                <c:if test="${not empty u.fullname}">
                                                    <small class="text-secondary d-block"><c:out value="${u.fullname}" /></small>
                                                </c:if>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="cell-truncate" tabindex="0" title="${fn:escapeXml(u.email)}">
                                            <c:out value="${u.email}" />
                                        </span>
                                    </td>
                                    <td><c:out value="${not empty u.phone ? u.phone : '-'}" /></td>
                                    <td>
                                        <span class="badge ${u.role == 'ADMIN' ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-secondary-subtle text-secondary border border-secondary-subtle'}">
                                            <c:out value="${u.role}" />
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.active}">
                                                <span class="badge bg-success-subtle text-success border border-success-subtle">Active</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Inactive</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <div class="table-actions justify-content-end">
                                            <a href="${pageContext.request.contextPath}/admin/users/edit?id=${u.id}"
                                               class="btn btn-sm btn-outline-secondary"
                                               aria-label="Edit user ${fn:escapeXml(u.username)}"
                                               title="Edit User">
                                                Edit
                                            </a>
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/admin/users/delete"
                                                  class="form-inline d-inline"
                                                  onsubmit="return confirm('Are you sure you want to delete user ${fn:escapeXml(u.username)}?');">
                                                <input type="hidden" name="_csrf" value="${csrfToken}" />
                                                <input type="hidden" name="id" value="${u.id}">
                                                <button type="submit"
                                                        class="btn btn-sm btn-outline-danger"
                                                        aria-label="Delete user ${fn:escapeXml(u.username)}"
                                                        title="Delete User">
                                                    Delete
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Pagination Controls -->
        <c:if test="${totalPages > 1}">
            <div class="pagination-bar">
                <span class="pagination-info">
                    Page ${currentPage} of ${totalPages}
                </span>
                <nav aria-label="Users pagination">
                    <ul class="pagination pagination-sm mb-0">
                        <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                            <c:choose>
                                <c:when test="${currentPage <= 1}">
                                    <span class="page-link" aria-disabled="true">Previous</span>
                                </c:when>
                                <c:otherwise>
                                    <a class="page-link" href="${pageContext.request.contextPath}/admin/users?page=${currentPage - 1}&amp;keyword=${fn:escapeXml(keyword)}" aria-label="Previous page">Previous</a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/admin/users?page=${i}&amp;keyword=${fn:escapeXml(keyword)}"${i == currentPage ? ' aria-current="page"' : ''} aria-label="Page ${i}">${i}</a>
                            </li>
                        </c:forEach>
                        <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                            <c:choose>
                                <c:when test="${currentPage >= totalPages}">
                                    <span class="page-link" aria-disabled="true">Next</span>
                                </c:when>
                                <c:otherwise>
                                    <a class="page-link" href="${pageContext.request.contextPath}/admin/users?page=${currentPage + 1}&amp;keyword=${fn:escapeXml(keyword)}" aria-label="Next page">Next</a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </ul>
                </nav>
            </div>
        </c:if>
    </div>

</main>
