package com.pawmatch.app.shared.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope

expect abstract class BaseViewModel() {
    val viewModelScope: CoroutineScope
    protected open fun onCleared()
}
