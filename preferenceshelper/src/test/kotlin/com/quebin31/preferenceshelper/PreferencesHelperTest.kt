package com.quebin31.preferenceshelper

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import com.quebin31.preferenceshelper.test.CoroutinesTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import strikt.api.expectThat
import strikt.assertions.*
import java.util.*

private val Context.testDataStore by preferencesDataStore(name = "testDataStore")

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PreferencesHelperTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule(StandardTestDispatcher())

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferencesHelper = PreferencesHelper(context.testDataStore)

    @After
    fun clean() = runTest {
        preferencesHelper.clear()
    }

    @Test
    fun `test saving and getting a value`() = runTest {
        val intKey = intPreferencesKey(name = "keyA")
        preferencesHelper.save(intKey, 3)
        expectThat(preferencesHelper.get(intKey)).isEqualTo(3)
    }

    @Test
    fun `test saving and getting as flow`() = runTest {
        val longKey = longPreferencesKey(name = "keyA")
        val collectedValues = mutableListOf<Long>()
        val job = launch(UnconfinedTestDispatcher()) {
            preferencesHelper
                .getAsFlow(longKey)
                .filterNotNull()
                .collect {
                    collectedValues.add(it)
                }
        }

        val valuesToInsert = listOf(1L, 2, 3, 4, 5)
        for (value in valuesToInsert) {
            preferencesHelper.save(longKey, value)
        }

        runCurrent()
        job.cancelAndJoin()

        expectThat(collectedValues).containsExactly(valuesToInsert)
    }

    @Test
    fun `test updating and getting as flow`() = runTest {
        val stringKey = stringPreferencesKey(name = "keyA")
        val collectedValues = mutableListOf<String?>()
        val job = launch(UnconfinedTestDispatcher()) {
            preferencesHelper
                .getAsFlow(stringKey)
                .collect {
                    collectedValues.add(it)
                }
        }

        val expectedValues = mutableListOf<String>()
        repeat(times = 5) {
            val savedValue = preferencesHelper.update(stringKey, default = "a") { current ->
                "${current}a"
            }

            expectedValues.add(savedValue)
        }

        runCurrent()
        job.cancelAndJoin()

        expectThat(collectedValues).containsExactly(listOf(null) + expectedValues)
    }

    @Test
    fun `test deleting and getting as flow`() = runTest {
        val stringKey = stringPreferencesKey(name = "keyA")
        val collectedValues = mutableListOf<String?>()
        val job = launch(UnconfinedTestDispatcher()) {
            preferencesHelper
                .getAsFlow(stringKey)
                .collect {
                    collectedValues.add(it)
                }
        }

        val expectedValues = listOf(null, "1", "2", "3", null)
        expectedValues
            .forEach {
                if (it != null) {
                    preferencesHelper.save(stringKey, it)
                } else {
                    preferencesHelper.delete(stringKey)
                }
            }

        runCurrent()
        job.cancelAndJoin()

        expectThat(collectedValues).containsExactly(expectedValues)
    }

    @Test
    fun `test deleting a value`() = runTest {
        val booleanKey = booleanPreferencesKey(name = "keyA")

        preferencesHelper.save(booleanKey, true)

        expectThat(preferencesHelper.delete(booleanKey))
            .isNotNull()
            .isTrue()

        expectThat(preferencesHelper.get(booleanKey))
            .isNull()

        expectThat(preferencesHelper.delete(booleanKey))
            .isNull()
    }

    @Test
    fun `test deleting a value conditionally`() = runTest {
        val keyA = intPreferencesKey(name = "keyA")
        val keyB = intPreferencesKey(name = "keyB")

        preferencesHelper.batch {
            save(keyA, 1)
            save(keyB, 2)
        }

        val isEqualToOne = { num: Int -> num == 1 }

        val keyADeletedValue = preferencesHelper.deleteIf(keyA, isEqualToOne)
        val keyBDeletedValue = preferencesHelper.deleteIf(keyB, isEqualToOne)

        expectThat(preferencesHelper.get(keyA)).isNull()
        expectThat(keyADeletedValue).isEqualTo(1)

        expectThat(preferencesHelper.get(keyB)).isEqualTo(2)
        expectThat(keyBDeletedValue).isNull()
    }

    @Test
    fun `test batch operations`() = runTest {
        val floatKey = floatPreferencesKey(name = "keyA")
        val doubleKey = doublePreferencesKey(name = "keyB")
        val setKey = stringSetPreferencesKey(name = "keyC")
        val intKey = intPreferencesKey(name = "keyD")
        val stringKey = stringPreferencesKey(name = "keyE")

        preferencesHelper.batch {
            save(doubleKey, 2.0)
            save(intKey, 1)
            save(stringKey, "hi")
        }

        var deletedDouble: Double? = null
        var updatedSet: Set<String> = emptySet()
        var deletedInt: Int? = null
        var deletedString: String? = null

        preferencesHelper.batch {
            save(floatKey, 3f)
            deletedDouble = delete(doubleKey)
            updatedSet = update(setKey, default = emptySet()) { set ->
                set.plus("string")
            }

            deletedInt = deleteIf(intKey) { it == 1 }
            deletedString = deleteIf(stringKey) { it == "hey" }
        }

        expectThat(preferencesHelper.get(floatKey)).isEqualTo(3f)

        expectThat(preferencesHelper.get(doubleKey)).isNull()
        expectThat(deletedDouble).isEqualTo(2.0)

        expectThat(preferencesHelper.get(setKey)).isEqualTo(updatedSet)

        expectThat(preferencesHelper.get(intKey)).isNull()
        expectThat(deletedInt).isEqualTo(1)

        expectThat(preferencesHelper.get(stringKey)).isEqualTo("hi")
        expectThat(deletedString).isNull()
    }

    @Test
    fun `test clearing preferences`() = runTest {
        val keyA = intPreferencesKey(name = "keyA")
        val keyB = intPreferencesKey(name = "keyB")
        val keyC = intPreferencesKey(name = "keyC")

        preferencesHelper.batch {
            save(keyA, 1)
            save(keyB, 2)
            save(keyC, 3)
        }

        preferencesHelper.clear()

        expectThat(preferencesHelper.get(keyA)).isNull()
        expectThat(preferencesHelper.get(keyB)).isNull()
        expectThat(preferencesHelper.get(keyC)).isNull()
    }

    @Test
    fun `test clearing preferences while keeping`() = runTest {
        val keyA = intPreferencesKey(name = "keyA")
        val keyB = intPreferencesKey(name = "keyB")
        val keyC = intPreferencesKey(name = "keyC")

        preferencesHelper.batch {
            save(keyA, 1)
            save(keyB, 2)
            save(keyC, 3)
        }

        preferencesHelper.clearButKeep(keyB)

        expectThat(preferencesHelper.get(keyA)).isNull()
        expectThat(preferencesHelper.get(keyB)).isEqualTo(2)
        expectThat(preferencesHelper.get(keyC)).isNull()
    }

    @Test
    fun `test get or fallback`() = runTest {
        val key = intPreferencesKey(name = "keyA")

        expectThat(preferencesHelper.getOrFallback(key, fallback = 1)).isEqualTo(1)
        expectThat(preferencesHelper.getOrFallback(key, fallback = 2)).isEqualTo(2)
    }

    @Test
    fun `test get or put`() = runTest {
        val key = intPreferencesKey(name = "keyA")

        expectThat(preferencesHelper.getOrPut(key, default = 1)).isEqualTo(1)
        expectThat(preferencesHelper.getOrPut(key, default = 2)).isEqualTo(1)
    }

    @Test
    fun `test get and save with transformations`() = runTest {
        val keyA = longPreferencesKey(name = "keyA")
        val keyB = longPreferencesKey(name = "keyB")

        val dateA = Date()
        val dateB = Date(dateA.time + 10)

        preferencesHelper.saveWith(keyA) { dateA.time }

        preferencesHelper.batch {
            expectThat(get(keyA, ::Date))
                .isEqualTo(dateA)

            saveWith(keyB) { dateB.time }
        }

        expectThat(preferencesHelper.get(keyB, ::Date))
            .isEqualTo(dateB)
    }
}