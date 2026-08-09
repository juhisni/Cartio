# Architecture

## Shape

Cartio is a single-activity Compose application using feature-oriented packages under `fi.cartio`.

- `core/model`: immutable domain-facing models and enums
- `core/database`: Hilt bindings and Room construction
- `core/localization`: runtime strings and persisted language/theme preferences
- `data/local`: Room entities, DAO, converters, database, and transactions
- `data/repository`: offline repository implementation and entity mapping
- `domain/repository`: UI-independent persistence contract
- `domain/suggestion`: category suggestion interface and bundled dictionary implementation
- `feature/*`: screen-specific immutable UI state, ViewModels, and Compose UI
- `CartioApp`: navigation shell, theme/language composition, bottom navigation, and quick-add presentation

## Data flow

Compose sends user intents to a Hilt-provided ViewModel. The ViewModel performs repository operations in `viewModelScope` and exposes immutable `StateFlow`. Room flows are the source of truth; the UI never maintains a second product list.

Saving/restoring a list is atomic through Room `@Transaction`. Usage metadata is updated when a product is added and powers recent/frequent suggestions.

## Category suggestion

`CategorySuggestionEngine` normalizes Unicode text, removes punctuation, checks a bilingual exact-alias dictionary, checks whole-word aliases, checks locally learned overrides first, and falls back to `Other`. The interface isolates the algorithm for future improvement without changing product entry.

## Localization and theming

Language and theme preferences use Preferences DataStore. Their flow is collected above navigation, so changes recompose all visible destinations immediately. The design system intentionally disables dynamic colors to preserve Cartio's green/forest identity.

## Offline boundary

The app has no Internet permission, client, account, telemetry, or remote service. Room and DataStore are the only persistence systems.
