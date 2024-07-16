package com.appdev.printitout.Repository

import com.appdev.printitout.ModelClasses.Elemento
import com.appdev.printitout.ModelClasses.Order
import com.appdev.printitout.ModelClasses.Orderine
import com.appdev.printitout.Network.OkHttpClientInstance
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class Repository {
    fun getOrdersFlow(
        key: String,
        codice_reparto: String,
        module: String = "pos",
        function: String = "getorders",
        no_set_print: Int = 1
    ): Flow<List<Order>> = flow {
        val client = OkHttpClientInstance.instance
        val url = "https://demo.kliveer.cloud/webservice/api.php" +
                "?key=$key" +
                "&module=$module" +
                "&function=$function" +
                "&codice_reparto=$codice_reparto" +
                "&no_set_print=$no_set_print"
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let { responseBody ->
                    val jsonObject = JsonParser().parse(responseBody.string()).asJsonObject
                    jsonObject.remove("return_caret")

                    if (jsonObject.size() > 0) {
                        val gson = Gson()
                        val listOfOrders = jsonObject.entrySet().mapNotNull { (orderId, orderData) ->
                            if (orderData.isJsonObject) {
                                val orderDataObject = orderData.asJsonObject
                                val ordine = gson.fromJson(
                                    orderDataObject.getAsJsonObject("ordine"),
                                    Orderine::class.java
                                )
                                val elementiArray = gson.fromJson(
                                    orderDataObject.getAsJsonArray("elementi"),
                                    Array<Elemento>::class.java
                                )
                                Order(ordine = ordine, elementi = elementiArray.toList())
                            } else {
                                null
                            }
                        }
                        emit(listOfOrders)
                    } else {
                        emit(emptyList())
                    }
                } ?: emit(emptyList())
            } else {
                emit(emptyList())
            }
        } catch (e: IOException) {
            emit(emptyList())
        }
    }
}