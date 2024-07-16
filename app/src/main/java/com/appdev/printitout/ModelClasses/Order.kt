package com.appdev.printitout.ModelClasses

import com.google.gson.annotations.SerializedName

data class Order(
    val ordine: Orderine,
    val elementi: List<Elemento>
)