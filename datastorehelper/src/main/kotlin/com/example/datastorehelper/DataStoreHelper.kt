package com.example.datastorehelper

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

/**
 * Create a new [DataStoreHelper] that wraps a [DataStore]<[Preferences]>.
 */
fun DataStoreHelper(dataStore: DataStore<Preferences>): DataStoreHelper =
    DataStoreHelperImpl(dataStore)

interface BatchScope {

    /**
     * Save the given [key]-[value] pair, similar to [DataStoreHelper.save].
     */
    fun <T> save(key: DataStoreKey<T>, value: T)

    /**
     * Update the value linked to [key], if no such value exists then [default] is used
     * and passed to [transform], similar to [DataStoreHelper.update].
     */
    fun <T> update(key: DataStoreKey<T>, default: T, transform: (T) -> T): T

    /**
     * Delete the value linked to [key] and returns the deleted value, returns `null` if no
     * value was deleted.
     */
    fun <T> delete(key: DataStoreKey<T>): T?
}

interface DataStoreHelper {

    /**
     * Get a [Flow] which emits values linked to [key], may emit `null` if no such key-value
     * pair exists.
     */
    fun <T> getAsFlow(key: DataStoreKey<T>): Flow<T?>

    /**
     * Get a [T] value from the inner data store, if no such key-value pair exists this returns
     * `null`.
     */
    suspend fun <T> get(key: DataStoreKey<T>): T?

    /**
     * Save the given [key]-[value] pair in the data store.
     */
    suspend fun <T> save(key: DataStoreKey<T>, value: T)

    /**
     * Update the value linked to [key], if no such key-value pair exists, then [default] is
     * used, [transform] should use the current value and return the new one.
     */
    suspend fun <T> update(key: DataStoreKey<T>, default: T, transform: (T) -> T): T

    /**
     * Delete the [key]-value pair from data store and return the deleted value, returns `null` if
     * no value was deleted.
     */
    suspend fun <T> delete(key: DataStoreKey<T>): T?

    /**
     * Perform a batch of operations atomically, useful to avoid multiple separate write
     * operations. For example, instead of doing the following 4 separate operations:
     *
     * ```
     * helper.save(keyA, valueA)
     * helper.save(keyB, valueB)
     * helper.delete(keyC)
     * helper.update(keyD, default = 0) { it + 1 }
     * ```
     *
     * One can do a single batch operation as follows:
     *
     * ```
     * helper.batch {
     *     save(keyA, valueA)
     *     save(keyB, valueB)
     *     delete(keyC)
     *     update(keyD, default = 0) { it + 1 }
     * }
     * ```
     */
    suspend fun batch(block: BatchScope.() -> Unit)

    /**
     * Clear the whole data store.
     */
    suspend fun clear()
}