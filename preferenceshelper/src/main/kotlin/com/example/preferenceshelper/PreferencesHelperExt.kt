package com.example.preferenceshelper

import androidx.datastore.preferences.core.Preferences

/**
 * Get a [T] value from the inner data store, if no such [key]-value pair exists this returns
 * the given [fallback] value. Basically a wrapper for `get(key) ?: fallback`
 */
suspend inline fun <T> PreferencesHelper.getOrFallback(key: Preferences.Key<T>, fallback: T): T =
    get(key) ?: fallback

/**
 * Get a [T] value from the inner data store, if no such [key]-value pair exists this saves the
 * given [default] value and returns it.
 */
suspend inline fun <T> PreferencesHelper.getOrPut(key: Preferences.Key<T>, default: T): T =
    update(key, default = default, transform = { it })

/**
 * Get a [T] value from the inner data store, if no such [key]-value pair exists this returns
 * `null`. Applies [transform] before returning if the value exists.
 */
suspend inline fun <T, R> PreferencesHelper.get(key: Preferences.Key<R>, transform: (R) -> T): T? =
    get(key)?.let(transform)

/**
 * Save the value produced by [producer] with the given [key].
 */
suspend inline fun <T> PreferencesHelper.saveWith(key: Preferences.Key<T>, producer: () -> T) {
    save(key, producer())
}

/**
 * Get a [T] value from the inner data store, if no such [key]-value pair exists this returns
 * `null`. Applies [transform] before returning if the value exists.
 */
inline fun <T, R> BatchScope.get(key: Preferences.Key<R>, transform: (R) -> T): T? =
    get(key)?.let(transform)

/**
 * Save the value produced by [producer] with the given [key].
 */
inline fun <T> BatchScope.saveWith(key: Preferences.Key<T>, producer: () -> T) {
    save(key, producer())
}