package com.appdev.printitout

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.appdev.printitout.utils.ObjectsGlobal.Companion.CHANNEL_ID

class applicationClass:Application() {

    override fun onCreate() {
        super.onCreate()
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Reprint Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}