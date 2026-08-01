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
# screen = new (default) | chat (seeded streaming) | history | sync | settings
adb shell am start -S -n com.brokerbot.chat/.ui.MainActivity --es screen chat
adb shell 'sleep 2.5'
adb exec-out screencap -p > screenshots/02-chat-streaming.png
```

The demo build uses `FakeStreamClient` (chat) and `FakeSyncClient` (sync), which reproduce
the streaming tokens and the discover -> connect -> heartbeat -> drop -> reconnect lifecycle
with no backend and no second device.

For the sync screenshots, grant the notification permission first (foreground service),
launch the sync tab, and capture the connected state early and the reconnecting state
(the fake drops ~11s after connecting):

```sh
adb shell pm grant com.brokerbot.chat android.permission.POST_NOTIFICATIONS
adb shell am start -S -n com.brokerbot.chat/.ui.MainActivity --es screen sync
adb shell 'sleep 4';  adb exec-out screencap -p > screenshots/05-sync-connected.png
adb shell 'sleep 8';  adb exec-out screencap -p > screenshots/06-sync-reconnecting.png
```

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
