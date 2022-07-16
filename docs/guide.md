# Guide 

1. [Index](#)
   1. [Build a `PreferencesHelper`](#build-a-preferenceshelper)
   2. [Keys](#keys)
   3. [Operations](#operations)
      1. [Get a value](#get-a-value)
      2. [Get a value as a `Flow`](#get-a-value-as-a-flow)
      3. [Save](#save)
      4. [Update](#update)
      5. [Delete](#delete)
      6. [Delete if](#delete-if)
      7. [Batch](#batch)
      8. [Clear](#clear)
      9. [Clear but keep](#clear-but-keep)
   4. [Extensions](#extensions)
      1. [Get or fallback](#get-or-fallback)
      2. [Get or put](#get-or-put)
      3. [Get and save with transformation](#get-and-save-with-transformation)

## Build a `PreferencesHelper`
```kotlin
// Could be private to the file if you want to only have access to the DataStore 
// using the `PreferencesHelper` wrapper. 
private val Context.dataStore by preferencesDatastore(name = "datastore")

// In the same file you could have an injectable interface with access to the 
// Application context.
val preferencesHelper = PreferencesHelper(context.dataStore)
```

## Keys
One thing I often do is define my keys in a single file like the following:
```kotlin
object Keys {
   val Counter = intPreferencesKey(name = "counter")
   val Host = stringPrefrencesKey(name = "host")
   // etc...
}
```

This has the benefit of grouping all keys in a single place while aliasing them behind 
typed values, it'll be really hard to mistakenly try to save or get a value with a key 
of the incorrect type.

## Operations

### Get a value
```kotlin
// Getting a value may return null if no value is associated with the given key.
val value: Int? = preferencesHelper.get(Keys.Counter)
```

### Get a value as a `Flow`
```kotlin
// The nullability is inside the Flow, if no value is associated with the given key 
// the flow will emit a null value, once you save a value the flow will emit the new 
// value immediately.
val flow: Flow<Int?> = preferencesHelper.get(Keys.Counter)
```

### Save 
```kotlin
// Saves the value even if there's a value already stored with the same key.
preferencesHelper.save(Keys.Counter, 0)
```

### Update
```kotlin
// Update the value associated to the given key, if no value exists then default will
// be used as the initial value passed to the lambda.
val newValue: Int = preferencesHelper.update(Keys.Counter, default = 0) {
    it + 1 // Increment by one
}
```

### Delete
```kotlin
// Returns the deleted value if any, otherwise returns null. 
val deletedValue: Int? = preferencesHelper.delete(Keys.Counter)
```

### Delete if
```kotlin
// Deletes the value if the predicate returns true for the current value.
val deleteValue: Int? = preferencesHelper.deleteIf(Keys.Counter) { it == 10 }
```

### Batch
```kotlin
// You can do pretty much everything you can do with the PreferencesHelper, but 
// keeping it as a single transaction, note however that you MUST NOT use preferences
// helper inside the batch because it could cause a dead-lock.
preferencesHelper.batch {
   val value: Int? = get(Keys.Counter)
   save(Keys.Host, "192.168.1.10")
   val newValue: Int = update(Keys.Counter, default = 0) { it + 1 }
   val deletedHost: Int? = delete(Keys.Host)
   val deletedCounter: Int? = deleteIf(Keys.Counter) { it == 10 }
}
```

### Clear
```kotlin
// Remove everything!
preferencesHelper.clear()
```

### Clear but keep
```kotlin
// Remove everything except the given keys
preferencesHelper.clearButKeep(Keys.Counter, Keys.Host)
```

## Extensions
Some operations are written as extensions on top of the provided operations.

### Get or fallback
```kotlin
// If no value is associated with the given key then the `fallback` value is returned
val value: Int = preferencesHelper.getOrFallback(Keys.Counter, fallback = 3)
```

### Get or put
```kotlin
// If no value is associated with the given key then the `default` value is saved
// and then it's returned.
val value: Int = preferencesHelper.getOrPut(Keys.Counter, default = 3)
```

### Get and save with transformation
```kotlin
// One usual example is saving Date objects as Long values
val dateKey = longPreferencesKey(name = "date")

// Saving the value
val date = Date()
preferencesHelper.saveWith(dateKey) { date.time }

// Getting the value
preferencesHelper.get(dateKey, ::Date)
// or
preferencesHelper.get(dateKey) { Date(it) }
```

This extensions are also available for `BatchScope`:

```kotlin
val dateKey = longPreferencesKey(name = "date")
val date = Date()

preferencesHelper.batch {
   saveWith(dateKey) { date.time }
   get(dateKey, ::Date)
   // or 
   get(dateKey) { Date(it) }
}
```