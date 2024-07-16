package com.appdev.printitout.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import com.appdev.printitout.ModelClasses.LogObject
import com.appdev.printitout.ServiceModule.ReprintService
import com.appdev.printitout.ServiceModule.ReprintService.Companion.isRunning
import com.appdev.printitout.ViewModel.MyViewModel
import com.appdev.printitout.navigation.Routes
import com.appdev.printitout.utils.Utils
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.random.nextInt

@Composable
fun FirstScreen(viewModel: MyViewModel, navController: NavHostController) {
    val context = LocalContext.current
    var taskObject by remember {
        mutableStateOf(LogObject(1000L, "Waiting...", ""))
    }
    var listOfLogs by remember {
        mutableStateOf(listOf<LogObject>())
    }
    var loading by remember {
        mutableStateOf(false)
    }

    val ordersData by viewModel.ordersState.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(key1 = ordersData) {
        if (loading) {
            if (ordersData.isNotEmpty()) {
                val logItems = mutableListOf<LogObject>()
                var time = convertMillisToFormattedTime(System.currentTimeMillis())
                ordersData.forEach { order ->
                    logItems.add(
                        LogObject(
                            System.currentTimeMillis() + Random.nextInt(1500000 + 1),
                            "Order ${order.ordine.id} found",
                            time
                        )
                    )
                }
                listOfLogs = listOfLogs.toMutableList().apply {
                    addAll(logItems)
                }
                if(Utils(context).getPrinter()!=null){
                    Utils(context).getPrinter()?.let { bd ->
                        if (printerIsActive(bd)) {
                            printText(bd, ordersData) {
                                if (it.trim().isEmpty()) {
                                    time = convertMillisToFormattedTime(System.currentTimeMillis())
                                    val printList = mutableListOf<LogObject>()
                                    ordersData.forEach { order ->
                                        printList.add(
                                            LogObject(
                                                System.currentTimeMillis() + Random.nextInt(1000000 + 1),
                                                "Order ${order.ordine.id} Printed",
                                                time
                                            )
                                        )
                                    }
                                    listOfLogs = listOfLogs.toMutableList().apply {
                                        addAll(printList)
                                    }
                                }
                            }
                        }
                    }
                } else{
                    taskObject = LogObject(
                        System.currentTimeMillis() + Random.nextInt(200000),
                        "Please select the printer",
                        ""
                    )
                }
            }
        }
        loading = false
        viewModel.updateOrders()
    }
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val taskRec = intent.getParcelableExtra<LogObject>("logO")
                val ordersLog = intent.getParcelableArrayListExtra<LogObject>("logList")
                taskRec?.let { obj ->
                    taskObject = obj
                }
                ordersLog?.let { aList ->
                    if (aList.isNotEmpty()) {
                        listOfLogs = listOfLogs.toMutableList().apply {
                            addAll(aList)
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(
            receiver,
            IntentFilter("countdown-tick")
        )

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(key1 = taskObject) {
        if (!listOfLogs.contains(taskObject)) {
            listOfLogs = listOfLogs.toMutableList().apply {
                add(taskObject)
            }
        }
    }


    Scaffold { pv ->
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .background(Color.White)
                .padding(pv)
                .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Dialog(onDismissRequest = { /*TODO*/ }) {
                            CircularProgressIndicator(color = Color.LightGray)
                        }
                    }
                }
            }
            OutlinedCard {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CustomButton(
                        onClick = { navController.navigate(Routes.MainScr.route) },
                        text = "SETTINGS"
                    )
                    CustomButton(onClick = {
                        if(Utils(context).getPrinter()!=null){
                            Utils(context).getPrinter()?.let { bluetoothDevice ->
                                if (printerIsActive(bluetoothDevice)) {
                                    if (isRunning) {
                                        sendToStopService(context, Intent("stopService"))
                                    }
                                    val serviceIntent =
                                        Intent(context, ReprintService::class.java).apply {
                                            putExtra("connectedDevice", bluetoothDevice)
                                            putExtra("Apikey", Utils(context).getKey())
                                            putExtra("Codice", Utils(context).getCodice())
                                        }
                                    ContextCompat.startForegroundService(context, serviceIntent)
                                } else {
                                    taskObject = LogObject(
                                        System.currentTimeMillis() + Random.nextInt(200000),
                                        "Printer is not active try to re-select thr printer",
                                        ""
                                    )
                                }
                            }
                        } else{
                            taskObject = LogObject(
                                System.currentTimeMillis() + Random.nextInt(200000),
                                "Please select the printer",
                                ""
                            )
                        }

                    }, text = "START PRINT SERVICE")
                    CustomButton(onClick = {
                        loading = true
                        taskObject = LogObject(
                            System.currentTimeMillis() + Random.nextInt(100000),
                            "Retrieving orders",
                            convertMillisToFormattedTime(System.currentTimeMillis())
                        )
                        sendToStopService(context, Intent("stopService"))
                        viewModel.fetchOrders(
                            Utils(context).getKey(),
                            Utils(context).getCodice()
                        )
                    }, text = "MANUAL ORDER RETRIEVAL")
                }
            }
            OutlinedCard(modifier = Modifier.padding(bottom = 30.dp)) {
                LazyColumn(Modifier.padding(vertical = 10.dp)) {
                    items(listOfLogs, key = { it.id }) { log ->
                        Row(
                            modifier = Modifier.animateItem()
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (log.task.length > 25
                                ) log.task.take(25) + ("...") else log.task,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            Text(
                                text = log.time,
                                color = Color.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun sendToStopService(context: Context, intent: Intent) {
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
}
