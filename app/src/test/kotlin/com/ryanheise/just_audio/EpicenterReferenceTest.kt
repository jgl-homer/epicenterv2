package com.ryanheise.just_audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import org.junit.Assert.assertTrue
import org.junit.Test

class EpicenterReferenceTest {
    @Test
    fun missingFundamentalFixtureExercisesEveryEngine() {
        EpicenterEngine.entries.forEach { engine ->
            val result = runFixture(engine)
            assertTrue("$engine produced non-finite output", result.output.all { it.isFinite() })
            assertTrue("$engine did not alter the enabled fixture", result.maxDelta > 1e-4f)

            if (engine != EpicenterEngine.LEGACY) {
                assertTrue(
                    "$engine detected unexpected f0 ${result.detectedF0}",
                    abs(result.detectedF0 - 36f) < 2f,
                )
                assertTrue(
                    "$engine confidence too low ${result.confidence}",
                    result.confidence > 0.5f,
                )
            }
        }
    }

    private fun runFixture(engine: EpicenterEngine): FixtureResult {
        val rate = 48_000
        val frames = (rate * 0.35).toInt()
        val input = FloatArray(frames * 2)
        val output = FloatArray(input.size)

        for (frame in 0 until frames) {
            val t = frame.toDouble() / rate
            val x = (
                0.16 * sin(2 * PI * 72 * t) +
                    0.12 * sin(2 * PI * 108 * t) +
                    0.08 * sin(2 * PI * 144 * t)
                ).toFloat()
            input[frame * 2] = x
            input[frame * 2 + 1] = x
        }

        val params = EpicenterParams()
        val dsp = EpicenterDsp(rate, engine, debugEnabled = true)
        dsp.prepare(2, params)
        dsp.processInterleaved(input, output, 2, params, enabled = true)

        var delta = 0f
        for (i in input.indices) delta = max(delta, abs(output[i] - input[i]))

        return FixtureResult(
            output = output,
            maxDelta = delta,
            detectedF0 = dsp.debugState.detectedF0,
            confidence = dsp.debugState.confidence,
        )
    }

    private data class FixtureResult(
        val output: FloatArray,
        val maxDelta: Float,
        val detectedF0: Float,
        val confidence: Float,
    )
}
