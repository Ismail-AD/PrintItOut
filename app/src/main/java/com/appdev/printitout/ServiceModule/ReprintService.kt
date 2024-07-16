package com.appdev.printitout.ServiceModule

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appdev.printitout.ModelClasses.LogObject
import com.appdev.printitout.R
import com.appdev.printitout.Repository.Repository
import com.appdev.printitout.ui.screens.convertMillisToFormattedTime
import com.appdev.printitout.ui.screens.printText
import com.appdev.printitout.ui.screens.printerIsActive
import com.appdev.printitout.utils.ObjectsGlobal.Companion.CHANNEL_ID
import com.appdev.printitout.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class ReprintService : Service() {
    val repository = Repository()
    var startIdOfService = 0
    lateinit var alarmManager: AlarmManager
    var bluetoothDevice: BluetoothDevice? = null
    var apiKey: String? = null
    var codice: String? = null
    companion object {
        @Volatile
        var isRunning = false
            private set
    }

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "stopService") {
                stopForeground(true)
                if (startIdOfService != 0) {
                    stopSelf(startIdOfService)
                } else {
                    stopSelf()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(stopReceiver, IntentFilter("stopService"))
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startIdOfService = startId
        bluetoothDevice = intent?.getParcelableExtra("connectedDevice")
        apiKey = intent?.getStringExtra("Apikey")
        codice = intent?.getStringExtra("Codice")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notify)
            .setContentTitle("PrintItOut")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText("Service has been started to print the data after a minute")
            .build()
        startForeground(startId, notification)
        if (bluetoothDevice != null && !printerIsActive(bluetoothDevice!!)) {
            broadcastRemainingTime(
                LogObject(
                    System.currentTimeMillis(),
                    "Printer is not active",
                    ""
                )
            )
        } else if (bluetoothDevice != null && apiKey != null && codice != null) {
            CoroutineScope(Dispatchers.IO).launch {
                broadcastRemainingTime(
                    LogObject(
                        System.currentTimeMillis(),
                        "Retrieving orders",
                        convertMillisToFormattedTime(System.currentTimeMillis())
                    )
                )
                repository.getOrdersFlow(apiKey!!, codice!!).collect { orders ->
                    if (orders.isNotEmpty()) {
                        var time = convertMillisToFormattedTime(System.currentTimeMillis())
                        val logItems = mutableListOf<LogObject>()
                        orders.forEach { order ->
                            logItems.add(
                                LogObject(
                                    System.currentTimeMillis() + Random.nextInt(1000000 + 1),
                                    "Order ${order.ordine.id} found",
                                    time
                                )
                            )
                        }
                        broadcastRemainingTime(null, logItems)
                        printText(bluetoothDevice!!, orders) {
                            if (it.trim().isEmpty()) {
                                time = convertMillisToFormattedTime(System.currentTimeMillis())
                                val printList = mutableListOf<LogObject>()
                                orders.forEach { order ->
                                    printList.add(
                                        LogObject(
                                            System.currentTimeMillis() + Random.nextInt(1000000 + 1),
                                            "Order ${order.ordine.id} Printed",
                                            time
                                        )
                                    )
                                }
                                broadcastRemainingTime(null, printList)
                            }
                        }
                    }
                    broadcastRemainingTime(LogObject(System.currentTimeMillis(), "Waiting", " "))
                    reschedulePrint()
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stopReceiver)
    }

    private fun reschedulePrint() {
        val serviceIntent = Intent(this, ReprintService::class.java).apply {
            putExtra("connectedDevice", bluetoothDevice)
            putExtra("Apikey", apiKey)
            putExtra("Codice", codice)
        }
        val pendingIntent = PendingIntent.getService(
            applicationContext,
            System.currentTimeMillis().toInt(),
            serviceIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val interval = System.currentTimeMillis() + 60 * 1000L
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, interval, pendingIntent)
    }


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }


    private fun broadcastRemainingTime(
        logObject: LogObject? = null,
        logList: List<LogObject> = emptyList()
    ) {
        val intent = Intent("countdown-tick").apply {
            logObject?.let { logObj ->
                putExtra("logO", logObj)
            }
            if (logList.isNotEmpty()) {
                putParcelableArrayListExtra("logList", ArrayList(logList))
            }
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}