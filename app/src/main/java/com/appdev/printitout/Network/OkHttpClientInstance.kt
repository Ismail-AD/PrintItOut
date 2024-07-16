package com.appdev.printitout.Network

import okhttp3.OkHttpClient

object OkHttpClientInstance {
    val instance: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // Add any custom configurations here if needed
            .build()
    }
}