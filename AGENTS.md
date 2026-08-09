# Cartio engineering rules

- Keep the app offline-first. Do not add networking unless explicitly requested.
- Build UI with Jetpack Compose only.
- Protect the minimal-tap product experience: a suggestion tap or typing plus Enter must add immediately.
- Preserve the feature-oriented architecture and immutable StateFlow UI state.
- Maintain complete Finnish and English support for visible text.
- Add or update tests for business-logic changes.
- Do not add a dependency without a specific, documented reason.
- Preserve `fi.cartio` as the application ID unless a migration is explicitly approved.
- Use Room transactions for multi-table list operations.
- Keep accessibility, 48 dp touch targets, large text, dark mode, IME, and system insets working.
