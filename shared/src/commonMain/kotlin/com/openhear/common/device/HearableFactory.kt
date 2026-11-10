package com.openhear.common.device

import com.juul.kable.Advertisement

object HearableFactory {

    /**
     * Creates a concrete [BaseHearable] driver based on the device name or BLE advertisement.
     */
    fun create(
        name: String,
        advertisement: Advertisement? = null
    ): BaseHearable {
        val normalizedName = name.lowercase()
        return when {
            normalizedName.contains("800") || normalizedName.contains("airdopes") -> {
                Airdopes800Hearable(deviceName = name).also {
                    if (advertisement != null) {
                        it.cachedAdvertisement = advertisement
                    }
                }
            }
            else -> {
                // Default to Airdopes800Hearable driver as the primary supported hardware profile
                Airdopes800Hearable(deviceName = name).also {
                    if (advertisement != null) {
                        it.cachedAdvertisement = advertisement
                    }
                }
            }
        }
    }
}
