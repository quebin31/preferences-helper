package com.example.preferenceshelper

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * Create a [PreferencesHelper] which wraps the given [dataStore].
 */
fun PreferencesHelper(dataStore: DataStore<Preferences>): PreferencesHelper =
    PreferencesHelperImpl(dataStore)

private class PreferencesHelperImpl(private val dataStore: DataStore<Preferences>) :
    PreferencesHelper {

    override val data: Flow<Preferences>
        get() = dataStore.data

    override fun <T> getAsFlow(key: Preferences.Key<T>): Flow<T?> =
        dataStore.data.map { snapshot -> snapshot[key] }

    override suspend fun <T> get(key: Preferences.Key<T>): T? =
        dataStore.data.firstOrNull()?.get(key)

    override suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    override suspend fun <T> update(key: Preferences.Key<T>, default: T, transform: (T) -> T): T {
        val snapshot = dataStore.edit { preferences ->
            preferences[key] = transform(preferences[key] ?: default)
        }

        return snapshot[key]!!
    }

    override suspend fun <T> delete(key: Preferences.Key<T>): T? = get(key)?.also {
        dataStore.edit { preferences ->
            preferences.minusAssign(key)
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

private class BatchScopeImpl(private val mutablePreferences: MutablePreferences) : BatchScope {
    override fun <T> save(key: Preferences.Key<T>, value: T) {
        mutablePreferences[key] = value
    }

    override fun <T> update(key: Preferences.Key<T>, default: T, transform: (T) -> T): T {
        return transform(mutablePreferences[key] ?: default).also { newValue ->
            mutablePreferences[key] = newValue
        }
    }

    override fun <T> delete(key: Preferences.Key<T>): T? = mutablePreferences[key]?.also {
        mutablePreferences.minusAssign(key)
    }
}