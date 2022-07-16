package com.quebin31.preferenceshelper

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface BatchScope {

    /**
     * Get the value associated to the given [key], if no such value exists returns `null`.
     */
    fun <T> get(key: Preferences.Key<T>): T?

    /**
     * Save the given [key]-[value] pair, overwrites any other saved value.
     */
    fun <T> save(key: Preferences.Key<T>, value: T)

    /**
     * Update the value associated to the given [key], if no such value exists then the [default]
     * value is used and passed to the [transform] lambda, returns the updated value.
     */
    fun <T> update(key: Preferences.Key<T>, default: T, transform: (T) -> T): T

    /**
     * Delete the value associated to the given [key] and return it, returns `null` if no
     * value was deleted.
     */
    fun <T> delete(key: Preferences.Key<T>): T?

    /**
     * Delete the value associated to the given [key] if [predicate] returns `true`, returns
     * the deleted value or `null` if no value was deleted because it didn't exist or [predicate]
     * returned `false`.
     */
    fun <T> deleteIf(key: Preferences.Key<T>, predicate: (T) -> Boolean): T?
}

interface PreferencesHelper {

    /**
     * Return the underlying [Flow]<[Preferences]> in the DataStore instance.
     */
    val data: Flow<Preferences>

    /**
     * Get a [Flow] which emits values associated to the given [key], may emit `null` if no such
     * value exists at that moment.
     */
    fun <T> getAsFlow(key: Preferences.Key<T>): Flow<T?>

    /**
     * Get the value associated to the given [key], if no such value exists returns `null`.
     */
    suspend fun <T> get(key: Preferences.Key<T>): T?

    /**
     * Save the given [key]-[value] pair, overwrites any other saved value.
     */
    suspend fun <T> save(key: Preferences.Key<T>, value: T)

    /**
     * Update the value associated to the given [key], if no such value exists then the [default]
     * value is used and passed to the [transform] lambda, returns the updated value.
     */
    suspend fun <T> update(key: Preferences.Key<T>, default: T, transform: (T) -> T): T

    /**
     * Delete the value associated to the given [key] and return it, returns `null` if no
     * value was deleted.
     */
    suspend fun <T> delete(key: Preferences.Key<T>): T?

    /**
     * Delete the value associated to the given [key] if [predicate] returns `true`, returns
     * the deleted value or `null` if no value was deleted because it didn't exist or [predicate]
     * returned `false`.
     */
    suspend fun <T> deleteIf(key: Preferences.Key<T>, predicate: (T) -> Boolean): T?

    /**
     * Perform a batch of operations atomically, useful to avoid multiple separate write
     * operations. For example, instead of doing the following 4 separate atomic operations:
     *
     * ```
     * helper.save(keyA, valueA)
     * helper.save(keyB, valueB)
     * helper.delete(keyC)
     * helper.update(keyD, default = 0) { it + 1 }
     * ```
     *
     * One can do a single batch atomic operation as follows:
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
     * in the batch scope, it could cause dead-locks.**
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
