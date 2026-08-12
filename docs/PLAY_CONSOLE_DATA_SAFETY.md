# Google Play Data safety declaration

Last audited: August 11, 2026

This document records the intended Google Play Console answers for Cartio (`fi.cartio`). Re-audit these answers whenever permissions, dependencies, analytics, advertising, accounts, cloud features, or networking change.

## Audit result

Cartio:

- has no `INTERNET` permission;
- has no account system;
- includes no advertising, analytics, tracking, crash-reporting, or social SDK;
- requests no sensitive device permissions;
- stores shopping lists, product history, and preferences locally;
- does not transmit user data to the developer or third parties; and
- excludes app data from Android cloud backup and device-to-device transfer.

Google Play defines collection as transmitting user data off the device. Data that is accessed and processed only on-device is outside the scope of Data safety collection. On the audited build, Cartio therefore does not collect or share any Google Play user-data type.

## Play Console answers

Open **Policy and programs → App content → Data safety**.

### Data collection and security

| Question | Answer |
| --- | --- |
| Does your app collect or share any of the required user data types? | **No** |

Because the answer is **No**, do not select any data types or purposes. Questions about encryption in transit and developer-provided deletion mechanisms are not applicable to developer-collected data.

### Privacy policy

Use this public URL:

<https://juhisni.github.io/Cartio/privacy/>

### Account deletion

Cartio does not allow account creation, so Google Play's account-deletion requirement does not apply. Users can delete local content inside the app, clear Cartio's storage in Android settings, or uninstall Cartio. Cartio excludes app data from Android cloud backup and device-to-device transfer.

### Security-review claims

Do not claim an independent security review unless Cartio later completes a qualifying assessment. Do not claim data is encrypted in transit as a substitute for the **No collection** answer; Cartio does not transmit user data.

## Consistency check before every release

Confirm all of the following before submitting an update:

1. The merged release manifest still has no networking or sensitive permissions.
2. Runtime dependencies still contain no data-collecting SDKs.
3. The app still has no cloud sync, accounts, analytics, ads, crash reporting, or telemetry.
4. The in-app disclosure and public privacy policy still match actual behavior.
5. If any answer changes, update both Play Console and the privacy policy before publishing the release.

## Official guidance

- [Google Play Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en)
- [Google Play User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en)
