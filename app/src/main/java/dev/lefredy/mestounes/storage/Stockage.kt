package dev.lefredy.mestounes.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class Stockage(contexte: Context) {
    private val preferences = EncryptedSharedPreferences.create(
        contexte,
        "stockage",
        MasterKey.Builder(contexte).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun sauvegarder(cle: String, valeur: String) {
        preferences.edit().putString(cle, valeur).apply()
    }

    fun charger(cle: String, defaut: String = ""): String {
        return preferences.getString(cle, defaut) ?: defaut
    }

    fun sauvegarderLong(cle: String, valeur: Long) {
        preferences.edit().putLong(cle, valeur).apply()
    }

    fun chargerLong(cle: String): Long {
        return preferences.getLong(cle, 0L)
    }
}
