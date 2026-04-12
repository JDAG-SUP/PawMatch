package com.pawmatch.app.shared.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

actual abstract class BaseViewModel {
    actual val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    
    actual protected open fun onCleared() {
        // Nothing to do by default here, can be overridden.
    }
    
    fun clear() {
        onCleared()
        viewModelScope.cancel()
    }
}
