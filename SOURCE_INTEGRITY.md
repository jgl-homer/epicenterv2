# DSP source integrity

Source repository: `jgl-homer/player`

Pinned source commit: `7237dbbc46682362f0656df6f5c3839d5a67974d`

The following production DSP files were copied without package renaming, tuning changes, cleanup, formatting, or equation changes. The Git blob hashes below match the blobs in `player/main` at the pinned commit.

| File | Git blob SHA-1 |
|---|---|
| `BassAnalyzer.kt` | `851a42b49f155802e1d479312d36474ddc9ce71c` |
| `EpicenterAudioProcessor.kt` | `7e126e36197d1ba2297fbb7843bb03327caeef03` |
| `EpicenterDsp.kt` | `f3fed418920ac62dac7c3f70a8e83ae449a606bc` |
| `EpicenterPcm.kt` | `ae7c33f7ca47343d20746fae6de9e577fdaa4d57` |
| `HybridLegacyCore.kt` | `39da89522976b6c92aa8e7fe974e8846525ca175` |
| `LegacyEpicenterDsp.kt` | `5f0e7b46cf8c0558970733589cd0cf7a1645d65f` |

Verification command:

```bash
for f in app/src/main/kotlin/com/ryanheise/just_audio/*.kt; do
  printf '%s ' "$(basename "$f")"
  git hash-object "$f"
done
```
