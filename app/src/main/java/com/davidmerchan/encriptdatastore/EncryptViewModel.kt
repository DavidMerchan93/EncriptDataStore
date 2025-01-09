package com.davidmerchan.encriptdatastore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class EncryptViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesDataStore = UserPreferencesDataStore(application)

    private val _decryptedText = MutableStateFlow("")
    val decryptedText: StateFlow<String> = _decryptedText.asStateFlow()

    fun encrypt(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesDataStore.saveData(
                UserPreferences(data = data)
            )
        }
    }

    fun decrypt() {
        viewModelScope.launch {
            userPreferencesDataStore.getData()
                .flowOn(Dispatchers.IO)
                .collect {
                    _decryptedText.value = it.data ?: "N/A®"
                }
        }
    }

}
