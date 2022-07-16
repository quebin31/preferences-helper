package com.quebin31.preferenceshelper

import androidx.datastore.preferences.core.Preferences

/**
 * Get the value associated to the given [key], if no such value exists returns the given [fallback].
 * Basically a wrapper for `get(key) ?: fallback`
 */
suspend inline fun <T> PreferencesHelper.getOrFallback(key: Preferences.Key<T>, fallback: T): T =
    get(key) ?: fallback

/**
 * Get the value associated to the given [key], if no such value exists then the [default] value
 * is saved and returned.
 */
suspend inline fun <T> PreferencesHelper.getOrPut(key: Preferences.Key<T>, default: T): T =
    update(key, default = default, transform = { it })

/**
 * Get the value associated to the given [key], if no such value exists returns `null`. Applies
 * [transform] before returning the non-null value.
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
 * Get the value associated to the given [key], if no such value exists returns `null`. Applies
 * [transform] before returning the non-null value.
 */
inline fun <T, R> BatchScope.get(key: Preferences.Key<R>, transform: (R) -> T): T? =
    get(key)?.let(transform)

/**
 * Save the value produced by [producer] with the given [key].
 */
inline fun <T> BatchScope.saveWith(key: Preferences.Key<T>, producer: () -> T) {
    save(key, producer())
}