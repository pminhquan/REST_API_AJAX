/**
 * AJAX Product Management
 * Uses fetch against /api/products and /api/categories REST endpoints.
 */
(() => {
    'use strict';

    function getContextPath() {
        const meta = document.querySelector('meta[name="context-path"]');
        if (meta && meta.content && meta.content !== '{{CONTEXT_PATH}}') {
            return meta.content.trim().replace(/\/+$/, '');
        }
        return '';
    }

    const CONTEXT_PATH = getContextPath();
    const API_BASE = `${CONTEXT_PATH}/api/products`;
    const CATEGORIES_API = `${CONTEXT_PATH}/api/categories`;

    // DOM Elements
    let formElement;
    let formTitleElement;
    let submitButtonElement;
    let cancelButtonElement;
    let productIdInput;
    let productNameInput;
    let categorySelect;
    let priceInput;
    let descriptionInput;
    let imagesInput;
    let statusSelect;
    let tableBody;
    let searchInput;
    let searchForm;
    let clearSearchButton;
    let alertContainer;

    // Cache of loaded categories
    let categoriesList = [];

    function init() {
        formElement = document.getElementById('product-form');
        formTitleElement = document.getElementById('form-title');
        submitButtonElement = document.getElementById('btn-submit');
        cancelButtonElement = document.getElementById('btn-cancel');
        productIdInput = document.getElementById('product-id');
        productNameInput = document.getElementById('product-name');
        categorySelect = document.getElementById('product-category');
        priceInput = document.getElementById('product-price');
        descriptionInput = document.getElementById('product-description');
        imagesInput = document.getElementById('product-images');
        statusSelect = document.getElementById('product-status');
        tableBody = document.getElementById('product-table-body');
        searchInput = document.getElementById('search-input');
        searchForm = document.getElementById('search-form');
        clearSearchButton = document.getElementById('btn-clear-search');
        alertContainer = document.getElementById('alert-container');

        if (formElement) {
            formElement.addEventListener('submit', handleFormSubmit);
        }
        if (cancelButtonElement) {
            cancelButtonElement.addEventListener('click', cancelEdit);
        }
        if (searchForm) {
            searchForm.addEventListener('submit', handleSearch);
        }
        if (clearSearchButton) {
            clearSearchButton.addEventListener('click', () => {
                if (searchInput) searchInput.value = '';
                loadProducts('');
            });
        }

        // Initial loads
        loadCategories().then(() => {
            loadProducts('', true);
        });
    }

    function getCsrfToken() {
        const meta = document.querySelector('meta[name="csrf-token"]');
        if (meta && meta.content && meta.content !== '{{CSRF_TOKEN}}') {
            return meta.content;
        }
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const parts = cookie.trim().split('=');
            if (parts[0] === 'XSRF-TOKEN' && parts[1]) {
                return decodeURIComponent(parts[1]);
            }
        }
        return '';
    }

    function showAlert(message, type = 'danger', append = false) {
        if (!alertContainer) return;
        const bgClass = type === 'success' ? 'alert-success' : 'alert-danger';
        const alertHtml = `
            <div class="alert ${bgClass}" role="alert">
                <span>${escapeHtml(message)}</span>
                <button type="button" class="alert-close" aria-label="Close" onclick="this.parentElement.remove();">&times;</button>
            </div>
        `;
        if (append && alertContainer.children.length > 0) {
            alertContainer.insertAdjacentHTML('beforeend', alertHtml);
        } else {
            alertContainer.innerHTML = alertHtml;
        }
        alertContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function hideAlert() {
        if (alertContainer) {
            alertContainer.innerHTML = '';
        }
    }

    async function parseErrorMessage(response) {
        try {
            const data = await response.json();
            if (data && data.message) {
                return data.message;
            }
        } catch (_) {
            // Not JSON
        }
        return `Request failed with status ${response.status} (${response.statusText || 'Error'})`;
    }

    async function loadCategories() {
        try {
            const response = await fetch(CATEGORIES_API, {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });
            if (!response.ok) {
                const errorMsg = await parseErrorMessage(response);
                showAlert(`Failed to load categories: ${errorMsg}`, 'danger');
                return;
            }
            categoriesList = await response.json();
            populateCategorySelect(categoriesList);
        } catch (error) {
            showAlert('Network error: Unable to load categories.', 'danger');
        }
    }

    function populateCategorySelect(categories) {
        if (!categorySelect) return;
        categorySelect.innerHTML = '<option value="">-- Select Category --</option>';
        if (Array.isArray(categories)) {
            categories.forEach(cat => {
                const opt = document.createElement('option');
                opt.value = cat.categoryid;
                opt.textContent = `${cat.categoryname} (ID: ${cat.categoryid})`;
                categorySelect.appendChild(opt);
            });
        }
    }

    async function loadProducts(keyword = '', preserveAlert = false) {
        if (!preserveAlert) {
            hideAlert();
        }
        if (tableBody) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center py-4 text-muted">Loading products...</td>
                </tr>
            `;
        }

        const url = keyword && keyword.trim()
            ? `${API_BASE}?keyword=${encodeURIComponent(keyword.trim())}`
            : API_BASE;

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });

            if (!response.ok) {
                const errorMsg = await parseErrorMessage(response);
                showAlert(errorMsg, 'danger', preserveAlert);
                if (tableBody) {
                    tableBody.innerHTML = `
                        <tr>
                            <td colspan="7" class="text-center py-4 text-danger">Failed to load products.</td>
                        </tr>
                    `;
                }
                return;
            }

            const products = await response.json();
            renderProducts(products);
        } catch (error) {
            showAlert('Network error: Unable to connect to server.', 'danger', preserveAlert);
            if (tableBody) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="7" class="text-center py-4 text-danger">Network error loading products.</td>
                    </tr>
                `;
            }
        }
    }

    function resolveImageUrl(image) {
        if (!image || !image.trim()) return '';
        const img = image.trim();
        if (img.startsWith('http://') || img.startsWith('https://')) {
            return img;
        }
        if (CONTEXT_PATH && img.startsWith(CONTEXT_PATH + '/')) {
            return img;
        }
        if (img.startsWith('/')) {
            return `${CONTEXT_PATH}${img}`;
        }
        return `${CONTEXT_PATH}/uploads/products/${img}`;
    }

    function renderProducts(products) {
        if (!tableBody) return;
        tableBody.innerHTML = '';

        if (!Array.isArray(products) || products.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center py-4 text-muted">No products found.</td>
                </tr>
            `;
            return;
        }

        products.forEach(prod => {
            const tr = document.createElement('tr');
            tr.id = `product-row-${prod.productid}`;

            const isActive = prod.status === 1;
            const statusBadge = isActive
                ? '<span class="badge badge-active">Active</span>'
                : '<span class="badge badge-inactive">Inactive</span>';

            const categoryName = prod.category ? prod.category.categoryname : 'N/A';

            const placeholderUrl = `${CONTEXT_PATH}/assets/images/placeholder.svg`;
            const imageHtml = prod.images && prod.images.trim()
                ? `<img src="${escapeHtml(resolveImageUrl(prod.images))}" alt="${escapeHtml(prod.productname)}" class="product-thumb" onerror="this.onerror=null;this.src='${escapeHtml(placeholderUrl)}';">`
                : '<span class="text-muted">No image</span>';

            const formattedPrice = typeof prod.price === 'number'
                ? prod.price.toLocaleString('en-US')
                : prod.price;

            tr.innerHTML = `
                <td class="font-mono">${prod.productid}</td>
                <td>${imageHtml}</td>
                <td>
                    <div class="font-semibold">${escapeHtml(prod.productname)}</div>
                    ${prod.description ? `<small class="text-muted">${escapeHtml(prod.description)}</small>` : ''}
                </td>
                <td><span class="badge badge-category">${escapeHtml(categoryName)}</span></td>
                <td class="font-mono font-semibold">${escapeHtml(formattedPrice)}</td>
                <td>${statusBadge}</td>
                <td>
                    <button type="button" class="btn btn-sm btn-outline-primary btn-edit" data-id="${prod.productid}">Edit</button>
                    <button type="button" class="btn btn-sm btn-outline-danger btn-delete" data-id="${prod.productid}" data-name="${escapeHtml(prod.productname)}">Delete</button>
                </td>
            `;

            const editBtn = tr.querySelector('.btn-edit');
            if (editBtn) {
                editBtn.addEventListener('click', () => editProduct(prod));
            }

            const deleteBtn = tr.querySelector('.btn-delete');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', () => deleteProduct(prod.productid, prod.productname));
            }

            tableBody.appendChild(tr);
        });
    }

    async function handleFormSubmit(event) {
        event.preventDefault();
        hideAlert();

        const name = productNameInput ? productNameInput.value.trim() : '';
        const catIdStr = categorySelect ? categorySelect.value : '';
        const priceStr = priceInput ? priceInput.value.trim() : '';
        const description = descriptionInput ? descriptionInput.value.trim() : '';
        const images = imagesInput ? imagesInput.value.trim() : '';
        const status = statusSelect ? parseInt(statusSelect.value, 10) : 1;
        const id = productIdInput && productIdInput.value ? parseInt(productIdInput.value, 10) : null;

        if (!name) {
            showAlert('Product name cannot be empty.', 'danger');
            if (productNameInput) productNameInput.focus();
            return;
        }
        if (name.length > 250) {
            showAlert('Product name must not exceed 250 characters.', 'danger');
            return;
        }
        if (!catIdStr || parseInt(catIdStr, 10) <= 0) {
            showAlert('Category is required. Please select a valid category.', 'danger');
            if (categorySelect) categorySelect.focus();
            return;
        }
        const categoryId = parseInt(catIdStr, 10);

        const price = Number(priceStr);
        if (isNaN(price) || price <= 0 || !Number.isFinite(price)) {
            showAlert('Price must be greater than 0.', 'danger');
            if (priceInput) priceInput.focus();
            return;
        }
        if (!Number.isInteger(price)) {
            showAlert('Price must be a whole number.', 'danger');
            if (priceInput) priceInput.focus();
            return;
        }
        if (description.length > 500) {
            showAlert('Description must not exceed 500 characters.', 'danger');
            return;
        }
        if (images.length > 500) {
            showAlert('Image path must not exceed 500 characters.', 'danger');
            return;
        }
        if (images.includes('..')) {
            showAlert('Invalid image reference.', 'danger');
            return;
        }

        const payload = {
            productname: name,
            description: description,
            price: price,
            images: images,
            status: status,
            category: {
                categoryid: categoryId
            }
        };

        const isUpdate = Boolean(id);
        const url = isUpdate ? `${API_BASE}/${id}` : API_BASE;
        const method = isUpdate ? 'PUT' : 'POST';

        if (isUpdate) {
            payload.productid = id;
        }

        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken;
        }

        try {
            setFormSubmitting(true);
            const response = await fetch(url, {
                method: method,
                headers: headers,
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorMsg = await parseErrorMessage(response);
                showAlert(errorMsg, 'danger');
                return;
            }

            const result = await response.json();
            showAlert(isUpdate ? `Product "${result.productname}" updated successfully.` : `Product "${result.productname}" created successfully.`, 'success');
            cancelEdit();
            const currentKeyword = searchInput ? searchInput.value : '';
            loadProducts(currentKeyword);
        } catch (error) {
            showAlert('Network error: Unable to save product.', 'danger');
        } finally {
            setFormSubmitting(false);
        }
    }

    async function editProduct(prod) {
        hideAlert();
        let targetProduct = prod;

        // Fetch fresh detail from GET /api/products/{id} if needed
        try {
            const res = await fetch(`${API_BASE}/${prod.productid}`, {
                headers: { 'Accept': 'application/json' }
            });
            if (res.ok) {
                targetProduct = await res.json();
            }
        } catch (_) {
            // fallback to passed product object
        }

        if (productIdInput) productIdInput.value = targetProduct.productid;
        if (productNameInput) productNameInput.value = targetProduct.productname || '';
        if (categorySelect && targetProduct.category) {
            categorySelect.value = String(targetProduct.category.categoryid || '');
        }
        if (priceInput) priceInput.value = targetProduct.price !== undefined ? targetProduct.price : '';
        if (descriptionInput) descriptionInput.value = targetProduct.description || '';
        if (imagesInput) imagesInput.value = targetProduct.images || '';
        if (statusSelect) statusSelect.value = targetProduct.status !== undefined ? String(targetProduct.status) : '1';

        if (formTitleElement) formTitleElement.textContent = `Edit Product #${targetProduct.productid}`;
        if (submitButtonElement) submitButtonElement.textContent = 'Update Product';
        if (cancelButtonElement) cancelButtonElement.classList.remove('d-none');

        if (productNameInput) productNameInput.focus();
        formElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function cancelEdit() {
        if (productIdInput) productIdInput.value = '';
        if (formElement) formElement.reset();
        if (statusSelect) statusSelect.value = '1';
        if (categorySelect) categorySelect.value = '';
        if (formTitleElement) formTitleElement.textContent = 'Add New Product';
        if (submitButtonElement) submitButtonElement.textContent = 'Save Product';
        if (cancelButtonElement) cancelButtonElement.classList.add('d-none');
    }

    async function deleteProduct(id, name) {
        hideAlert();
        const confirmed = window.confirm(`Are you sure you want to delete product "${name}" (ID: ${id})?`);
        if (!confirmed) return;

        const headers = {
            'Accept': 'application/json'
        };
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken;
        }

        try {
            const response = await fetch(`${API_BASE}/${id}`, {
                method: 'DELETE',
                headers: headers
            });

            if (response.status === 204) {
                showAlert(`Product "${name}" deleted successfully.`, 'success');
                const currentKeyword = searchInput ? searchInput.value : '';
                loadProducts(currentKeyword);
                return;
            }

            const errorMsg = await parseErrorMessage(response);
            showAlert(errorMsg, 'danger');
        } catch (error) {
            showAlert('Network error: Unable to delete product.', 'danger');
        }
    }

    function handleSearch(event) {
        event.preventDefault();
        const keyword = searchInput ? searchInput.value.trim() : '';
        loadProducts(keyword);
    }

    function setFormSubmitting(isSubmitting) {
        if (!submitButtonElement) return;
        submitButtonElement.disabled = isSubmitting;
        if (isSubmitting) {
            submitButtonElement.dataset.originalText = submitButtonElement.textContent;
            submitButtonElement.textContent = 'Saving...';
        } else if (submitButtonElement.dataset.originalText) {
            submitButtonElement.textContent = submitButtonElement.dataset.originalText;
        }
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    // Expose for testing
    window.ProductAjax = {
        loadProducts,
        loadCategories,
        cancelEdit
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
