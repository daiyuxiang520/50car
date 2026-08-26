package com.fiftycar.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// ★ token 存独立文件名,已在 backup_rules 中排除云备份
private val Context.secureStore by preferencesDataStore(name = "secure_tokens")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val KEY_TOKEN = stringPreferencesKey("access_token")
    private val KEY_VIN = stringPreferencesKey("current_vin")

    val tokenFlow: Flow<String?> = context.secureStore.data.map { it[KEY_TOKEN] }
    val vinFlow: Flow<String?> = context.secureStore.data.map { it[KEY_VIN] }

    suspend fun save(token: String, vin: String?) {
        context.secureStore.edit {
            it[KEY_TOKEN] = token
            if (vin != null) it[KEY_VIN] = vin
        }
    }

    suspend fun clear() = context.secureStore.edit { it.clear() }
}
