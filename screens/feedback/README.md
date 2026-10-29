# Empty and error screens

Empty lists, offline, errors, maintenance and force update. 12 files, package `com.yourapp.ui.screens.feedback`.

| Screen | Function | What it is | |
|---|---|---|---|
| Force update | [`BlockingForceUpdate`](BlockingForceUpdate.kt) | Your version against the latest, and one button. | [Preview](https://composedomainkit.vercel.app/screens/empty/force-update) |
| Maintenance | [`BlockingMaintenance`](BlockingMaintenance.kt) | Planned downtime with the expected return time. | [Preview](https://composedomainkit.vercel.app/screens/empty/maintenance) |
| All caught up | [`EmptyAllCaughtUp`](EmptyAllCaughtUp.kt) | A positive empty state with no main action. | [Preview](https://composedomainkit.vercel.app/screens/empty/all-caught-up) |
| First use | [`EmptyFirstUse`](EmptyFirstUse.kt) | Nothing created yet; the main action creates the first item. | [Preview](https://composedomainkit.vercel.app/screens/empty/first-use) |
| No results | [`EmptyNoResults`](EmptyNoResults.kt) | Says which filter hid the results and offers the fix. | [Preview](https://composedomainkit.vercel.app/screens/empty/no-results) |
| Not found | [`ErrorNotFound`](ErrorNotFound.kt) | An old or removed link, with Go to home and Search. | [Preview](https://composedomainkit.vercel.app/screens/empty/not-found) |
| Offline | [`ErrorOffline`](ErrorOffline.kt) | Full screen, reloads by itself when the connection returns. | [Preview](https://composedomainkit.vercel.app/screens/empty/offline) |
| Permission needed | [`ErrorPermission`](ErrorPermission.kt) | Why the permission is needed and how to turn it on. | [Preview](https://composedomainkit.vercel.app/screens/empty/permission-needed) |
| Something went wrong | [`ErrorServer`](ErrorServer.kt) | Our problem, not your connection: retry and a reference code. | [Preview](https://composedomainkit.vercel.app/screens/empty/something-went-wrong) |
| Session expired | [`ErrorSessionExpired`](ErrorSessionExpired.kt) | A bottom sheet over the screen; the draft is kept. | [Preview](https://composedomainkit.vercel.app/screens/empty/session-expired) |
| Offline banner | [`OfflineBanner`](OfflineBanner.kt) | The banner version over a normal screen, with cached content. | [Preview](https://composedomainkit.vercel.app/screens/empty/offline-banner) |
| Inside a section | [`SectionError`](SectionError.kt) | One section failed and one is empty; the rest load normally. | [Preview](https://composedomainkit.vercel.app/screens/empty/inside-a-section) |

[← All categories](../../README.md#screens)
