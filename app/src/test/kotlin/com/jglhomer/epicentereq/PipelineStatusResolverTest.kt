package com.jglhomer.epicentereq

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PipelineStatusResolverTest {
    @Test
    fun emptyStateNeverClaimsAttachmentOrProcessing() {
        val status = PipelineStatusResolver.resolve(
            activePackages = emptyList(),
            sessions = emptyList(),
            backendProbe = EffectProbeResult(false, false, emptyList()),
            powerampEqInstalled = false,
        )

        assertFalse(status.playerDetected)
        assertFalse(status.sessionDetected)
        assertFalse(status.systemBackendDiscovered)
        assertFalse(status.effectAttached)
        assertFalse(status.dspProcessing)
    }

    @Test
    fun detectionStillDoesNotClaimProcessing() {
        val status = PipelineStatusResolver.resolve(
            activePackages = listOf("example.player"),
            sessions = listOf(PlayerSession(42, "example.player", 0)),
            backendProbe = EffectProbeResult(
                found = true,
                insertFound = true,
                descriptions = listOf("JGL Epicenter"),
            ),
            powerampEqInstalled = true,
        )

        assertTrue(status.playerDetected)
        assertTrue(status.sessionDetected)
        assertTrue(status.systemBackendDiscovered)
        assertTrue(status.powerampEqInstalled)
        assertFalse(status.effectAttached)
        assertFalse(status.dspProcessing)
    }
}
