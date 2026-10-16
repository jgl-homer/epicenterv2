package com.jglhomer.epicentereq

data class PipelineStatus(
    val playerDetected: Boolean,
    val sessionDetected: Boolean,
    val systemBackendDiscovered: Boolean,
    val effectAttached: Boolean,
    val dspProcessing: Boolean,
    val powerampEqInstalled: Boolean,
    val powerampEqAttachedKnown: Boolean = false,
    val powerampEqAttached: Boolean = false,
)

object PipelineStatusResolver {
    fun resolve(
        activePackages: Collection<String>,
        sessions: Collection<PlayerSession>,
        backendProbe: EffectProbeResult,
        powerampEqInstalled: Boolean,
    ): PipelineStatus {
        return PipelineStatus(
            playerDetected = activePackages.isNotEmpty() || sessions.any { it.packageName.isNotBlank() },
            sessionDetected = sessions.isNotEmpty(),
            systemBackendDiscovered = backendProbe.insertFound,
            effectAttached = false,
            dspProcessing = false,
            powerampEqInstalled = powerampEqInstalled,
        )
    }
}
