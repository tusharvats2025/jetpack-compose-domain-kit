<div align="center">

# ComposeDomainKit

**Jetpack Compose screens you copy, not install.**

Native Android screens, from login to checkout. One Kotlin file each.

[Browse the screens](https://composedomainkit.vercel.app) · [Request a screen](../../issues/new?template=screen-request.yml) · [Report a problem](../../issues/new?template=screen-bug.yml)

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?logo=materialdesign&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-green)

</div>

---

Each screen is one standalone Kotlin file: no library to add, no icon pack, no shared
code. Copy the file into your app, pass your state, handle the events. Because nothing
is installed, nothing pins your Compose or Kotlin version.

The website has a live preview of every screen, a colour picker, and the full source
with a usage example. This repo is the same code, ready to clone or copy.

## Use a screen

1. Copy a file from [`screens/`](screens) into your project.
2. Change its `package` line to yours.
3. Call it with your state and handle its events:

```kotlin
LoginCenteredLogo(
    state = state,                 // LoginCenteredLogoState, from your ViewModel
    onEvent = viewModel::onEvent,  // LoginCenteredLogoEvent: Submit, ForgotPassword, ...
)
```

Every screen follows the same shape, named after its function:

| Type | What it holds |
|---|---|
| `…State` | Everything the screen shows: field values, loading, errors. |
| `…Event` | Everything the user can do. One `onEvent` callback receives them. |
| `…Strings` | All visible text, with English defaults. Pass your own to translate. |
| `…Colors` | Only on screens with a fixed look (brutalist, editorial, dark). |

Screens hold no ViewModel and no business logic, so they work with any architecture
and are easy to test. Open the file in Android Studio to see its previews.

## Requirements

- Jetpack Compose with **Material 3**, Compose BOM **2025.04.00** or newer.
- Kotlin with the Compose compiler plugin.
- [`BlockingForceUpdate`](screens/feedback/BlockingForceUpdate.kt) also needs
  `androidx.activity:activity-compose` (for `BackHandler`). No other screen needs
  anything beyond Compose.

## Theming

Screens read colours, type and shapes from your `MaterialTheme`. Set your brand colour
once and every screen follows:

```kotlin
MaterialTheme(
    colorScheme = lightColorScheme(primary = Color(0xFF3949AB)),
) {
    LoginCenteredLogo(state = state, onEvent = viewModel::onEvent)
}
```

- Some screens take an `accent` parameter to override the brand colour just for that
  screen.
- Screens with a fixed look take a `…Colors` object instead of following the theme.
- Each file's previews use a private `PreviewColors` scheme so Android Studio shows the
  same colours as the website. It only affects previews; your app uses your theme.

Screens are built edge to edge and keep their content clear of the system bars, so call
`enableEdgeToEdge()` in your activity. Text fields scroll themselves above the keyboard.

## Screens

89 screens in six categories. Each category folder has its own list.

| Category | Screens | |
|---|---|---|
| [Login](screens/login) | 16 | Sign in, sign up, PIN, passkeys and recovery. |
| [Onboarding](screens/onboarding) | 17 | Welcome, permissions, pickers and first-run setup. |
| [Profile](screens/profile) | 15 | Profile headers, stats, edit forms and account pages. |
| [Settings](screens/settings) | 12 | Grouped lists, appearance, notifications and privacy. |
| [Checkout](screens/checkout) | 17 | Cart, payment, paywalls, receipts and order tracking. |
| [Empty and error](screens/feedback) | 12 | Empty lists, offline, errors, maintenance and force update. |

<details>
<summary><b>Login</b> · 16 screens</summary>

