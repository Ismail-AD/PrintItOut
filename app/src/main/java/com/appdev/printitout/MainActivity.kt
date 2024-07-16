package com.appdev.printitout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.appdev.printitout.ViewModel.MyViewModel
import com.appdev.printitout.navigation.navGraph
import com.appdev.printitout.ui.screens.MainScreen

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MyViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
            val navController = rememberNavController()
            navGraph(navController,viewModel)

        }
    }
}


