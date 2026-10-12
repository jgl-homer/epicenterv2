package com.jglhomer.epicentereq

import android.media.audiofx.AudioEffect

data class EffectProbeResult(val found: Boolean, val descriptions: List<String>)

object EffectProbe {
    fun scan(): EffectProbeResult {
        val effects = runCatching { AudioEffect.queryEffects().orEmpty().toList() }.getOrDefault(emptyList())
        val hits = effects.filter {
            it.name.contains("epicenter", ignoreCase = true) ||
                it.name.contains("jgl", ignoreCase = true) ||
                it.implementor.contains("jgl", ignoreCase = true)
        }
        return EffectProbeResult(
            found = hits.isNotEmpty(),
            descriptions = hits.map { "${it.name} · ${it.implementor} · ${it.uuid}" },
        )
    }
}
