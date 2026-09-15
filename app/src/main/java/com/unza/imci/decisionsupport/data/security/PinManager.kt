package com.unza.imci.decisionsupport.data.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Manages the app-level PIN lock.
 * The app excludes full user authentication (see proposal Scope), so this PIN is the
 * only barrier protecting locally stored patient data on a shared or lost device.
 * The PIN itself is never stored — only a salted SHA-256 hash of it.
 */
object PinManager {
    private const val PREFS_NAME = "imci_security_prefs"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_PIN_SALT = "pin_salt"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isPinSet(context: Context): Boolean {
        return prefs(context).contains(KEY_PIN_HASH)
    }

    fun setPin(context: Context, pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        prefs(context).edit()
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val storedHash = prefs(context).getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = prefs(context).getString(KEY_PIN_SALT, null) ?: return false
        val salt = Base64.decode(storedSalt, Base64.NO_WRAP)
        return hashPin(pin, salt) == storedHash
    }

    /** Clears the stored PIN, requiring setup again. Used as the "forgot PIN" recovery path. */
    fun clearPin(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}
