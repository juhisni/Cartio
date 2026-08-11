# Cartio

Cartio is a calm, fast, offline-first Android shopping list. Products are added with one tap or typing plus Enter, automatically categorized, and kept on-device.

## Features

- Room-persisted shopping list grouped into nine grocery categories
- Consecutive quick-add in a modal bottom sheet with offline suggestions, recent items, and frequent items
- Check, uncheck, and remove products
- Reset completion state or remove completed products with undo
- Collapse category sections while keeping unchecked counts visible
- Edit product names, quantities, units, and categories
- Choose contextual unit suggestions based on the product category
- Undo deleted products and saved lists
- Save, rename, delete, and restore reusable lists
- Duplicate saved lists with all products and list details preserved
- Manage the active list directly from the main view
- Share the active shopping list as plain text through Android's share sheet
- Finnish and English UI
- Light, dark, and system themes
- Edge-to-edge Material 3 interface with three-destination bottom navigation

## Technology

Kotlin, Jetpack Compose, Material 3, Navigation Compose, MVVM, Room, Hilt, Coroutines, StateFlow, DataStore, Gradle Kotlin DSL, and a version catalog. Minimum Android version is API 26. No feature uses a network connection.

## Run

1. Open the project in the current stable Android Studio.
2. Use JDK 17+ (Android Studio's bundled JBR is recommended).
3. Sync Gradle and run the `app` configuration on an API 26+ device.

From PowerShell:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

## Test

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

## Current limitations

- The bundled catalog is a curated collection of recognizable everyday Finnish/English grocery products adapted from Fineli Release 20. Nutritional, preparation, package-size, and synthetic variants are excluded.
- No barcode scanner, cloud sync, accounts, analytics, notifications, or networking.

## Privacy policy

The bilingual privacy policy is published with GitHub Pages from the `docs` directory:

<https://juhisni.github.io/Cartio/privacy/>

Legal and license notices are published at:

<https://juhisni.github.io/Cartio/legal/>

The audited Google Play Data safety answers are documented in [`docs/PLAY_CONSOLE_DATA_SAFETY.md`](docs/PLAY_CONSOLE_DATA_SAFETY.md).

Target-audience and IARC content-rating answers are documented in [`docs/PLAY_CONSOLE_AUDIENCE_AND_RATING.md`](docs/PLAY_CONSOLE_AUDIENCE_AND_RATING.md).

Pricing and distribution decisions are documented in [`docs/PLAY_CONSOLE_PRICING_AND_DISTRIBUTION.md`](docs/PLAY_CONSOLE_PRICING_AND_DISTRIBUTION.md).

Draft English and Finnish Google Play metadata is maintained in [`docs/PLAY_STORE_LISTING.md`](docs/PLAY_STORE_LISTING.md).
