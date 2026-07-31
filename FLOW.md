# Screenshot / demo capture flow

How the screenshots and `screenshots/demo.gif` in this repo were produced, so they can be regenerated.

## Prerequisites

- Android SDK + an emulator AVD, and `adb` on PATH.
- JDK 17.
- `ffmpeg` (for the GIF conversion).

## Boot the emulator

```sh
export ANDROID_HOME=$HOME/Library/Android/sdk
$ANDROID_HOME/emulator/emulator -avd <YourAVD> -no-snapshot-save -no-boot-anim &
adb wait-for-device
```

## Build + install

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## How it works

`MainActivity` reads an optional `screen` intent extra so each screen can be launched
deterministically for capture (no UI driving needed):

```sh
# screen = new (default) | chat (seeded streaming) | history | settings
adb shell am start -S -n com.brokerbot.chat/.ui.MainActivity --es screen chat
adb shell 'sleep 2.5'
adb exec-out screencap -p > screenshots/02-chat-streaming.png
```

The demo build uses `FakeStreamClient`, which streams a canned reply word-by-word, so the
typing indicator, live token append, and Stop button all animate with no backend.

## Demo GIF

Record while driving the app (tap a suggestion chip -> stream -> browse tabs), then convert:

```sh
adb shell 'screenrecord --time-limit 20 --bit-rate 5000000 /sdcard/demo.mp4' &
adb shell input tap 540 1520   # "Explain SSE streaming" chip
# ... tap History / Settings / Chat tabs ...
adb pull /sdcard/demo.mp4 demo.mp4

ffmpeg -y -i demo.mp4 -vf "fps=12,scale=300:-1:flags=lanczos,palettegen=stats_mode=diff" pal.png
ffmpeg -y -i demo.mp4 -i pal.png \
  -lavfi "fps=12,scale=300:-1:flags=lanczos[x];[x][1:v]paletteuse=dither=bayer:bayer_scale=3" \
  screenshots/demo.gif
```
