package com.example.datastorehelper

import androidx.datastore.preferences.core.*

sealed class DataStoreKey<T>(val name: String) {
    class IntKey(name: String) : DataStoreKey<Int>(name)
    class DoubleKey(name: String) : DataStoreKey<Double>(name)
    class StringKey(name: String) : DataStoreKey<String>(name)
    class BooleanKey(name: String) : DataStoreKey<Boolean>(name)
    class FloatKey(name: String) : DataStoreKey<Float>(name)
    class LongKey(name: String) : DataStoreKey<Long>(name)
    class StringSetKey(name: String) : DataStoreKey<Set<String>>(name)
    class ObjectKey<T : Any>(name: String) : DataStoreKey<T>(name)

    @Suppress("UNCHECKED_CAST")
    internal fun toPreferencesKey(): Preferences.Key<*> = when (this) {
        is IntKey -> intPreferencesKey(name)
        is DoubleKey -> doublePreferencesKey(name)
        is StringKey -> stringPreferencesKey(name)
        is BooleanKey -> booleanPreferencesKey(name)
        is FloatKey -> floatPreferencesKey(name)
        is LongKey -> longPreferencesKey(name)
        is StringSetKey -> stringSetPreferencesKey(name)
        is ObjectKey -> stringPreferencesKey(name)
    }

    internal fun isObjectKey(): Boolean = this is ObjectKey<*>
}


fun intDataStoreKey(name: String) = DataStoreKey.IntKey(name)

fun doubleDataStoreKey(name: String) = DataStoreKey.DoubleKey(name)

fun stringDataStoreKey(name: String) = DataStoreKey.StringKey(name)

fun booleanDataStoreKey(name: String) = DataStoreKey.BooleanKey(name)

fun floatDataStoreKey(name: String) = DataStoreKey.FloatKey(name)

fun longDataStoreKey(name: String) = DataStoreKey.LongKey(name)

fun stringSetDataStoreKey(name: String) = DataStoreKey.StringSetKey(name)

fun <T : Any> objectDataStoreKey(name: String) = DataStoreKey.ObjectKey<T>(name)
