<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
    <title><c:choose><c:when test="${not empty product}">${fn:escapeXml(product.productname)} - Products</c:when><c:otherwise>Product Details</c:otherwise></c:choose></title>
</head>

<main class="container py-5">

    <div class="row justify-content-center">

        <div class="col-lg-10 col-xl-9">

            <c:set var="isManagement" value="${param.from == 'manage' or param.from == 'products' or (not empty sessionScope.authenticatedUserId and param.from != 'home' and param.from != 'catalog' and param.from != 'product')}"/>

            <c:choose>
                <c:when test="${param.from == 'home'}">
                    <c:set var="backUrl" value="${pageContext.request.contextPath}/home"/>
                    <c:set var="backLabel" value="Back to Home"/>
                </c:when>
                <c:when test="${isManagement}">
                    <c:set var="backUrl" value="${pageContext.request.contextPath}/products"/>
                    <c:set var="backLabel" value="Back to Management"/>
                </c:when>
                <c:otherwise>
                    <c:set var="backUrl" value="${pageContext.request.contextPath}/product"/>
                    <c:set var="backLabel" value="Back to Products"/>
                </c:otherwise>
            </c:choose>

            <c:choose>

                <c:when test="${not empty error}">

                    <div class="card shadow-sm detail-card">

                        <div class="card-body detail-card-body">

                            <div class="empty-state">

                                <h1 class="empty-state__title">
                                    Product unavailable
                                </h1>

                                <p class="empty-state__text" role="alert">
                                    <c:out value="${error}"/>
                                </p>

                            </div>

                            <div class="detail-actions detail-actions--center">

                                <a href="${backUrl}"
                                   class="btn btn-secondary">
                                    <c:out value="${backLabel}"/>
                                </a>

                            </div>

                        </div>

                    </div>

                </c:when>

                <c:otherwise>

                    <nav aria-label="breadcrumb" class="detail-breadcrumb-nav">
                        <ol class="breadcrumb detail-breadcrumb">
                            <li class="breadcrumb-item">
                                <a href="${pageContext.request.contextPath}/home">Home</a>
                            </li>
                            <c:choose>
                                <c:when test="${isManagement}">
                                    <li class="breadcrumb-item">
                                        <a href="${pageContext.request.contextPath}/products">Management</a>
                                    </li>
                                </c:when>
                                <c:otherwise>
                                    <li class="breadcrumb-item">
                                        <a href="${pageContext.request.contextPath}/product">Products</a>
                                    </li>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty product.category.categoryname}">
                                <li class="breadcrumb-item">
                                    <c:out value="${product.category.categoryname}"/>
                                </li>
                            </c:if>
                            <li class="breadcrumb-item active" aria-current="page">
                                <c:out value="${product.productname}"/>
                            </li>
                        </ol>
                    </nav>

                    <c:choose>
                        <c:when test="${isManagement}">
                            <div class="page-header">
                                <div class="page-header__text">
                                    <h1 class="page-header__title">Product Details</h1>
                                    <p class="page-header__subtitle">
                                        Product #<c:out value="${product.productid}"/>
                                    </p>
                                </div>

                                <div class="page-header__actions">
                                    <a href="${backUrl}"
                                       class="btn btn-ghost d-inline-flex align-items-center gap-1"
                                       aria-label="${fn:escapeXml(backLabel)}">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                            <line x1="19" y1="12" x2="5" y2="12"></line>
                                            <polyline points="12 19 5 12 12 5"></polyline>
                                        </svg>
                                        <span><c:out value="${backLabel}"/></span>
                                    </a>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="page-header page-header--detail">
                                <div class="page-header__actions">
                                    <a href="${backUrl}"
                                       class="btn btn-ghost d-inline-flex align-items-center gap-1"
                                       aria-label="${fn:escapeXml(backLabel)}">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                            <line x1="19" y1="12" x2="5" y2="12"></line>
                                            <polyline points="12 19 5 12 12 5"></polyline>
                                        </svg>
                                        <span><c:out value="${backLabel}"/></span>
                                    </a>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="card shadow-sm detail-card">

                        <div class="card-body detail-card-body">

                            <div class="detail-grid">

                                <div class="detail-media">

                                    <c:choose>

                                        <c:when test="${not empty product.images}">
                                            <c:choose>

                                                <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                                                    <img src="${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="detail-media__img"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='flex';"/>
                                                    <div class="detail-media__empty" style="display:none;">
                                                        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" class="detail-media__empty-icon">
                                                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                            <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                            <polyline points="21 15 16 10 5 21"></polyline>
                                                        </svg>
                                                        <span class="text-empty">No image available</span>
                                                    </div>
                                                </c:when>

                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/uploads/${fn:escapeXml(product.images)}"
                                                         alt="${fn:escapeXml(product.productname)}"
                                                         class="detail-media__img"
                                                         loading="lazy"
                                                         onerror="this.onerror=null; this.style.display='none'; this.nextElementSibling.style.display='flex';"/>
                                                    <div class="detail-media__empty" style="display:none;">
                                                        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" class="detail-media__empty-icon">
                                                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                            <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                            <polyline points="21 15 16 10 5 21"></polyline>
                                                        </svg>
                                                        <span class="text-empty">No image available</span>
                                                    </div>
                                                </c:otherwise>

                                            </c:choose>
                                        </c:when>

                                        <c:otherwise>
                                            <div class="detail-media__empty">
                                                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" class="detail-media__empty-icon">
                                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                                    <polyline points="21 15 16 10 5 21"></polyline>
                                                </svg>
                                                <span class="text-empty">
                                                    No image available
                                                </span>
                                            </div>
                                        </c:otherwise>

                                    </c:choose>

                                </div>

                                <div class="detail-body">

                                    <c:choose>
                                        <c:when test="${isManagement}">
                                            <div class="detail-titlebar">
                                                <h2 class="detail-title">
                                                    <c:out value="${product.productname}"/>
                                                </h2>
                                                <span class="badge ${product.status == 1 ? 'bg-success' : 'bg-secondary'} badge-status">
                                                    <c:out value="${product.status == 1 ? 'Active' : 'Inactive'}"/>
                                                </span>
                                            </div>

                                            <div class="detail-price-box">
                                                <span class="detail-price-label">Price</span>
                                                <p class="detail-price">
                                                    <span class="detail-price__amount"><fmt:formatNumber value="${product.price}" pattern="#,##0"/></span>
                                                    <span class="detail-price__currency" aria-label="Vietnamese Dong">₫</span>
                                                </p>
                                            </div>

                                            <div class="detail-facts">

                                                <div class="detail-fact">
                                                    <span class="detail-fact__label">Category</span>
                                                    <span class="detail-fact__value">
                                                        <c:out value="${product.category.categoryname}"/>
                                                    </span>
                                                </div>

                                                <div class="detail-fact">
                                                    <span class="detail-fact__label">Product ID</span>
                                                    <span class="detail-fact__value cell-id">
                                                        <c:out value="${product.productid}"/>
                                                    </span>
                                                </div>

                                            </div>

                                            <div class="detail-section detail-section--description">
                                                <h3 class="detail-section__title">Description</h3>
                                                <c:choose>
                                                    <c:when test="${not empty product.description}">
                                                        <p class="detail-description">
                                                            <c:out value="${product.description}"/>
                                                        </p>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <p class="detail-description detail-description--empty">
                                                            No description provided.
                                                        </p>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </c:when>

                                        <c:otherwise>
                                            <c:if test="${not empty product.category.categoryname}">
                                                <div class="detail-badge-wrap">
                                                    <span class="product-card__category detail-category">
                                                        <c:out value="${product.category.categoryname}"/>
                                                    </span>
                                                </div>
                                            </c:if>

                                            <div class="detail-titlebar">
                                                <h1 class="detail-title mb-0">
                                                    <c:out value="${product.productname}"/>
                                                </h1>
                                                <span class="badge ${product.status == 1 ? 'bg-success' : 'bg-secondary'} badge-status">
                                                    <c:out value="${product.status == 1 ? 'Active' : 'Inactive'}"/>
                                                </span>
                                            </div>

                                            <div class="detail-price-box">
                                                <span class="detail-price-label">Price</span>
                                                <p class="detail-price">
                                                    <span class="detail-price__amount"><fmt:formatNumber value="${product.price}" pattern="#,##0"/></span>
                                                    <span class="detail-price__currency" aria-label="Vietnamese Dong">₫</span>
                                                </p>
                                            </div>

                                            <div class="detail-section detail-section--description">
                                                <h2 class="detail-section__title">Description</h2>
                                                <c:choose>
                                                    <c:when test="${not empty product.description}">
                                                        <p class="detail-description">
                                                            <c:out value="${product.description}"/>
                                                        </p>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <p class="detail-description detail-description--empty">
                                                            No description provided.
                                                        </p>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                </div>

                            </div>

                            <div class="detail-actions">

                                <a href="${backUrl}"
                                   class="btn btn-secondary d-inline-flex align-items-center gap-1">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                        <line x1="19" y1="12" x2="5" y2="12"></line>
                                        <polyline points="12 19 5 12 12 5"></polyline>
                                    </svg>
                                    <span><c:out value="${backLabel}"/></span>
                                </a>

                                <c:choose>
                                    <c:when test="${not empty sessionScope.authenticatedUserId}">
                                        <a href="${pageContext.request.contextPath}/products/edit?id=${fn:escapeXml(product.productid)}"
                                           class="btn btn-primary d-inline-flex align-items-center gap-1">
                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                            </svg>
                                            <span>Edit Product</span>
                                        </a>
                                    </c:when>
                                    <c:when test="${param.from == 'home'}">
                                        <a href="${pageContext.request.contextPath}/product"
                                           class="btn btn-primary d-inline-flex align-items-center gap-1">
                                            <span>Browse Products</span>
                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                <line x1="5" y1="12" x2="19" y2="12"></line>
                                                <polyline points="12 5 19 12 12 19"></polyline>
                                            </svg>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/home"
                                           class="btn btn-primary d-inline-flex align-items-center gap-1">
                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                                                <polyline points="9 22 9 12 15 12 15 22"></polyline>
                                            </svg>
                                            <span>Home</span>
                                        </a>
                                    </c:otherwise>
                                </c:choose>

                            </div>

                        </div>

                    </div>

                </c:otherwise>

            </c:choose>

        </div>

    </div>

</main>
