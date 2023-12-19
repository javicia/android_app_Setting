package com.example.settings.model

import android.os.storage.StorageVolume

data class SettingModel(
    val volume: Int,
    val bluetooth: Boolean,
    val vibration: Boolean,
    val darkMode: Boolean
)
