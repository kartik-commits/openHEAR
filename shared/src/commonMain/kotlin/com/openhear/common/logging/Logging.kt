package com.openhear.common.logging

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object AppLogging {
    private var initialized = false

    fun init() {
        if (!initialized) {
            Napier.base(DebugAntilog())
            initialized = true
        }
    }
}
