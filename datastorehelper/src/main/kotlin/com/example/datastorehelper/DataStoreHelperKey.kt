@file:Suppress("MemberVisibilityCanBePrivate")

package com.example.datastorehelper

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlin.reflect.KClass

sealed class DataStoreHelperKey<T : Any> private constructor(
    val name: String,
    internal val kClass: KClass<T>,
) {
    class PrimitiveKey<T : Any>(name: String, kClass: KClass<T>) :
        DataStoreHelperKey<T>(name, kClass)

    class ObjectKey<T : Any>(name: String, kClass: KClass<T>) :
        DataStoreHelperKey<T>(name, kClass)

    internal fun toPreferencesKey(): Preferences.Key<*> = when (kClass) {
        Int::class -> intPreferencesKey(name)
        Double::class -> doublePreferencesKey(name)
        String::class -> stringPreferencesKey(name)
        Boolean::class -> booleanPreferencesKey(name)
        Float::class -> floatPreferencesKey(name)
        Long::class -> longPreferencesKey(name)
        else -> stringPreferencesKey(name)
    }

    internal fun isObjectKey(): Boolean = this is ObjectKey<*>
}

inline fun <reified T : Any> dataStoreHelperKey(name: String): DataStoreHelperKey<T> {
    val anyKey = when (T::class) {
        Int::class -> DataStoreHelperKey.PrimitiveKey(name, Int::class)
        Double::class -> DataStoreHelperKey.PrimitiveKey(name, Double::class)
        String::class -> DataStoreHelperKey.PrimitiveKey(name, String::class)
        Boolean::class -> DataStoreHelperKey.PrimitiveKey(name, Boolean::class)
        Float::class -> DataStoreHelperKey.PrimitiveKey(name, Float::class)
        Long::class -> DataStoreHelperKey.PrimitiveKey(name, Long::class)
        else -> DataStoreHelperKey.ObjectKey(name, T::class)
    }

    @Suppress("UNCHECKED_CAST")
    return anyKey as DataStoreHelperKey<T>
}

private val Context.test by preferencesDataStore(name = "test")

suspend fun foo(context: Context) {
    val key = dataStoreHelperKey<Int>(name = "key_name")
    val helper = DataStoreHelper(context.test)
    helper.save(key, 3)
    val value = helper.get(key)
}
