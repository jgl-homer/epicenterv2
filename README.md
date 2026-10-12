# JGL Epicenter EQ

Android experiment to turn the Epicenter engine from `jgl-homer/player` into a dedicated effect-control app with player/session tracking similar in concept to standalone equalizer apps.

## Important: the DSP is not a rewrite

The complete current production motor was copied **1:1** from `player` at commit:

`7237dbbc46682362f0656df6f5c3839d5a67974d`

Included unchanged:

- HYBRID
- SMART
- LEGACY
- `BassAnalyzer` / `BassPitchTracker`
- `HybridLegacyCore`
- PCM16 / PCM24 / PCM32 / Float conversion
- Media3 `EpicenterAudioProcessor`
- Sweep / Width / Intensity / Volume
- Peak protection / linked limiting
- Existing bypass and engine-switch behavior

See [`SOURCE_INTEGRITY.md`](SOURCE_INTEGRITY.md) for the exact Git blob hashes.

## What the app already implements

- Receives `ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION` and `ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION`.
- Records package name, Android audio-session ID and content type when a compatible player announces them.
- Uses Notification Listener + `MediaSessionManager` to identify active media-player packages.
- Handles `ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL` so compatible players can open the control UI.
- Exposes HYBRID / SMART / LEGACY and the same user controls as the copied motor.
- Includes a local deterministic DSP self-test from the app UI.
- Scans Android's installed audio effects and reports whether a JGL/Epicenter system effect backend exists.

## Current Android limitation

The copied Kotlin `EpicenterDsp` can process PCM when it is inside a playback pipeline (as it does in `player`). A normal third-party APK does **not** receive another application's PCM merely because it knows that application's audio-session ID.

Therefore this repository deliberately separates two pieces:

```text
Player/session tracking + UI
             |
             v
System effect backend  <---- required for external apps
             |
             v
Exact Epicenter DSP
```

At the current stage, player/session recognition works and the exact DSP runs locally/self-test, but external Spotify/YouTube Music/etc. audio is not falsely claimed to be processed unless a compatible insert effect backend is installed in Android's audio framework.

The next backend step is system/root integration. It must preserve the DSP behavior represented by the pinned Kotlin core rather than replacing it with Android `BassBoost` or an approximate EQ.

## JVM verification performed

The unchanged pure Kotlin engine files compile and execute outside Flutter. A 48 kHz stereo missing-fundamental fixture was run through all three engines:

```text
HYBRID delta=0.23302759 f0=36.001846 confidence=0.99845505
SMART  delta=0.21877652 f0=36.001846 confidence=0.9955862
LEGACY delta=0.20866635 f0=0.0 confidence=0.0
```

This is a sanity check of the transplanted code, not an Android device listening test.

## Project layout

```text
app/src/main/kotlin/
├── com/ryanheise/just_audio/       # copied 1:1 from player
│   ├── BassAnalyzer.kt
│   ├── EpicenterAudioProcessor.kt
│   ├── EpicenterDsp.kt
│   ├── EpicenterPcm.kt
│   ├── HybridLegacyCore.kt
│   └── LegacyEpicenterDsp.kt
└── com/jglhomer/epicentereq/       # new standalone app shell
    ├── AudioEffectSessionReceiver.kt
    ├── EffectProbe.kt
    ├── MainActivity.kt
    ├── PlayerNotificationListenerService.kt
    ├── SessionStore.kt
    └── SimpleItemSelectedListener.kt
```

## Build

The project uses AGP 8.9.1, Kotlin 2.1.0, compile/target SDK 35 and Media3 1.4.1, matching the relevant generation of the source project.

A Gradle wrapper binary is intentionally not fabricated in this generated copy. Create/restore the wrapper with a compatible Gradle installation, then build normally:

```bash
gradle wrapper --gradle-version 8.11.1
./gradlew assembleDebug
```
