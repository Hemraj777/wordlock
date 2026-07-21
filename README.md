# WordLock 🔤

A daily vocabulary builder that shows a new English word and its Nepali meaning as a **lock screen notification** on your Android phone.

## Features
- Daily word + pronunciation + meaning (English & Nepali)
- Appears as a notification on your lock screen
- Persists after reboot
- No ads, no tracking, completely free

## Installation

1. Go to [Releases](../../releases) and download the latest `.apk`
2. Open the APK on your phone
3. Allow installation from unknown sources
4. Open WordLock → tap **"Enable Lock Screen Notification"**
5. Grant notification permission when prompted

That's it — every day a new word will appear on your lock screen.

## Build from Source

```bash
# Clone this repo
git clone https://github.com/your-username/wordlock.git
cd wordlock

# Open in Android Studio
# Build > Build Bundle(s) / APK(s) > Build APK(s)

# Or via command line
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/`

## Requirements
- Android 8.0 (API 26) or higher
- Notification permission

## License
MIT
