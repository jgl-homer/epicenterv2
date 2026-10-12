package com.jglhomer.epicentereq

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class PlayerSession(
    val sessionId: Int,
    val packageName: String,
    val contentType: Int,
    val openedAtMs: Long = System.currentTimeMillis(),
)

object SessionStore {
    private val sessions = ConcurrentHashMap<Int, PlayerSession>()
    private val activeMediaPackages = ConcurrentHashMap.newKeySet<String>()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun open(session: PlayerSession) {
        sessions[session.sessionId] = session
        notifyChanged()
    }

    fun close(sessionId: Int) {
        sessions.remove(sessionId)
        notifyChanged()
    }

    fun setActiveMediaPackages(packages: Collection<String>) {
        activeMediaPackages.clear()
        activeMediaPackages.addAll(packages)
        notifyChanged()
    }

    fun sessions(): List<PlayerSession> = sessions.values.sortedByDescending { it.openedAtMs }
    fun activePackages(): List<String> = activeMediaPackages.sorted()

    fun addListener(listener: () -> Unit) { listeners += listener }
    fun removeListener(listener: () -> Unit) { listeners -= listener }

    private fun notifyChanged() = listeners.forEach { runCatching { it() } }
}
