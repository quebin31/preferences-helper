package com.example.datastorehelper

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@Suppress("UNCHECKED_CAST")
internal class DataStoreHelperImpl(private val dataStore: DataStore<Preferences>) :
    DataStoreHelper {

    override fun <T : Any> getAsFlow(key: DataStoreHelperKey<T>): Flow<T?> =
        dataStore.data.map { snapshot ->
            snapshot[key.toPreferencesKey() as Preferences.Key<T>]
        }

    override suspend fun <T : Any> get(key: DataStoreHelperKey<T>): T? =
        dataStore.data.firstOrNull()?.get(key.toPreferencesKey() as Preferences.Key<T>)

    override suspend fun <T : Any> save(key: DataStoreHelperKey<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key.toPreferencesKey() as Preferences.Key<T>] = value
        }
    }

    override suspend fun <T : Any> update(key: DataStoreHelperKey<T>, default: T, transform: (T) -> T): T {
        val preferencesKey = key.toPreferencesKey() as Preferences.Key<T>
        val snapshot = dataStore.edit { preferences ->
            preferences[preferencesKey] = transform(preferences[preferencesKey] ?: default)
        }

        return snapshot[preferencesKey]!!
    }

    override suspend fun <T : Any> delete(key: DataStoreHelperKey<T>): T? = get(key)?.also {
        dataStore.edit { preferences ->
            preferences.minusAssign(key.toPreferencesKey() as Preferences.Key<T>)
        }
    }

    override suspend fun batch(block: BatchScope.() -> Unit) {
        dataStore.edit {
            BatchScopeImpl(it).block()
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

@Suppress("UNCHECKED_CAST")
private class BatchScopeImpl(private val mutablePreferences: MutablePreferences) : BatchScope {
    override fun <T : Any> save(key: DataStoreHelperKey<T>, value: T) {
        mutablePreferences[key.toPreferencesKey() as Preferences.Key<T>] = value
    }

    override fun <T : Any> update(key: DataStoreHelperKey<T>, default: T, transform: (T) -> T): T {
        val preferencesKey = key.toPreferencesKey() as Preferences.Key<T>
        return transform(mutablePreferences[preferencesKey] ?: default).also { newValue ->
            mutablePreferences[preferencesKey] = newValue
        }
    }

    override fun <T : Any> delete(key: DataStoreHelperKey<T>): T? {
        val preferencesKey = key.toPreferencesKey() as Preferences.Key<T>
        return mutablePreferences[preferencesKey]?.also {
            mutablePreferences.minusAssign(preferencesKey)
        }
    }
}