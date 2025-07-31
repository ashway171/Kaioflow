package com.ateeb.kaioflow.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class HomeViewModel  : ViewModel(){
    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    fun processIntent(intent: HomeIntent){
        when (intent) {
            is HomeIntent.SetFocusDuration -> {
                val timeInMillis = ((intent.hours.toInt() * 3600) + (intent.minutes.toInt() * 60)) * 1000L
                _state.value = _state.value.copy(selectedTimeInMillis = timeInMillis)
            }
        }
    }

}