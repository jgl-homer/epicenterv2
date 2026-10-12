package com.jglhomer.epicentereq

import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService

class PlayerNotificationListenerService : NotificationListenerService() {
    private lateinit var mediaSessionManager: MediaSessionManager
    private val component by lazy { ComponentName(this, PlayerNotificationListenerService::class.java) }
    private val listener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        update(controllers.orEmpty())
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        mediaSessionManager = getSystemService(MediaSessionManager::class.java)
        mediaSessionManager.addOnActiveSessionsChangedListener(listener, component)
        runCatching { update(mediaSessionManager.getActiveSessions(component)) }
    }

    override fun onListenerDisconnected() {
        if (::mediaSessionManager.isInitialized) {
            runCatching { mediaSessionManager.removeOnActiveSessionsChangedListener(listener) }
        }
        SessionStore.setActiveMediaPackages(emptyList())
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        if (::mediaSessionManager.isInitialized) {
            runCatching { mediaSessionManager.removeOnActiveSessionsChangedListener(listener) }
        }
        super.onDestroy()
    }

    private fun update(controllers: List<MediaController>) {
        SessionStore.setActiveMediaPackages(controllers.map { it.packageName }.distinct())
    }
}
