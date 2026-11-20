# Appium UI/UX tests

The suite automates eight contextual user tasks. It checks that the app makes the key paths discoverable and that a completed action produces the expected UI state. It does not replace moderated usability research: observations, completion time, and qualitative feedback still need real participants.

## Covered tasks

1. Orient on the first screen and find the primary navigation.
2. Find expenses and income.
3. Reach financial accounts.
4. Open analytics from the top bar.
5. Change a theme and verify the choice remains selected.
6. Choose and apply an operation date.
7. Cancel a pending operation-date change.
8. Set and confirm a PIN code.

## Prerequisites

- Node.js 20 or newer;
- Android SDK with a running emulator or a connected device;
- a debug APK at `app/build/outputs/apk/debug/app-debug.apk`, or its path in `FINANCE_APP_PATH`;
- an Appium 2 server with the UiAutomator2 driver installed.

The test configuration resets app data at the beginning of a session. The operation-date cases require the transaction editor to load its categories and accounts, so use a build configured with a valid API token and a reachable backend.

## Run

```bash
cd appium
npm install
npx appium driver install uiautomator2
npx appium
```

In a second terminal:

```bash
cd appium
npm run test:android
```

When the APK is elsewhere or the server uses another port, set `FINANCE_APP_PATH`, `APPIUM_HOST`, or `APPIUM_PORT` before running the command.

## Appium selectors

The application maps Compose `testTag` to Android `resource-id` once on the root UI container. Appium selectors therefore use the tag itself through UiAutomator, rather than display text that changes with the selected app language.
