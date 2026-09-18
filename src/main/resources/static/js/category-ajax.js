/**
 * AJAX Category Management
 * Uses fetch against /api/categories REST endpoints.
 */
(() => {
    'use strict';

    const API_BASE = '/api/categories';

    // DOM Elements
    let formElement;
    let formTitleElement;
    let submitButtonElement;
    let cancelButtonElement;
    let categoryIdInput;
    let categoryNameInput;
    let imagesInput;
    let statusSelect;
    let tableBody;
    let searchInput;
    let searchForm;
    let clearSearchButton;
    let alertContainer;
    let emptyStateRow;

    function init() {
        formElement = document.getElementById('category-form');
        formTitleElement = document.getElementById('form-title');
        submitButtonElement = document.getElementById('btn-submit');
        cancelButtonElement = document.getElementById('btn-cancel');
        categoryIdInput = document.getElementById('category-id');
        categoryNameInput = document.getElementById('category-name');
        imagesInput = document.getElementById('category-images');
        statusSelect = document.getElementById('category-status');
        tableBody = document.getElementById('category-table-body');
        searchInput = document.getElementById('search-input');
        searchForm = document.getElementById('search-form');
        clearSearchButton = document.getElementById('btn-clear-search');
        alertContainer = document.getElementById('alert-container');
        emptyStateRow = document.getElementById('empty-state');

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
                loadCategories('');
            });
        }

        loadCategories('');
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

    function showAlert(message, type = 'danger') {
        if (!alertContainer) return;
        const bgClass = type === 'success' ? 'alert-success' : 'alert-danger';
        alertContainer.innerHTML = `
            <div class="alert ${bgClass}" role="alert">
                <span>${escapeHtml(message)}</span>
                <button type="button" class="alert-close" aria-label="Close" onclick="this.parentElement.remove();">&times;</button>
            </div>
        `;
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

    async function loadCategories(keyword = '') {
        hideAlert();
        if (tableBody) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center py-4 text-muted">Loading categories...</td>
                </tr>
            `;
        }

        const url = keyword && keyword.trim()
            ? `${API_BASE}?keyword=${encodeURIComponent(keyword.trim())}`
            : API_BASE;

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                const errorMsg = await parseErrorMessage(response);
                showAlert(errorMsg, 'danger');
                if (tableBody) {
                    tableBody.innerHTML = `
                        <tr>
                            <td colspan="5" class="text-center py-4 text-danger">Failed to load categories.</td>
                        </tr>
                    `;
                }
                return;
            }

            const categories = await response.json();
            renderCategories(categories);
        } catch (error) {
            showAlert('Network error: Unable to connect to server.', 'danger');
            if (tableBody) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center py-4 text-danger">Network error loading categories.</td>
                    </tr>
                `;
            }
        }
    }

    function renderCategories(categories) {
        if (!tableBody) return;
        tableBody.innerHTML = '';

        if (!Array.isArray(categories) || categories.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center py-4 text-muted">No categories found.</td>
                </tr>
            `;
            return;
        }

        categories.forEach(cat => {
            const tr = document.createElement('tr');
            tr.id = `category-row-${cat.categoryid}`;

            const isActive = cat.status === 1;
            const statusBadge = isActive
                ? '<span class="badge badge-active">Active</span>'
                : '<span class="badge badge-inactive">Inactive</span>';

            const imageHtml = cat.images && cat.images.trim()
                ? `<img src="${escapeHtml(cat.images.startsWith('http') || cat.images.startsWith('/') ? cat.images : '/uploads/' + cat.images)}" alt="${escapeHtml(cat.categoryname)}" class="category-thumb" onerror="this.onerror=null;this.src='/assets/images/logo.svg';">`
                : '<span class="text-muted">No image</span>';

            tr.innerHTML = `
                <td class="font-mono">${cat.categoryid}</td>
                <td>${imageHtml}</td>
                <td class="font-semibold">${escapeHtml(cat.categoryname)}</td>
                <td>${statusBadge}</td>
                <td>
                    <button type="button" class="btn btn-sm btn-outline-primary btn-edit" data-id="${cat.categoryid}">Edit</button>
                    <button type="button" class="btn btn-sm btn-outline-danger btn-delete" data-id="${cat.categoryid}" data-name="${escapeHtml(cat.categoryname)}">Delete</button>
                </td>
            `;

            const editBtn = tr.querySelector('.btn-edit');
            if (editBtn) {
                editBtn.addEventListener('click', () => editCategory(cat));
            }

            const deleteBtn = tr.querySelector('.btn-delete');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', () => deleteCategory(cat.categoryid, cat.categoryname));
            }

            tableBody.appendChild(tr);
        });
    }

    async function handleFormSubmit(event) {
        event.preventDefault();
        hideAlert();

        const name = categoryNameInput ? categoryNameInput.value.trim() : '';
        const images = imagesInput ? imagesInput.value.trim() : '';
        const status = statusSelect ? parseInt(statusSelect.value, 10) : 1;
        const id = categoryIdInput && categoryIdInput.value ? parseInt(categoryIdInput.value, 10) : null;

        if (!name) {
            showAlert('Category name cannot be empty.', 'danger');
            if (categoryNameInput) categoryNameInput.focus();
            return;
        }
        if (name.length > 100) {
            showAlert('Category name must not exceed 100 characters.', 'danger');
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
            categoryname: name,
            images: images,
            status: status
        };

        const isUpdate = Boolean(id);
        const url = isUpdate ? `${API_BASE}/${id}` : API_BASE;
        const method = isUpdate ? 'PUT' : 'POST';

        if (isUpdate) {
            payload.categoryid = id;
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
            showAlert(isUpdate ? `Category "${result.categoryname}" updated successfully.` : `Category "${result.categoryname}" created successfully.`, 'success');
            cancelEdit();
            const currentKeyword = searchInput ? searchInput.value : '';
            loadCategories(currentKeyword);
        } catch (error) {
            showAlert('Network error: Unable to save category.', 'danger');
        } finally {
            setFormSubmitting(false);
        }
    }

    function editCategory(cat) {
        hideAlert();
        if (categoryIdInput) categoryIdInput.value = cat.categoryid;
        if (categoryNameInput) categoryNameInput.value = cat.categoryname || '';
        if (imagesInput) imagesInput.value = cat.images || '';
        if (statusSelect) statusSelect.value = cat.status !== undefined ? String(cat.status) : '1';

        if (formTitleElement) formTitleElement.textContent = `Edit Category #${cat.categoryid}`;
        if (submitButtonElement) submitButtonElement.textContent = 'Update Category';
        if (cancelButtonElement) cancelButtonElement.classList.remove('d-none');

        if (categoryNameInput) categoryNameInput.focus();
        formElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function cancelEdit() {
        if (categoryIdInput) categoryIdInput.value = '';
        if (formElement) formElement.reset();
        if (statusSelect) statusSelect.value = '1';
        if (formTitleElement) formTitleElement.textContent = 'Add New Category';
        if (submitButtonElement) submitButtonElement.textContent = 'Save Category';
        if (cancelButtonElement) cancelButtonElement.classList.add('d-none');
    }

    async function deleteCategory(id, name) {
        hideAlert();
        const confirmed = window.confirm(`Are you sure you want to delete category "${name}" (ID: ${id})?`);
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
                showAlert(`Category "${name}" deleted successfully.`, 'success');
                const currentKeyword = searchInput ? searchInput.value : '';
                loadCategories(currentKeyword);
                return;
            }

            const errorMsg = await parseErrorMessage(response);
            showAlert(errorMsg, 'danger');
        } catch (error) {
            showAlert('Network error: Unable to delete category.', 'danger');
        }
    }

    function handleSearch(event) {
        event.preventDefault();
        const keyword = searchInput ? searchInput.value.trim() : '';
        loadCategories(keyword);
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

    // Expose for debugging if needed
    window.CategoryAjax = {
        loadCategories,
        cancelEdit
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
