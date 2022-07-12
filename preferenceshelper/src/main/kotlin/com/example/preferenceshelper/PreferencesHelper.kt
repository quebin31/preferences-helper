package com.example.preferenceshelper

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface BatchScope {

    /**
     * Get the value linked to [key], if no such value exists returns `null`.
     */
    fun <T> get(key: Preferences.Key<T>): T?

    /**
     * Save the given [key]-[value] pair, similar to [PreferencesHelper.save].
     */
    fun <T> save(key: Preferences.Key<T>, value: T)

    /**
     * Update the value linked to [key], if no such value exists then [default] is used
     * and passed to [transform], similar to [PreferencesHelper.update].
     */
    fun <T> update(key: Preferences.Key<T>, default: T, transform: (T) -> T): T

    /**
     * Delete the value linked to [key] and return it, returns `null` if no
     * value was deleted.
     */
    fun <T> delete(key: Preferences.Key<T>): T?

    /**
     * Delete the value linked to [key] if [predicate] returns `true`, returns the deleted value
     * or `null` if no value was deleted because it didn't exist or [predicate] returned `false`.
     */
    fun <T> deleteIf(key: Preferences.Key<T>, predicate: (T) -> Boolean): T?
}

interface PreferencesHelper {

    /**
     * Return the underlying [Flow]<[Preferences]> in the DataStore instance.
     */
    val data: Flow<Preferences>

    /**
     * Get a [Flow] which emits values linked to [key], may emit `null` if no such key-value
     * pair exists.
     */
    fun <T> getAsFlow(key: Preferences.Key<T>): Flow<T?>

    /**
     * Get a [T] value from the inner data store, if no such [key]-value pair exists this returns
     * `null`.
     */
    suspend fun <T> get(key: Preferences.Key<T>): T?

    /**
     * Save the given [key]-[value] pair in the data store.
     */
    suspend fun <T> save(key: Preferences.Key<T>, value: T)

    /**
     * Update the value linked to [key], if no such key-value pair exists, then [default] is
     * used, [transform] should use the current value and return the new one.
     */
    suspend fun <T> update(key: Preferences.Key<T>, default: T, transform: (T) -> T): T

    /**
     * Delete the [key]-value pair from data store and return the deleted value, returns `null` if
     * no value was deleted.
     */
    suspend fun <T> delete(key: Preferences.Key<T>): T?

    /**
     * Delete the [key]-value pair from data store if [predicate] is `true` and return the deleted
     * value, if nothing is deleted because the key doesn't exists or because `predicate` was
     * `false` then this returns `null`.
     */
    suspend fun <T> deleteIf(key: Preferences.Key<T>, predicate: (T) -> Boolean): T?

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
     *
     * **WARNING: DO NOT call DataStore raw operations or external [PreferencesHelper] operations
     * in the batch scope, it could cause deadlocks.**
     */
    suspend fun batch(block: BatchScope.() -> Unit)

    /**
     * Clear the whole data store.
     */
    suspend fun clear()

    /**
     * Clear the whole data store but keep the provided [keys].
     */
    suspend fun clearButKeep(vararg keys: Preferences.Key<*>)
}
