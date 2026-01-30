package com.example.mptaskandroid2025

import android.content.Context
import com.mixpanel.android.sessionreplay.MPSessionReplay
import com.mixpanel.android.sessionreplay.models.MPSessionReplayConfig
import com.mixpanel.android.sessionreplay.sensitive_views.AutoMaskedView
import java.util.Collections.emptySet

class SRManager {
    var MPToken: String = "f8bd7cddaf94642530004c3d0509691f"

    fun initialize(mContext: Context, userId: String){
        val config = MPSessionReplayConfig()
        config.autoStartRecording = true
        config.wifiOnly = false
        config.flushInterval = 10
        config.recordingSessionsPercent = 100.0
        config.enableLogging = true
        val emptySet = emptySet<AutoMaskedView>()
        config.autoMaskedViews = emptySet
        MPSessionReplay.initialize(mContext, MPToken,userId, config)
    }

    fun changeIdentity(newId: String){
        MPSessionReplay.getInstance()?.identify(newId);
    }

}