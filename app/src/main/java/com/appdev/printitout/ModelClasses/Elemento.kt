package com.appdev.printitout.ModelClasses

data class Elemento(
    val api_printer_note: String,
    val api_printer_variazioni_list: List<String>,
    val qta: String,
    val nome_prodotto: String,
    val note: String
)