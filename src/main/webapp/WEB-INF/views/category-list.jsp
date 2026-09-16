<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
    <title>Category Management - Admin</title>
</head>

<main class="container py-5">

    <div class="page-header d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div class="page-header__text">
            <h1 class="page-header__title h3 fw-bold mb-1">Category Management</h1>
            <p class="page-header__subtitle text-secondary mb-0">
                <c:choose>
                    <c:when test="${totalCategories == 1}">1 category in total</c:when>
                    <c:otherwise>${totalCategories} categories in total</c:otherwise>
                </c:choose>
            </p>
        </div>

        <div class="page-header__actions">
            <a href="${pageContext.request.contextPath}/categories?action=add" class="btn btn-primary d-inline-flex align-items-center gap-1">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <line x1="12" y1="5" x2="12" y2="19"></line>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                <span>Add Category</span>
            </a>
        </div>
    </div>

    <c:if test="${param.message == 'add_success'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            Category added successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <c:if test="${param.message == 'update_success'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            Category updated successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <c:if test="${param.message == 'delete_success'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            Category deleted successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <c:if test="${param.error == 'invalid_id'}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            Invalid category ID provided.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <c:if test="${param.error == 'not_found'}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            Category does not exist.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <c:if test="${param.error == 'in_use'}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            Cannot delete category because it contains active products.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <c:if test="${param.error == 'delete_failed'}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            Failed to delete the category due to a database error.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <!-- Search Toolbar -->
    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body p-3">
            <form method="get" action="${pageContext.request.contextPath}/categories" class="row g-2 align-items-center">
                <div class="col-12 col-md-6 col-lg-5">
                    <div class="input-group">
                        <span class="input-group-text bg-light border-end-0">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <circle cx="11" cy="11" r="8"></circle>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            </svg>
                        </span>
                        <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" class="form-control border-start-0" placeholder="Search category by name..." aria-label="Search categories by name">
                    </div>
                </div>
                <div class="col-auto">
                    <button type="submit" class="btn btn-primary">Search</button>
                    <c:if test="${not empty keyword}">
                        <a href="${pageContext.request.contextPath}/categories" class="btn btn-outline-secondary ms-1">Clear</a>
                    </c:if>
                </div>
            </form>
        </div>
    </div>

    <!-- Category Table -->
    <div class="card shadow-sm border-0 card--flush">
        <div class="card-body p-0 table-wrap table-responsive">
            <table class="table table-hover table--data align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th scope="col" style="width: 60px;">#</th>
                        <th scope="col" style="width: 90px;">Image</th>
                        <th scope="col">Category Name</th>
                        <th scope="col" style="width: 120px;">Status</th>
                        <th scope="col" class="col-actions text-end" style="width: 160px;">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty categories}">
                            <tr>
                                <td colspan="5" class="text-center py-5 text-secondary">
                                    <div class="empty-state">
                                        <p class="empty-state__title">No categories found.</p>
                                        <p class="empty-state__text">
                                            <c:choose>
                                                <c:when test="${not empty keyword}">No categories match your search keyword &ldquo;<c:out value="${keyword}"/>&rdquo;. Try clearing your filter.</c:when>
                                                <c:otherwise>No categories have been created yet.</c:otherwise>
                                            </c:choose>
                                        </p>
                                    </div>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="category" items="${categories}">
                                <tr>
                                    <td>
                                        <span class="cell-id">
                                            <c:out value="${category.categoryid}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="category-table-img-wrap" style="width: 56px; height: 56px; overflow: hidden; border-radius: 8px; background: #f8f9fa;">
                                            <c:choose>
                                                <c:when test="${not empty category.images}">
                                                    <c:choose>
                                                        <c:when test="${fn:startsWith(category.images, 'http://') or fn:startsWith(category.images, 'https://')}">
                                                            <c:set var="catImgSrc" value="${category.images}" />
                                                        </c:when>
                                                        <c:when test="${fn:startsWith(category.images, '/')}">
                                                            <c:set var="catImgSrc" value="${pageContext.request.contextPath}${category.images}" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:set var="catImgSrc" value="${pageContext.request.contextPath}/uploads/${category.images}" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <img src="${fn:escapeXml(catImgSrc)}" alt="${fn:escapeXml(category.categoryname)}" class="w-100 h-100 object-fit-cover" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
                                                    <div class="w-100 h-100 d-none align-items-center justify-content-center text-secondary small bg-light">No img</div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="w-100 h-100 d-flex align-items-center justify-content-center text-secondary small bg-light">No img</div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="cell-strong"><c:out value="${category.categoryname}" /></div>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${category.status == 1}">
                                                <span class="badge bg-success-subtle text-success border border-success-subtle">Active</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">Inactive</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <div class="table-actions justify-content-end">
                                            <a href="${pageContext.request.contextPath}/categories?action=edit&amp;id=${category.categoryid}"
                                               class="btn btn-sm btn-outline-secondary"
                                               aria-label="Edit category ${fn:escapeXml(category.categoryname)}">
                                                Edit
                                            </a>
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/categories"
                                                  class="form-inline d-inline"
                                                  onsubmit="return confirm('Are you sure you want to delete category ${fn:escapeXml(category.categoryname)}?');">
                                                <input type="hidden" name="_csrf" value="${csrfToken}" />
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${category.categoryid}">
                                                <button type="submit"
                                                        class="btn btn-sm btn-outline-danger"
                                                        aria-label="Delete category ${fn:escapeXml(category.categoryname)}">
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
                    Page ${currentPage} of ${totalPages} (${totalCategories} total)
                </span>
                <nav aria-label="Category pagination">
                    <ul class="pagination pagination-sm mb-0">
                        <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                            <c:choose>
                                <c:when test="${currentPage <= 1}">
                                    <span class="page-link" aria-disabled="true">Previous</span>
                                </c:when>
                                <c:otherwise>
                                    <a class="page-link" href="${pageContext.request.contextPath}/categories?page=${currentPage - 1}&amp;keyword=${fn:escapeXml(keyword)}" aria-label="Previous page">Previous</a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/categories?page=${i}&amp;keyword=${fn:escapeXml(keyword)}"${i == currentPage ? ' aria-current="page"' : ''} aria-label="Page ${i}">${i}</a>
                            </li>
                        </c:forEach>
                        <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                            <c:choose>
                                <c:when test="${currentPage >= totalPages}">
                                    <span class="page-link" aria-disabled="true">Next</span>
                                </c:when>
                                <c:otherwise>
                                    <a class="page-link" href="${pageContext.request.contextPath}/categories?page=${currentPage + 1}&amp;keyword=${fn:escapeXml(keyword)}" aria-label="Next page">Next</a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </ul>
                </nav>
            </div>
        </c:if>
    </div>

</main>
