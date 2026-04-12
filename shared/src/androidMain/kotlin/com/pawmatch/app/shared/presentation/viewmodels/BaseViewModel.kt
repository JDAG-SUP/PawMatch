package com.pawmatch.app.shared.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

actual abstract class BaseViewModel : ViewModel() {
    actual val viewModelScope: CoroutineScope = this.viewModelScope
    
    actual override fun onCleared() {
        super.onCleared()
    }
}
