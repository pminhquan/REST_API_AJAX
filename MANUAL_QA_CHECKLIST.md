# Manual QA checklist

Use a disposable database or clearly marked test records. Record the build/WAR checksum, browser, database, SMTP mailbox, tester, and date before starting. Do not use production credentials or data.

## Public pages

- [ ] `GET /` loads the home page without server errors.
- [ ] `GET /home` resolves to the home page.
- [ ] `GET /product` shows the public catalog and product cards/table.
- [ ] Product search/filter results are correct and preserve the selected query.
- [ ] Product detail opens from the public catalog at `/products/detail?id=<id>`.
- [ ] Public product detail remains reachable without an authenticated session.
- [ ] Navigation, images, CSS, icons, and footer links load without 404s.
- [ ] Empty, invalid, and unavailable product states show usable feedback.

## Authentication flow

- [ ] `/login` renders with visible labels and a CSRF token.
- [ ] Invalid credentials show a clear error and do not create an authenticated session.
- [ ] Valid customer credentials reach the authenticated user experience.
- [ ] Valid admin credentials reach the admin experience.
- [ ] Logout clears the session and protected pages redirect to `/login`.
- [ ] Browser Back/Forward does not expose authenticated content after logout.
- [ ] Direct access to protected routes without a session is rejected or redirected.
- [ ] POST requests without a valid CSRF token are rejected.

## Registration and OTP flow

- [ ] `/register` accepts valid new-account data and rejects invalid or duplicate username/email data.
- [ ] Registration creates a pending verification state and routes to `/verify-otp`.
- [ ] The OTP arrives in the configured test mailbox; record delivery time and recipient.
- [ ] A valid registration OTP activates the account and permits login.
- [ ] An invalid, blank, reused, expired, or over-attempt OTP is rejected with clear feedback.
- [ ] Refreshing or directly opening `/verify-otp` without a pending registration does not bypass the flow.
- [ ] `/forgot-password` accepts a registered email and sends a password-reset OTP.
- [ ] A valid reset OTP reaches `/reset-password` and changes the password.
- [ ] The old password no longer works after reset; the new password does.
- [ ] OTP values never appear in URLs, page source, logs visible to the tester, or user-facing errors.

## Profile

- [ ] `/profile` requires authentication and renders the current user data.
- [ ] Profile fields save successfully and remain changed after reload and relogin.
- [ ] Duplicate or invalid email/username changes are rejected without partial updates.
- [ ] Valid avatar upload displays after save and survives reload/restart.
- [ ] Invalid extension, mismatched image content, and files over 5 MB are rejected.
- [ ] Leaving an existing avatar unchanged preserves it.
- [ ] Replacing an avatar does not break the new image or leave the old image unexpectedly exposed.

## Admin authorization

- [ ] A customer session cannot access `/admin`, `/categories`, `/admin/users`, or `/products`.
- [ ] A customer can still access public `/product` and public product detail.
- [ ] An admin session can access `/admin` and sees the admin decorator/navigation.
- [ ] Admin pages do not expose controls to unauthenticated or customer sessions.
- [ ] Direct URL access and POST actions enforce the same authorization as navigation links.
- [ ] Admin logout removes access to all admin pages.

## Category CRUD

- [ ] `/categories` loads with search, pagination, status, and action controls.
- [ ] Add category validates required fields and accepts valid JPG/JPEG/PNG/WEBP images.
- [ ] Added category appears in the list and in product category selectors.
- [ ] Edit category updates the intended record and preserves the existing image when no replacement is supplied.
- [ ] Delete removes an unused category only after the expected confirmation.
- [ ] Delete of a category referenced by products is rejected safely with clear feedback.
- [ ] CSRF is present on every category mutation form.

## User CRUD

- [ ] `/admin/users` loads with search, pagination, role, status, and action controls.
- [ ] Add user validates required data, role, uniqueness, and optional avatar upload.
- [ ] Edit user updates only the selected account and preserves unchanged fields/images.
- [ ] Role and active/inactive status render correctly after reload.
- [ ] Delete removes an allowed target after confirmation.
- [ ] An admin cannot delete the current account or the last remaining admin.
- [ ] CSRF is present on add, edit, and delete forms.

## Product CRUD

- [ ] `/products` loads as the protected management view with management actions.
- [ ] Add product validates name, description, price, category, status, and optional image.
- [ ] Added product appears in management search/list and public `/product` when active.
- [ ] Edit product updates the intended record and preserves the existing image when unchanged.
- [ ] Delete removes the intended product after confirmation and leaves unrelated products intact.
- [ ] Product detail links preserve the management/public context correctly.
- [ ] CSRF is present on add, edit, and delete forms.

## Search

- [ ] Product search returns only matching products and handles no-result text.
- [ ] Category search returns only matching categories and handles no-result text.
- [ ] User search matches username, email, and full name.
- [ ] Clear-filter controls restore the unfiltered first page.
- [ ] Search values are escaped and remain present when navigating between pages.
- [ ] Search with spaces, punctuation, mixed case, and HTML-like text does not break layout or markup.

## Pagination

- [ ] First-page Previous and last-page Next controls are visibly disabled and not actionable.
- [ ] Next, Previous, and numbered links reach the expected page.
- [ ] The current page is visibly and semantically identified.
- [ ] Search/filter parameters remain preserved across page links.
- [ ] Empty and one-page result sets do not show broken or misleading pagination.
- [ ] Tables remain usable at narrow viewport widths without clipping actions.

## Upload

- [ ] Valid `.jpg`, `.jpeg`, `.png`, and `.webp` files upload for profile, category, user, and product flows.
- [ ] An extension/content mismatch is rejected.
- [ ] Files over 5 MB and requests over 6 MB are rejected with actionable feedback.
- [ ] Uploads are stored under the configured persistent upload root, not only inside the temporary exploded WAR.
- [ ] Uploaded files remain available after an application restart/redeploy.
- [ ] Replacing/deleting an image does not delete an unrelated file.
- [ ] Direct traversal-style upload/read paths cannot escape the upload root.
- [ ] Uploaded images render with the correct content type and no broken-image placeholders.

## QA result

- Tester:
- Build/WAR checksum:
- Environment/database:
- SMTP mailbox:
- Browser and viewport:
- Date:
- Result: `PASS` / `FAIL` / `BLOCKED`
- Failed cases and evidence:
