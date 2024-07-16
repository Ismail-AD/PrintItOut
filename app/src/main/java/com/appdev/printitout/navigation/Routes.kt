package com.appdev.printitout.navigation

sealed class Routes(val route: String) {
    object MainScr : Routes("Main_Screen")
    object ScannedDevices : Routes("SDevices_Screen")
    object FirstScreen : Routes("fScreen_Screen")
}