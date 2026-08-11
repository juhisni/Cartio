# Google Play pricing and distribution

Last updated: August 11, 2026

This document records the confirmed pricing configuration and the remaining distribution decision for Cartio (`fi.cartio`).

## Pricing and monetization

| Setting | Selection |
| --- | --- |
| App price | **Free** |
| Contains ads | **No** |
| In-app products | **None** |
| Subscriptions | **None** |

In Play Console, open **Products → App pricing** and select **Make your app free**.

Google Play does not allow an app that has been offered for free to later become a paid-download app under the same package name. A future Cartio release could still introduce optional Google Play in-app products or subscriptions, but doing so would require implementation work and updates to the store listing, privacy disclosures, and applicable Play Console declarations.

## Country and region availability

Country availability is configured independently for production and some testing tracks. The initial production scope must be selected before release.

Recommended option: distribute Cartio in all Google Play-supported countries and regions. Cartio is an offline utility with an English default and a complete Finnish translation, has no payments or regulated service, and does not depend on country-specific servers.

If a smaller initial launch is preferred, Finland can be used for the first production rollout and additional countries can be added later. When distributing in the European Union, keep availability consistent across EU countries unless a legitimate legal reason requires a restriction.

## Official guidance

- [Set up app pricing](https://support.google.com/googleplay/android-developer/answer/6334373?hl=en)
- [Distribute releases to specific countries](https://support.google.com/googleplay/android-developer/answer/7550024?hl=en)
- [Country and region distribution requirements](https://support.google.com/googleplay/android-developer/answer/6223646?hl=en)
