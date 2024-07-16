package com.appdev.printitout.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.printitout.ModelClasses.Order
import com.appdev.printitout.Repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {
    private val repository = Repository()

    private val _ordersState = MutableStateFlow<List<Order>>(emptyList())
    val ordersState: StateFlow<List<Order>> = _ordersState

    fun fetchOrders(apiKey: String, codiceReparto: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getOrdersFlow(apiKey, codiceReparto).collect { response ->
                _ordersState.value = response
            }
        }
    }
    fun updateOrders(){
        _ordersState.value = emptyList()
    }
}