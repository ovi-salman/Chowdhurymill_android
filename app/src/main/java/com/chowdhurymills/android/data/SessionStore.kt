package com.chowdhurymills.android.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chowdhurymills.android.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "mill_prefs")

class SessionStore(private val context: Context) {
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val cookieKey = stringPreferencesKey("session_cookie")

    val baseUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[baseUrlKey]?.takeIf { it.isNotBlank() } ?: BuildConfig.DEFAULT_BASE_URL
    }

    suspend fun baseUrl(): String = baseUrlFlow.first()

    suspend fun setBaseUrl(url: String) {
        val cleaned = url.trim().trimEnd('/')
        context.dataStore.edit { it[baseUrlKey] = cleaned }
    }

    suspend fun sessionCookie(): String =
        context.dataStore.data.map { it[cookieKey].orEmpty() }.first()

    suspend fun setSessionCookie(cookie: String) {
        context.dataStore.edit { it[cookieKey] = cookie }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.remove(cookieKey) }
    }
}
