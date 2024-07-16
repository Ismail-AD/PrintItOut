package com.appdev.printitout.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.appdev.printitout.ViewModel.MyViewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appdev.printitout.ui.screens.FirstScreen
import com.appdev.printitout.ui.screens.MainScreen
import com.appdev.printitout.ui.screens.ScanDevicesList

@Composable
fun navGraph(controller: NavHostController, viewModel: MyViewModel) {
    NavHost(
        navController = controller,
        startDestination = Routes.FirstScreen.route, modifier = Modifier.background(Color.White)
    ) {
        composable(route = Routes.MainScr.route) {
            MainScreen(viewModel = viewModel, controller = controller)
        }
        composable(route = Routes.ScannedDevices.route) {
           ScanDevicesList(viewModel = viewModel, navController = controller)
        }
        composable(route = Routes.FirstScreen.route) {
            FirstScreen(viewModel = viewModel, navController = controller)
        }
    }
}
