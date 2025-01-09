package com.davidmerchan.encriptdatastore

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

@Serializable
data class UserPreferences(
    val data: String? = null
)

// Serializer to be used in DataStore
object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        // Get the encrypted saved data
        val encryptedData = withContext(Dispatchers.IO) {
            input.use { it.readBytes() }
        }
        // Decode data from Base64 encoded
        val encryptedByteDecode = Base64.getDecoder().decode(encryptedData)
        // Decrypt the data
        val decryptedBytes = CryptoUtil.decrypt(encryptedByteDecode)
        // Decode the data to json string
        val decodeJsonString = decryptedBytes.decodeToString()
        // Return userPreferences decoded
        return Json.decodeFromString(decodeJsonString)
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        // Transform user preferences into Json
        val json = Json.encodeToString(t)
        // Transform json to byteArray
        val bytes = json.toByteArray()
        // Encrypt the json
        val encryptedBytes = CryptoUtil.encrypt(bytes)
        // Transform the encrypted data to Base64
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)

        // Write the encrypted bytes to the output stream
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }
}
