package com.jglhomer.epicentereq

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect

class AudioEffectSessionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
        if (sessionId < 0) return

        when (intent.action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                SessionStore.open(
                    PlayerSession(
                        sessionId = sessionId,
                        packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME).orEmpty(),
                        contentType = intent.getIntExtra(AudioEffect.EXTRA_CONTENT_TYPE, 0),
                    )
                )
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> SessionStore.close(sessionId)
        }
    }
}
