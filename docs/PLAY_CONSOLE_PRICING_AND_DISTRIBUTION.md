# Google Play pricing and distribution

Last updated: August 11, 2026

This document records the confirmed pricing and production distribution configuration for Cartio (`fi.cartio`).

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

| Setting | Selection |
| --- | --- |
| Production availability | **All Google Play-supported countries and regions** |
| Geographic restrictions inside Cartio | **None** |

In Play Console, open **Production → Countries / regions**, choose **Add countries / regions**, and select all currently supported locations. Country availability is configured independently for production and some testing tracks.

This worldwide scope is appropriate because Cartio is an offline utility with an English default and a complete Finnish translation, has no payments or regulated service, and does not depend on country-specific servers. New supported locations can be added later when Google Play expands availability.

## Release check

Before each production rollout:

1. Confirm all desired supported countries and regions remain selected.
2. Confirm the default English store listing is complete so every market has readable metadata.
3. Keep the Finnish localized listing available for Finnish-speaking users.
4. Review whether future paid, regulated, networked, or country-specific features require distribution changes.

## Official guidance

- [Set up app pricing](https://support.google.com/googleplay/android-developer/answer/6334373?hl=en)
- [Distribute releases to specific countries](https://support.google.com/googleplay/android-developer/answer/7550024?hl=en)
- [Country and region distribution requirements](https://support.google.com/googleplay/android-developer/answer/6223646?hl=en)