| Screen | Function | What it is | |
|---|---|---|---|
| Neo-brutalist | [`LoginBrutalist`](screens/login/LoginBrutalist.kt) | Thick borders, hard shadows and monospace labels. Takes its own colours. | [Preview](https://composedomainkit.vercel.app/screens/login/neo-brutalist) |
| Centered logo | [`LoginCenteredLogo`](screens/login/LoginCenteredLogo.kt) | Logo, two outlined fields, a primary button and one provider button, all centred. | [Preview](https://composedomainkit.vercel.app/screens/login/centered-logo) |
| Check your email | [`LoginCheckEmail`](screens/login/LoginCheckEmail.kt) | Confirms where the link went, with a resend timer and a spam tip. | [Preview](https://composedomainkit.vercel.app/screens/login/check-your-email) |
| Dark scheme with biometrics | [`LoginDarkBiometric`](screens/login/LoginDarkBiometric.kt) | Always-dark sign in with a bright accent, a fingerprint button and a create-account card. | [Preview](https://composedomainkit.vercel.app/screens/login/dark-scheme) |
| Fashion editorial | [`LoginFashionEditorial`](screens/login/LoginFashionEditorial.kt) | Full-bleed campaign image, serif headline and guest browsing up front. | [Preview](https://composedomainkit.vercel.app/screens/login/fashion-editorial) |
| Forgot password | [`LoginForgotPassword`](screens/login/LoginForgotPassword.kt) | One email field that sends a reset link. | [Preview](https://composedomainkit.vercel.app/screens/login/forgot-password) |
| Large title | [`LoginLargeTitle`](screens/login/LoginLargeTitle.kt) | A large start-aligned headline, filled fields and a button pinned above the keyboard. | [Preview](https://composedomainkit.vercel.app/screens/login/large-title) |
| Minimal | [`LoginMinimal`](screens/login/LoginMinimal.kt) | Type-led and monochrome, with underline fields and a square full-width button. | [Preview](https://composedomainkit.vercel.app/screens/login/minimal) |
| OTP verify (step 2 of 2) | [`LoginOtpVerify`](screens/login/LoginOtpVerify.kt) | Six-box code entry with a resend timer and SMS auto-read hint. | [Preview](https://composedomainkit.vercel.app/screens/login/otp-verify) |
| Phone number (step 1 of 2) | [`LoginPhoneNumber`](screens/login/LoginPhoneNumber.kt) | Country code and mobile number, step one of phone sign-in. | [Preview](https://composedomainkit.vercel.app/screens/login/phone-number) |
| PIN unlock (fintech) | [`LoginPinUnlock`](screens/login/LoginPinUnlock.kt) | Returning-user unlock with PIN dots, a keypad and a fingerprint key. | [Preview](https://composedomainkit.vercel.app/screens/login/pin-unlock) |
| Set new password | [`LoginSetNewPassword`](screens/login/LoginSetNewPassword.kt) | New password with strength, a match check and sign-out elsewhere. | [Preview](https://composedomainkit.vercel.app/screens/login/set-new-password) |
| Sign up | [`LoginSignUp`](screens/login/LoginSignUp.kt) | Name, email and password with a live strength meter and terms consent. | [Preview](https://composedomainkit.vercel.app/screens/login/sign-up) |
| Social-first | [`LoginSocialFirst`](screens/login/LoginSocialFirst.kt) | Provider buttons first, with an email sign-in link as the fallback. | [Preview](https://composedomainkit.vercel.app/screens/login/social-first) |
| Split hero | [`LoginSplitHero`](screens/login/LoginSplitHero.kt) | A brand-coloured hero with the form on a rounded sheet over it. | [Preview](https://composedomainkit.vercel.app/screens/login/split-hero) |
| Workspace SSO (enterprise) | [`LoginWorkspaceSso`](screens/login/LoginWorkspaceSso.kt) | Work email first; the domain decides whether to route to company sign-in. | [Preview](https://composedomainkit.vercel.app/screens/login/workspace-sso) |

</details>

<details>
<summary><b>Onboarding</b> · 17 screens</summary>

| Screen | Function | What it is | |
|---|---|---|---|
| Swipe carousel | [`OnboardingCarousel`](screens/onboarding/OnboardingCarousel.kt) | Three swipeable benefit pages with dots and Skip. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/swipe-carousel) |
| Photos and prompts (dating) | [`OnboardingDatingPhotos`](screens/onboarding/OnboardingDatingPhotos.kt) | A photo grid with a main photo, and written prompts. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/photos-and-prompts) |
| Delivery location (food delivery) | [`OnboardingDeliveryLocation`](screens/onboarding/OnboardingDeliveryLocation.kt) | A map with a current-location button and address search. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/delivery-location) |
| Feature list | [`OnboardingFeatureList`](screens/onboarding/OnboardingFeatureList.kt) | Four benefit rows and a single Continue. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/feature-list) |
| Goal and body (fitness) | [`OnboardingFitnessGoals`](screens/onboarding/OnboardingFitnessGoals.kt) | Goal, height, weight and activity level with unit toggles. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/goal-and-body) |
| Consent and basics (healthcare) | [`OnboardingHealthConsent`](screens/onboarding/OnboardingHealthConsent.kt) | Medical basics and explicit consent before a first consultation. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/consent-and-basics) |
| Interest picker | [`OnboardingInterestPicker`](screens/onboarding/OnboardingInterestPicker.kt) | A chip grid with a minimum selection and a live count. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/interest-picker) |
| Join workspace (enterprise) | [`OnboardingJoinWorkspace`](screens/onboarding/OnboardingJoinWorkspace.kt) | An invite card and team picker for a B2B workspace. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/join-workspace) |
| KYC intro (fintech) | [`OnboardingKycIntro`](screens/onboarding/OnboardingKycIntro.kt) | A documents checklist, a time estimate and a privacy note. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/kyc-intro) |
| Permission denied | [`OnboardingPermissionDenied`](screens/onboarding/OnboardingPermissionDenied.kt) | What to do after a permission was refused. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/permission-denied) |
| Permission primer | [`OnboardingPermissionPrimer`](screens/onboarding/OnboardingPermissionPrimer.kt) | Explains a permission before the system dialog asks for it. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/permission-primer) |
| Preparing | [`OnboardingPreparing`](screens/onboarding/OnboardingPreparing.kt) | A progress ring and setup checklist while the app personalises. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/preparing) |
| Profile setup | [`OnboardingProfileSetup`](screens/onboarding/OnboardingProfileSetup.kt) | Photo, name and a username availability check. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/profile-setup) |
| Quiz step | [`OnboardingQuizStep`](screens/onboarding/OnboardingQuizStep.kt) | One question per screen with option cards and progress. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/quiz-step) |
| Resume setup | [`OnboardingResume`](screens/onboarding/OnboardingResume.kt) | Picks up an unfinished setup where it stopped. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/resume-setup) |
| Style picker (fashion) | [`OnboardingStylePicker`](screens/onboarding/OnboardingStylePicker.kt) | A look grid and usual size to tune a fashion feed. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/style-picker) |
| Single welcome | [`OnboardingWelcome`](screens/onboarding/OnboardingWelcome.kt) | A brand hero with Get started and an existing-account link. | [Preview](https://composedomainkit.vercel.app/screens/onboarding/single-welcome) |

</details>

<details>
<summary><b>Profile</b> · 15 screens</summary>

| Screen | Function | What it is | |
|---|---|---|---|
| Account hub | [`ProfileAccountHub`](screens/profile/ProfileAccountHub.kt) | A header card and grouped menu: orders, payments, settings, log out. | [Preview](https://composedomainkit.vercel.app/screens/profile/account-hub) |
| Profile completion | [`ProfileCompletion`](screens/profile/ProfileCompletion.kt) | A completion ring and a checklist of what is missing. | [Preview](https://composedomainkit.vercel.app/screens/profile/profile-completion) |
| Cover and stats | [`ProfileCoverStats`](screens/profile/ProfileCoverStats.kt) | Cover, avatar, follower counts and a tabbed post grid. | [Preview](https://composedomainkit.vercel.app/screens/profile/cover-and-stats) |
| Dating | [`ProfileDating`](screens/profile/ProfileDating.kt) | Photo first, a prompt card, interests, and report or block always visible. | [Preview](https://composedomainkit.vercel.app/screens/profile/dating) |
| Directory profile (enterprise) | [`ProfileDirectory`](screens/profile/ProfileDirectory.kt) | A colleague profile with role, manager and contact actions. | [Preview](https://composedomainkit.vercel.app/screens/profile/directory-profile) |
| Edit profile | [`ProfileEdit`](screens/profile/ProfileEdit.kt) | Photo, name, username, a bio counter and an unsaved-changes bar. | [Preview](https://composedomainkit.vercel.app/screens/profile/edit-profile) |
| Fintech account | [`ProfileFintechAccount`](screens/profile/ProfileFintechAccount.kt) | KYC status, UPI ID, linked banks and the daily limit. | [Preview](https://composedomainkit.vercel.app/screens/profile/fintech-account) |
| Fitness | [`ProfileFitness`](screens/profile/ProfileFitness.kt) | Streak, totals, a weekly chart and achievement badges. | [Preview](https://composedomainkit.vercel.app/screens/profile/fitness) |
| Learning progress (education) | [`ProfileLearning`](screens/profile/ProfileLearning.kt) | Resume a course, learning totals and certificates. | [Preview](https://composedomainkit.vercel.app/screens/profile/learning-progress) |
| Minimal card | [`ProfileMinimalCard`](screens/profile/ProfileMinimalCard.kt) | An avatar and a few editable account rows. | [Preview](https://composedomainkit.vercel.app/screens/profile/minimal-card) |
| Patient and family (healthcare) | [`ProfilePatient`](screens/profile/ProfilePatient.kt) | A patient profile with family members and medical records. | [Preview](https://composedomainkit.vercel.app/screens/profile/patient-and-family) |
| Who's watching | [`ProfilePicker`](screens/profile/ProfilePicker.kt) | Pick a profile or add one, for shared and family accounts. | [Preview](https://composedomainkit.vercel.app/screens/profile/who-s-watching) |
| Professional (freelance) | [`ProfileProfessional`](screens/profile/ProfileProfessional.kt) | Rating, skills, an experience timeline and a Hire button. | [Preview](https://composedomainkit.vercel.app/screens/profile/professional) |
| Shop account | [`ProfileShopAccount`](screens/profile/ProfileShopAccount.kt) | Loyalty points, quick tiles and the latest order. | [Preview](https://composedomainkit.vercel.app/screens/profile/shop-account) |
| Style profile (fashion) | [`ProfileStyle`](screens/profile/ProfileStyle.kt) | Sizes, fit preference, style tags and saved looks. | [Preview](https://composedomainkit.vercel.app/screens/profile/style-profile) |

</details>

<details>
<summary><b>Settings</b> · 12 screens</summary>

| Screen | Function | What it is | |
|---|---|---|---|
| Appearance | [`SettingsAppearance`](screens/settings/SettingsAppearance.kt) | Theme, text size and accent, with a live preview. | [Preview](https://composedomainkit.vercel.app/screens/settings/appearance) |
| Delete account | [`SettingsDeleteAccount`](screens/settings/SettingsDeleteAccount.kt) | Consequences, a grace period and typed confirmation. | [Preview](https://composedomainkit.vercel.app/screens/settings/delete-account) |
| Discovery (dating) | [`SettingsDiscovery`](screens/settings/SettingsDiscovery.kt) | Who to show, age range, distance and incognito. | [Preview](https://composedomainkit.vercel.app/screens/settings/discovery) |
| Security and limits (fintech) | [`SettingsFintechSecurity`](screens/settings/SettingsFintechSecurity.kt) | App lock, PIN and UPI transaction limits. | [Preview](https://composedomainkit.vercel.app/screens/settings/security-and-limits) |
| Units and health data (fitness) | [`SettingsFitnessUnits`](screens/settings/SettingsFitnessUnits.kt) | Units and per-type Health Connect permissions. | [Preview](https://composedomainkit.vercel.app/screens/settings/units-and-health-data) |
| Grouped list | [`SettingsGroupedList`](screens/settings/SettingsGroupedList.kt) | Grouped settings that use every row type once. | [Preview](https://composedomainkit.vercel.app/screens/settings/grouped-list) |
| Help and support | [`SettingsHelp`](screens/settings/SettingsHelp.kt) | Help topics, popular questions and contact options. | [Preview](https://composedomainkit.vercel.app/screens/settings/help-and-support) |
| Language | [`SettingsLanguage`](screens/settings/SettingsLanguage.kt) | A searchable language list, each in its own script. | [Preview](https://composedomainkit.vercel.app/screens/settings/language) |
| Notifications | [`SettingsNotifications`](screens/settings/SettingsNotifications.kt) | Push and email per category, plus quiet hours. | [Preview](https://composedomainkit.vercel.app/screens/settings/notifications) |
| Searchable settings | [`SettingsSearch`](screens/settings/SettingsSearch.kt) | Search across settings with the path under each result. | [Preview](https://composedomainkit.vercel.app/screens/settings/searchable-settings) |
| Security | [`SettingsSecurity`](screens/settings/SettingsSecurity.kt) | Password, two-step verification and signed-in devices. | [Preview](https://composedomainkit.vercel.app/screens/settings/security) |
| Subscription | [`SettingsSubscription`](screens/settings/SettingsSubscription.kt) | Plan, renewal, usage, billing history and cancel. | [Preview](https://composedomainkit.vercel.app/screens/settings/subscription) |

</details>

<details>
<summary><b>Checkout</b> · 17 screens</summary>

| Screen | Function | What it is | |
|---|---|---|---|
| Address picker | [`CheckoutAddressPicker`](screens/checkout/CheckoutAddressPicker.kt) | Saved addresses, a default badge and current location. | [Preview](https://composedomainkit.vercel.app/screens/checkout/address-picker) |
| Booking (events) | [`CheckoutBooking`](screens/checkout/CheckoutBooking.kt) | A seat-hold countdown, the event card and attendee details. | [Preview](https://composedomainkit.vercel.app/screens/checkout/booking) |
| Cart | [`CheckoutCart`](screens/checkout/CheckoutCart.kt) | Quantity steppers, a coupon, the price breakdown and a sticky total. | [Preview](https://composedomainkit.vercel.app/screens/checkout/cart) |
| Consultation booking (healthcare) | [`CheckoutConsultation`](screens/checkout/CheckoutConsultation.kt) | Doctor card, video or clinic, time slots and patient. | [Preview](https://composedomainkit.vercel.app/screens/checkout/consultation-booking) |
| Course purchase (education) | [`CheckoutCourse`](screens/checkout/CheckoutCourse.kt) | What's included, an applied coupon and the refund window. | [Preview](https://composedomainkit.vercel.app/screens/checkout/course-purchase) |
| Fashion bag | [`CheckoutFashionBag`](screens/checkout/CheckoutFashionBag.kt) | Inline size change, a low-stock warning and free returns. | [Preview](https://composedomainkit.vercel.app/screens/checkout/fashion-bag) |
| Food delivery | [`CheckoutFoodDelivery`](screens/checkout/CheckoutFoodDelivery.kt) | Veg markers, a rider tip, drop-off notes and the ETA. | [Preview](https://composedomainkit.vercel.app/screens/checkout/food-delivery) |
| Hire and fund (freelance) | [`CheckoutMilestone`](screens/checkout/CheckoutMilestone.kt) | Escrow milestones; fund the first one. | [Preview](https://composedomainkit.vercel.app/screens/checkout/hire-and-fund) |
| One-page checkout | [`CheckoutOnePage`](screens/checkout/CheckoutOnePage.kt) | Address, delivery speed and payment on one screen. | [Preview](https://composedomainkit.vercel.app/screens/checkout/one-page-checkout) |
| Order tracking | [`CheckoutOrderTracking`](screens/checkout/CheckoutOrderTracking.kt) | A live map, rider contact and the order timeline. | [Preview](https://composedomainkit.vercel.app/screens/checkout/order-tracking) |
| Payment method | [`CheckoutPaymentMethod`](screens/checkout/CheckoutPaymentMethod.kt) | UPI apps first, then cards, netbanking, wallets and cash on delivery. | [Preview](https://composedomainkit.vercel.app/screens/checkout/payment-method) |
| Payment status | [`CheckoutPaymentStatus`](screens/checkout/CheckoutPaymentStatus.kt) | Waiting for approval in the UPI app, with an expiry timer. | [Preview](https://composedomainkit.vercel.app/screens/checkout/upi-pending) |
| Subscription paywall | [`CheckoutPaywall`](screens/checkout/CheckoutPaywall.kt) | Yearly against monthly, trial terms and restore purchase. | [Preview](https://composedomainkit.vercel.app/screens/checkout/subscription-paywall) |
| Order review | [`CheckoutReview`](screens/checkout/CheckoutReview.kt) | Everything editable inline before placing the order. | [Preview](https://composedomainkit.vercel.app/screens/checkout/order-review) |
| Send money (fintech) | [`CheckoutSendMoney`](screens/checkout/CheckoutSendMoney.kt) | Payee, a large amount, a note and a keypad. | [Preview](https://composedomainkit.vercel.app/screens/checkout/send-money) |
| Stepped checkout | [`CheckoutStepped`](screens/checkout/CheckoutStepped.kt) | A four-step flow; the delivery step with day and time slots. | [Preview](https://composedomainkit.vercel.app/screens/checkout/stepped-checkout) |
| Order success | [`CheckoutSuccess`](screens/checkout/CheckoutSuccess.kt) | Order ID, delivery estimate and tracking. | [Preview](https://composedomainkit.vercel.app/screens/checkout/order-success) |

</details>

<details>
<summary><b>Empty and error</b> · 12 screens</summary>

| Screen | Function | What it is | |
|---|---|---|---|
| Force update | [`BlockingForceUpdate`](screens/feedback/BlockingForceUpdate.kt) | Your version against the latest, and one button. | [Preview](https://composedomainkit.vercel.app/screens/empty/force-update) |
| Maintenance | [`BlockingMaintenance`](screens/feedback/BlockingMaintenance.kt) | Planned downtime with the expected return time. | [Preview](https://composedomainkit.vercel.app/screens/empty/maintenance) |
| All caught up | [`EmptyAllCaughtUp`](screens/feedback/EmptyAllCaughtUp.kt) | A positive empty state with no main action. | [Preview](https://composedomainkit.vercel.app/screens/empty/all-caught-up) |
| First use | [`EmptyFirstUse`](screens/feedback/EmptyFirstUse.kt) | Nothing created yet; the main action creates the first item. | [Preview](https://composedomainkit.vercel.app/screens/empty/first-use) |
| No results | [`EmptyNoResults`](screens/feedback/EmptyNoResults.kt) | Says which filter hid the results and offers the fix. | [Preview](https://composedomainkit.vercel.app/screens/empty/no-results) |
| Not found | [`ErrorNotFound`](screens/feedback/ErrorNotFound.kt) | An old or removed link, with Go to home and Search. | [Preview](https://composedomainkit.vercel.app/screens/empty/not-found) |
| Offline | [`ErrorOffline`](screens/feedback/ErrorOffline.kt) | Full screen, reloads by itself when the connection returns. | [Preview](https://composedomainkit.vercel.app/screens/empty/offline) |
| Permission needed | [`ErrorPermission`](screens/feedback/ErrorPermission.kt) | Why the permission is needed and how to turn it on. | [Preview](https://composedomainkit.vercel.app/screens/empty/permission-needed) |
| Something went wrong | [`ErrorServer`](screens/feedback/ErrorServer.kt) | Our problem, not your connection: retry and a reference code. | [Preview](https://composedomainkit.vercel.app/screens/empty/something-went-wrong) |
| Session expired | [`ErrorSessionExpired`](screens/feedback/ErrorSessionExpired.kt) | A bottom sheet over the screen; the draft is kept. | [Preview](https://composedomainkit.vercel.app/screens/empty/session-expired) |
| Offline banner | [`OfflineBanner`](screens/feedback/OfflineBanner.kt) | The banner version over a normal screen, with cached content. | [Preview](https://composedomainkit.vercel.app/screens/empty/offline-banner) |
| Inside a section | [`SectionError`](screens/feedback/SectionError.kt) | One section failed and one is empty; the rest load normally. | [Preview](https://composedomainkit.vercel.app/screens/empty/inside-a-section) |

</details>

## Feedback

- **Something broken or different from the preview?**
  [Report a problem](../../issues/new?template=screen-bug.yml) with the screen's name
  and your Compose BOM version.
- **Missing a screen?** [Request one](../../issues/new?template=screen-request.yml).
  Requests decide what gets built next.

This repo mirrors the code on the website, so fixes land here with each update rather
than through pull requests.

## Licence

[MIT](LICENSE). Use the screens in personal and commercial apps, change them however you
like, no credit required.

Android and Jetpack Compose are trademarks of Google LLC. This project is not
affiliated with Google.
