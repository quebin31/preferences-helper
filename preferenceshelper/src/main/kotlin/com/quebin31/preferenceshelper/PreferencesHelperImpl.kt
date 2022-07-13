package com.quebin31.preferenceshelper

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        dataStore.data.first()[key]

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

    override suspend fun <T> delete(key: Preferences.Key<T>): T? =
        deleteIf(key) { true }

    override suspend fun <T> deleteIf(key: Preferences.Key<T>, predicate: (T) -> Boolean): T? {
        var deletedValue: T? = null
        dataStore.edit { preferences ->
            val value = preferences[key]
            if (value != null && predicate(value)) {
                preferences.minusAssign(key)
                deletedValue = value
            }
        }

        return deletedValue
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

    override suspend fun clearButKeep(vararg keys: Preferences.Key<*>) {
        dataStore.edit { preferences ->
            val keyValues = keys.associateWith { key ->
                preferences[key]
            }

            preferences.clear()

            for ((key, value) in keyValues) {
                @Suppress("UNCHECKED_CAST")
                when (value) {
                    is Int -> preferences[intPreferencesKey(key.name)] = value
                    is Double -> preferences[doublePreferencesKey(key.name)] = value
                    is String -> preferences[stringPreferencesKey(key.name)] = value
                    is Boolean -> preferences[booleanPreferencesKey(key.name)] = value
                    is Float -> preferences[floatPreferencesKey(key.name)] = value
                    is Long -> preferences[longPreferencesKey(key.name)] = value
                    is Set<*> -> preferences[stringSetPreferencesKey(key.name)] = value as Set<String>
                }
            }
        }
    }
}

private class BatchScopeImpl(private val mutablePreferences: MutablePreferences) : BatchScope {

    override fun <T> get(key: Preferences.Key<T>): T? = mutablePreferences[key]

    override fun <T> save(key: Preferences.Key<T>, value: T) {
        mutablePreferences[key] = value
    }

    override fun <T> update(key: Preferences.Key<T>, default: T, transform: (T) -> T): T {
        return transform(mutablePreferences[key] ?: default).also { newValue ->
            mutablePreferences[key] = newValue
        }
    }

    override fun <T> delete(key: Preferences.Key<T>): T? =
        deleteIf(key) { true }

    override fun <T> deleteIf(key: Preferences.Key<T>, predicate: (T) -> Boolean): T? =
        mutablePreferences[key]?.let { value ->
            if (predicate(value)) {
                mutablePreferences.minusAssign(key)
                value
            } else {
                null
            }
        }
}