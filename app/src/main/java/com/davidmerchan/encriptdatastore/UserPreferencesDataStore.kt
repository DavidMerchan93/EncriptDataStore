package com.davidmerchan.encriptdatastore

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow


private val Context.dataStore by dataStore(
    fileName = "user-preferences",
    serializer = UserPreferencesSerializer
)

class UserPreferencesDataStore(
    private val context: Context
) {
    suspend fun saveData(userPreferences: UserPreferences) {
        context.dataStore.updateData {
            userPreferences
        }
    }

    fun getData(): Flow<UserPreferences> {
        return context.dataStore.data
    }
}
