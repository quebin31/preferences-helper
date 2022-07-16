# Preferences Helper

Minimal add-on library that provides a nicer API for [Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore#preferences-datastore) 
which is more readable and convenient.

## Getting started
Just include the following dependency in your Gradle script, no need to add any extra repository
if you're already using the Maven Central repository (which in most cases you'll be using):

```kotlin
implementation("com.quebin31:preferences-helper:1.0.0")
```

## Increment counter example

As per the official Preferences DataStore documentation you'd write the following to update a value:

```kotlin
val Context.dataStore by preferencesDatastore(name = "datastore") // top file declaration

suspend fun incrementCounter() {
    val keyCounter = intPreferencesKey(name = "counter")
    context.dataStore.edit { mutablePreferences ->
        val currentValue = mutablePreferences[keyCounter] ?: 0
        mutablePreferences[keyCounter] = currentValue + 1
    }
}
```

That's a lot of boilerplate just to update a value, compare it to what you'd write if you were using 
this library:

```kotlin
val Context.dataStore by preferencesDatastore(name = "datastore") // top file declaration

// Somewhere you have access to the context, perhaps in a `PreferencesManager` that can be injected
// with Hilt
val helper = PreferencesHelper(context.dataStore) 

suspend fun incrementCounter() {
    val keyCounter = intPreferencesKey(name = "counter")
    helper.update(keyCounter, default = 0) { it + 1 }
}
```

## Performance
By calling `PreferencesHelper(datastore)` we're just creating a thin wrapper around our preferences
datastore, all calls are transformed to the operations you'd normally do if you were using the 
`edit` function directly, though there's an **important catch**, each function declared in the 
`PreferencesHelper` interface is atomic and separated from the others, you don't have to commit 
your changes, this has the advantage of providing an easy way to update, save, read or delete a value
in very few lines.

However, the disadvantage is that doing multiple separate atomic operations is too expensive, each time 
a value in the internal data is updated the whole preferences map is updated, the solution to this is 
using the `batch` operation whenever you want to update multiple values at once in a single place, 
this is pretty similar to using `edit` but with the benefit of having scoped operations identical to the
ones found in the `PreferencesHelper` interface:

### Example

Instead of doing this 3 separate atomic operations:
```kotlin
helper.save(keyA, 3)
helper.delete(keyB)
helper.update(keyC, default = 0) { it + 1 }
```

You can use `batch` to reduce the number of atomic operations from 3 to 1:
```kotlin
helper.batch { // single transaction
    save(keyA, 3)
    delete(keyB)
    update(keyC, default = 0) { it + 1 }
}
```


