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

    override fun <T> getAsFlow(key: DataStoreKey<T>): Flow<T?> =
        dataStore.data.map { snapshot ->
            snapshot[key.toPreferencesKey() as Preferences.Key<T>]
        }

    override suspend fun <T> get(key: DataStoreKey<T>): T? =
        dataStore.data.firstOrNull()?.get(key.toPreferencesKey() as Preferences.Key<T>)

    override suspend fun <T> save(key: DataStoreKey<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key.toPreferencesKey() as Preferences.Key<T>] = value
        }
    }

    override suspend fun <T> update(key: DataStoreKey<T>, default: T, transform: (T) -> T): T {
        val preferencesKey = key.toPreferencesKey() as Preferences.Key<T>
        val snapshot = dataStore.edit { preferences ->
            preferences[preferencesKey] = transform(preferences[preferencesKey] ?: default)
        }

        return snapshot[preferencesKey]!!
    }

    override suspend fun <T> delete(key: DataStoreKey<T>): T? = get(key)?.also {
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
    override fun <T> save(key: DataStoreKey<T>, value: T) {
        mutablePreferences[key.toPreferencesKey() as Preferences.Key<T>] = value
    }

    override fun <T> update(key: DataStoreKey<T>, default: T, transform: (T) -> T): T {
        val preferencesKey = key.toPreferencesKey() as Preferences.Key<T>
        return transform(mutablePreferences[preferencesKey] ?: default).also { newValue ->
            mutablePreferences[preferencesKey] = newValue
        }
    }

    override fun <T> delete(key: DataStoreKey<T>): T? {
        val preferencesKey = key.toPreferencesKey() as Preferences.Key<T>
        return mutablePreferences[preferencesKey]?.also {
            mutablePreferences.minusAssign(preferencesKey)
        }
    }
}