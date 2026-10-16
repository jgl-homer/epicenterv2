# Android backend plan

This document applies only to this repository.

## What the normal APK can do

The Android public `AudioEffect` API exposes audio-session control and enumeration of effects already provided by the Android audio framework. A player can announce an audio session with `ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION`, and an effect-control app can use the session ID when controlling a compatible framework effect.

References:

- https://developer.android.com/reference/android/media/audiofx/AudioEffect
- https://developer.android.com/reference/android/media/audiofx/AudioEffect.Descriptor

The public API does **not** turn the Kotlin `EpicenterDsp` class into an AudioFlinger insert effect by itself.

The app therefore reports these states separately:

1. Player detected
2. Audio session detected
3. System Epicenter backend discovered
4. Effect attached
5. External audio actually being processed

A detected session is never treated as proof of processing.

## Android 14+ / Android 15 backend

Modern Android uses Stable AIDL for the Audio HAL. The Effects HAL exposes an effect factory, descriptors, capabilities, parameters, and effect instances.

References:

- https://source.android.com/docs/core/audio/aidl-implement
- https://source.android.com/docs/core/audio/implement

A system/root implementation is the target path for making the JGL Epicenter a real insert effect usable by other applications.

## Poweramp Equalizer coexistence

The standalone Poweramp Equalizer package is `com.maxmpz.equalizer`. This app only reports whether that package is installed.

It does **not** currently claim that Poweramp EQ is attached to a specific session because the public APIs used here do not provide a reliable per-session effect-chain inspection API to third-party apps.

The final backend should permit both effects to coexist as separate effect implementations. The Epicenter must have its own effect identity and must not impersonate Android Equalizer or BassBoost.

## Next backend milestone

Implement a native/system backend with this contract:

```text
player audio session
       |
       v
Android audio effect chain
       |
       +--> Poweramp EQ (optional)
       |
       +--> JGL Epicenter native insert
                |
                v
          original DSP behavior
```

Before a native DSP replaces any Kotlin reference path, add deterministic fixtures and compare output for HYBRID, SMART, and LEGACY. Do not alter the existing DSP source files as part of backend plumbing.
