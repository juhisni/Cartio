# Cartio

Cartio is a calm, fast, offline-first Android shopping list. Products are added with one tap or typing plus Enter, automatically categorized, and kept on-device.

## Features

- Room-persisted shopping list grouped into nine grocery categories
- Consecutive quick-add in a modal bottom sheet with offline suggestions, recent items, and frequent items
- Check, uncheck, and remove products
- Collapse category sections while keeping unchecked counts visible
- Edit product names, quantities, units, and categories
- Undo deleted products and saved lists
- Save, rename, delete, and restore reusable lists
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
