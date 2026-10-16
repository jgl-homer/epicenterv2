package com.jglhomer.epicentereq

import android.content.Context

data class CompanionEqStatus(
    val powerampEqInstalled: Boolean,
)

object CompanionEqProbe {
    const val POWERAMP_EQ_PACKAGE = "com.maxmpz.equalizer"

    fun scan(context: Context): CompanionEqStatus {
        val installed = runCatching {
            context.packageManager.getApplicationInfo(POWERAMP_EQ_PACKAGE, 0)
            true
        }.getOrDefault(false)
        return CompanionEqStatus(powerampEqInstalled = installed)
    }
}
