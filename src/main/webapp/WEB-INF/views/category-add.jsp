<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
    <title>Add Category</title>
</head>

<main class="container py-5">

    <div class="row justify-content-center">

        <div class="col-md-7">

            <div class="page-header">

                <div class="page-header__text">
                    <h1 class="page-header__title">Add Category</h1>
                    <p class="page-header__subtitle">
                        Create a new product category.
                    </p>
                </div>

                <div class="page-header__actions">
                    <a href="${pageContext.request.contextPath}/categories"
                       class="btn btn-ghost">
                        &larr; Back to Categories
                    </a>
                </div>

            </div>

            <div class="card shadow-sm">

                <div class="card-body">

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger" role="alert">
                            <c:out value="${error}"/>
                        </div>
                    </c:if>

                    <form
                            action="${pageContext.request.contextPath}/categories?action=insert"
                            method="post"
                            enctype="multipart/form-data">
                        <input type="hidden" name="_csrf" value="${csrfToken}" />

                        <div class="form-field">

                            <label class="form-label label-required" for="categoryname">
                                Category Name
                            </label>

                            <input
                                    type="text"
                                    id="categoryname"
                                    name="categoryname"
                                    class="form-control"
                                    value="${fn:escapeXml(param.categoryname)}"
                                    placeholder="e.g. Laptops, Smartphones, Audio"
                                    maxlength="100"
                                    required>

                            <span class="form-hint">
                                A clear, unique name shown in product forms and catalog filters.
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label" for="image">
                                Category Image
                            </label>

                            <input
                                    type="file"
                                    id="image"
                                    name="image"
                                    class="form-control"
                                    accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp">

                            <span class="form-hint">
                                Optional. Supported formats: JPG, JPEG, PNG, WEBP (maximum size 5 MB).
                            </span>

                        </div>

                        <div class="form-field">

                            <label class="form-label" for="status">
                                Status
                            </label>

                            <select
                                    id="status"
                                    name="status"
                                    class="form-select">

                                <option value="1"
                                        ${empty param.status or param.status == '1' ? 'selected' : ''}>
                                    Active - Visible in product forms and catalog
                                </option>

                                <option value="0"
                                        ${param.status == '0' ? 'selected' : ''}>
                                    Inactive - Hidden from active product creation
                                </option>

                            </select>

                            <span class="form-hint">
                                Inactive categories stay in the system but cannot be selected for new products.
                            </span>

                        </div>

                        <div class="form-actions">

                            <a
                                    href="${pageContext.request.contextPath}/categories"
                                    class="btn btn-secondary">
                                Cancel
                            </a>

                            <button
                                    type="submit"
                                    class="btn btn-primary">
                                Save Category
                            </button>

                        </div>

                    </form>

                </div>

            </div>

        </div>

    </div>

</main>