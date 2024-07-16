package com.appdev.printitout.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.appdev.printitout.ModelClasses.Order
import com.appdev.printitout.ServiceModule.ReprintService
import com.appdev.printitout.ViewModel.MyViewModel
import com.appdev.printitout.utils.Utils
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDevicesList(viewModel: MyViewModel, navController: NavHostController) {
    val pairedPrinters by remember { mutableStateOf(getPairedPrinters()) }
    var showMsg by remember {
        mutableStateOf(Pair(false, ""))
    }

    var loading by remember {
        mutableStateOf(false)
    }
    var selectedPrinter by remember {
        mutableStateOf<BluetoothDevice?>(null)
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = showMsg) {
        if (showMsg.first && showMsg.second.trim().isNotEmpty()) {
            Toast.makeText(
                context,
                showMsg.second,
                Toast.LENGTH_SHORT
            ).show()
            showMsg = Pair(false, "")
        }
    }



    LaunchedEffect(key1 = selectedPrinter) {
        if (loading) {
            loading = false
            selectedPrinter?.let { bd ->
                val serviceIntent = Intent(context, ReprintService::class.java).apply {
                    putExtra("connectedDevice", bd)
                    putExtra("Apikey", Utils(context).getKey())
                    putExtra("Codice", Utils(context).getCodice())
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }

//            if (ordersData.isEmpty()) {
//                showMsg = Pair(true, "Something is wrong either Api key or codice Reparto")
//            } else {
//                    printText(bd, ordersData) {
//
//                        showMsg = Pair(true, it)
//                    }
//                }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "Scanned Devices", color = Color.Black, fontSize = 20.sp) },
            navigationIcon = {
                Card(
                    onClick = {
                        navController.navigateUp()
                    },
                    border = BorderStroke(1.dp, Color.Black),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.size(25.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = Color.Black
                        )
                    }
                }
            })
    }) { pv ->
        Box(
            modifier = Modifier
                .padding(pv)
                .fillMaxSize()
        ) {
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
            if (pairedPrinters.isEmpty()) {
                Text(
                    text = "No Bluetooth printers found.",
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            } else {

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pairedPrinters, key = { it }) { printer ->
                        OutlinedCard(
                            onClick = {
                                loading = true
                                selectedPrinter = printer
                                Utils(context).savePrinter(printer)
//                                task = "Retrieving orders"
//                                time = convertMillisToFormattedTime(System.currentTimeMillis())
//                                viewModel.fetchOrders(
//                                    Utils(context).getKey(),
//                                    Utils(context).getCodice()
//                                )
                            },
                            modifier = Modifier
                                .height(62.dp).animateItem()
                                .padding(horizontal = 10.dp),
                            shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text =  if ((printer.name
                                            ?: printer.address).length > 20
                                    ) (printer.name
                                        ?: printer.address).take(20) + ("...") else (printer.name
                                        ?: printer.address),
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.Black,
                                    fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                                )

                            }
                        }

                    }
                }
            }
        }
    }
}

fun printText(
    printerDevice: BluetoothDevice,
    orders: List<Order>,
    operationResult: (String) -> Unit
) {
    try {
        val printerConnection = getPrinterConnection(printerDevice)
        val printer = EscPosPrinter(printerConnection, 203, 48f, 32)
        val formattedText = buildString {
            orders.forEachIndexed { index, order ->
                if (index > 0) {
                    append("[L]\n[L]\n[L]\n[L]\n") // Add spacing between orders
                }

                append("[L]<b>#${order.ordine.id + " - " + order.ordine.data} </b>\n")
                append("[C]<font size='big'>${order.ordine.nome}</font>\n")
                append("[L]<b>PIETANZA</b>[R]<b>Q.TA</b>[R]<b>VAR.</b>\n")


                order.elementi.forEach { elemento ->
                    if (elemento.api_printer_variazioni_list.isNotEmpty()) {
                        append("[L]-${elemento.nome_prodotto}[R]n.${elemento.qta}[R]1\n")
                    } else {
                        append("[L]-${elemento.nome_prodotto}[R]n.${elemento.qta}[R]\n")
                    }

                    // Check for variations
                    if (elemento.api_printer_variazioni_list.isNotEmpty()) {
                        elemento.api_printer_variazioni_list.forEach { variazione ->
                            append("[L]${variazione}\n")
                        }
                    }
                    if (elemento.api_printer_note.trim().isNotEmpty()) {
                        append("[L]N.B.(${elemento.api_printer_note})\n")
                        append("[L]\n")
                    }
                }
            }
        }
        printer.printFormattedText(formattedText)
        operationResult("")
    } catch (e: Exception) {
        e.localizedMessage?.let { operationResult(it) }
    }
}

fun convertMillisToFormattedTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun getPrinterConnection(device: BluetoothDevice): BluetoothConnection? {
    val theList = BluetoothPrintersConnections().list
    return theList?.firstOrNull { listConnectionDevice ->
        listConnectionDevice.device.address == device.address
    }
}

fun printerIsActive(device: BluetoothDevice): Boolean {
    val theList = BluetoothPrintersConnections().list
    return theList?.any { listConnectionDevice ->
        listConnectionDevice.device.address == device.address
    } ?: false
}

fun getPairedPrinters(): List<BluetoothDevice> {
    val printersConnections = BluetoothPrintersConnections()
    val pairedPrinters = printersConnections.list
    return pairedPrinters?.map { it.device } ?: emptyList()
}