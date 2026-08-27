# Call Blocker

A small Android 12+ app that rejects incoming calls unless the number is in a local whitelist. It uses Android's `CallScreeningService` and stores numbers only in on-device `SharedPreferences`.

## Build

Install Android Studio or the Android command-line tools with Android SDK 37, then run:

```sh
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Install

Enable USB debugging on the phone and install the APK with:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

You can also transfer the APK to the phone and open it there. Android may ask you to allow installation from that source.

## Set Up

1. Open Call Blocker. On first launch, Android asks whether it may become the Call Screening app.
2. Approve the request. If it was dismissed, tap **Make active** in the app.
3. Grant contacts access when requested. Android does not send calls from saved contacts to a screening app without this permission, so it is needed to enforce the local whitelist for those callers too.
4. Add every number that should be allowed to ring. Numbers are normalized before storage and matched using Android's country-aware phone-number API, so formatting differences such as spaces, hyphens, parentheses, or a national number versus `+49...` are handled.

On OxygenOS 12, the role can also be checked under **Settings > Apps > Default apps > Caller ID & spam app**. The exact label can vary by OxygenOS release.

## Verify Blocking

1. Add one test caller to the whitelist.
2. Call the phone from that number and confirm that it rings.
3. Remove the number, call again, and confirm that Android rejects the call.
4. Call from a different number and confirm that it is rejected.
5. Confirm the app still says **Status: active Call Screening app** after restarting the phone.

Blocked calls remain visible in Android's call log and produce the normal blocked-call notification where the device supports it.

## Android Limitation

The app rejects calls with a missing or unusable number whenever Android delivers them to the service. However, Android's public `CallScreeningService` API does not deliver calls marked restricted, unknown, unavailable, or payphone to third-party screening services. Consequently, no app using only this supported API can guarantee rejection of withheld/private calls on every Android 12 device. OxygenOS's built-in blocked-number settings must be used for that platform-controlled case.

## Privacy

The app has no internet permission, analytics, ads, accounts, cloud synchronization, accessibility service, call-history feature, or SMS handling. Contacts access is used only to make Android submit calls from saved contacts to the screening service; the app never reads or stores the contacts database.
