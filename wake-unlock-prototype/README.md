# Wake & Unlock Test — Android prototype

A diagnostic live wallpaper, not a finished anime wallpaper app. It renders a golden wake reveal and a blue home transition using Android's live wallpaper service. It relies on onVisibilityChanged, dynamic screen-on/off broadcasts, keyguard state and USER_PRESENT; event timing and picker options vary across phones, and an unlock transition may start after the keyguard has disappeared. It does not implement exact swipe-progress synchronization. No permission to replace or overlay the system lock screen is requested.

## Build in Android Studio
1. Install Android Studio and Android SDK 35, and JDK 17.
2. Open this directory as an Android project and allow Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s), or run the app on a connected Android phone.
4. In the app, tap Preview and apply wallpaper. If the system picker allows, select both the home and lock screens.
5. Lock, wake, and unlock with fingerprint, face and swipe; note whether gold reveal and blue transition appear at the intended time. Test with always-on display on/off.

The project depends on the Android Gradle plugin 8.7.3 and Gradle-compatible JDK 17; internet access is needed to download build dependencies. It was not compiled or tested on a phone in the creation environment because Android SDK and Gradle were unavailable. The user-supplied ads are not included.
