package com.vpnclient.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.vpnclient.app.data.local.AssetManagerHelper
import com.vpnclient.app.data.local.PreferenceManager

class VpnApplication : Application() {

    lateinit var preferenceManager: PreferenceManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceManager = PreferenceManager(this)

        createNotificationChannel()

        // 1. Initialize Gomobile GoSeq context safely
        try {
            val seqClass = Class.forName("go.Seq")
            try {
                val setContextMethod = seqClass.getMethod("setContext", Any::class.java)
                setContextMethod.invoke(null, this)
            } catch (ignored: NoSuchMethodException) {
                val setContextMethod = seqClass.getMethod("setContext", android.content.Context::class.java)
                setContextMethod.invoke(null, this)
            }
            Log.i("VpnApplication", "go.Seq context initialized successfully")
        } catch (t: Throwable) {
            Log.w("VpnApplication", "go.Seq init notice: ${t.message}")
        }

        // 2. Extract geoip.dat and geosite.dat for Xray routing engine in background
        Thread {
            AssetManagerHelper.copyGeoAssetsIfNeeded(this)
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "VPN Service Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live VPN connection status"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "vpn_service_channel"
        lateinit var instance: VpnApplication
            private set
    }
}
