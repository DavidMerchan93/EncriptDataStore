package com.davidmerchan.encriptdatastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * This utility class provides methods for encrypting and decrypting data using AES encryption algorithm.
 * It uses the Android KeyStore to securely store and retrieve the encryption key.
 */
object CryptoUtil {

    private const val KEY_ALIAS = "secretkey"
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    private val cipher = Cipher.getInstance(TRANSFORMATION)
    private val keyStore = KeyStore
        .getInstance("AndroidKeyStore")
        .apply {
            load(null)
        }

    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore
            .getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createSecretKey()
    }

    private fun createSecretKey(): SecretKey {
        return KeyGenerator
            .getInstance(ALGORITHM)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        .setRandomizedEncryptionRequired(true)
                        .setUserAuthenticationRequired(false)
                        .build()
                )
            }
            .generateKey()
    }

    /**
     * Encrypts the given data using the stored secret key.
     *
     * @param data The data to be encrypted.
     * @return The encrypted data as a byte array. The first [Cipher.blockSize] bytes represent the initialization vector (IV).
     */
    fun encrypt(data: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        return iv + encryptedData
    }

    /**
     * Decrypts the given encrypted data using the stored secret key.
     *
     * @param data The encrypted data to be decrypted. The first [Cipher.blockSize] bytes represent the initialization vector (IV).
     * @return The decrypted data as a byte array.
     */
    fun decrypt(data: ByteArray): ByteArray {
        val iv = data.copyOfRange(0, cipher.blockSize)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), IvParameterSpec(iv))
        val bytes = data.copyOfRange(cipher.blockSize, data.size)
        return cipher.doFinal(bytes)
    }

}
