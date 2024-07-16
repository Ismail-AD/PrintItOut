package com.appdev.printitout.ui.screens

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.print.PrintManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.printitout.ModelClasses.Order
import com.appdev.printitout.R
import com.appdev.printitout.ViewModel.MyViewModel
import com.appdev.printitout.navigation.Routes
import com.appdev.printitout.utils.Utils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MyViewModel, controller: NavHostController) {
    val context = LocalContext.current

    var apiKeyInput by remember { mutableStateOf(Utils(context).getKey()) }
    var codiceInput by remember { mutableStateOf(Utils(context).getCodice()) }

    var loading by remember {
        mutableStateOf(false)
    }
    var showMsg by remember {
        mutableStateOf(Pair(false, ""))
    }
    val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }


    val locPermissionState = rememberMultiplePermissionsState(permissions)
    var showRationale by remember(locPermissionState) {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = locPermissionState) {
        if (locPermissionState.shouldShowRationale) {
            showRationale = true
        } else if (!locPermissionState.allPermissionsGranted) {
            locPermissionState.launchMultiplePermissionRequest()
        }
    }
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


    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "Settings", color = Color.Black, fontSize = 20.sp) },
            navigationIcon = {
                Card(
                    onClick = {
                        controller.navigateUp()
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
                .background(Color.White)
                .padding(pv)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
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
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        OutlinedCard(modifier = Modifier.padding(10.dp)) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 15.dp
                                )
                            ) {
                                Text(
                                    text = "1) inserisci o controlla che ci siano ApiKey e Codice reparto oppure leggi il QR Code con fapposito pulsante e salva le impostazioni."
                                )
                                Text(
                                    text = "Api Key",
                                    color = Color.Black,
                                    modifier = Modifier.padding(top = 15.dp)
                                )
                                Row(modifier = Modifier.height(IntrinsicSize.Max)) {
                                    OutlinedTextField(
                                        value = apiKeyInput,
                                        onValueChange = {
                                            apiKeyInput = it
                                        },
                                        textStyle = TextStyle(
                                            fontSize = 16.sp
                                        ), placeholder = {
                                            Text(
                                                text = "Api key", color = Color.Black
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 5.dp),
                                        singleLine = true
                                    )
                                }
                                Text(
                                    text = "Codice reparto",
                                    color = Color.Black,
                                    modifier = Modifier.padding(top = 15.dp)
                                )
                                Row(modifier = Modifier.height(IntrinsicSize.Max)) {
                                    OutlinedTextField(
                                        value = codiceInput,
                                        onValueChange = {
                                            codiceInput = it
                                        },
                                        textStyle = TextStyle(
                                            fontSize = 16.sp
                                        ), placeholder = {
                                            Text(
                                                text = "Codice reparto", color = Color.Black
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 5.dp),
                                        singleLine = true
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CustomButton(onClick = { /*TODO*/ }, text = "LEGGI QR")
                                    CustomButton(onClick = {
                                        Utils(context).saveKey(apiKeyInput)
                                        Utils(context).saveCodice(codiceInput)
                                        showMsg = Pair(true, "Salvataggio effettuato!")
                                    }, text = "SALVA IMPOSTAZIONI")
                                }
                            }
                        }
                        OutlinedCard(modifier = Modifier.padding(10.dp)) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 15.dp
                                )
                            ) {
                                Text(
                                    text = "2) Seleziona la stampante da usare dopo aver fatto il pairing bluetooth",
                                    modifier = Modifier.padding(
                                        top = 13.dp
                                    )
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 15.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CustomButton(onClick = {
                                        if (locPermissionState.shouldShowRationale) {
                                            showRationale = true
                                        } else if (locPermissionState.allPermissionsGranted) {
                                            controller.navigate(Routes.ScannedDevices.route)
                                        } else {
                                            locPermissionState.launchMultiplePermissionRequest()
                                        }
                                    }, text = "ASSOCIA STAMPANTE")
                                }
                            }
                        }
                        OutlinedCard(modifier = Modifier.padding(10.dp)) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 15.dp
                                )
                            ) {
                                Text(
                                    text = "3)Avvia o ferma il servizio di stampa",
                                    modifier = Modifier.padding(
                                        top = 13.dp
                                    )
                                )
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CustomButton(
                                        onClick = { /*TODO*/ },
                                        text = "AVVIA SERVIZIO DI STAMPA"
                                    )
                                    CustomButton(
                                        onClick = { /*TODO*/ },
                                        text = "STAMPA ORDINI UNA TANTUM"
                                    )
                                }
                            }
                        }
                        if (showRationale) {
                            AlertDialog(
                                onDismissRequest = {
                                    showRationale = false
                                },
                                title = {
                                    Text(
                                        text = "Allow all the permissions for app functionality to work ",
                                        color = Color.Black.copy(alpha = 0.8f)
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Enable the required settings for Permissions by the Application",
                                        color = Color.Black.copy(alpha = 0.8f)
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showRationale = false
                                            locPermissionState.launchMultiplePermissionRequest()
                                        },
                                    ) {
                                        Text("Continue", color = Color.Black.copy(alpha = 0.8f))
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            showRationale = false
                                        },
                                    ) {
                                        Text("Dismiss", color = Color.Black.copy(alpha = 0.8f))
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomButton(onClick: () -> Unit, text: String) {
    Card(
        onClick = { onClick() },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(start = 30.dp, end = 30.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp)),
        border = BorderStroke(
            0.dp,
            Color.Transparent
        ),
        colors = CardDefaults.cardColors(
            disabledContainerColor = Color.Gray,
            containerColor = Color(0xff7B70FF)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text, fontSize = 15.sp,
                letterSpacing = 0.sp,
                color = Color.White, textAlign = TextAlign.Center
            )
        }
    }
}

