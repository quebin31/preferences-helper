package com.example.datastorehelper

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import com.example.datastorehelper.test.CoroutinesTestRule
import com.example.preferenceshelper.PreferencesHelper
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
        val collectedValues = mutableListOf<String>()
        val job = launch(UnconfinedTestDispatcher()) {
            preferencesHelper
                .getAsFlow(stringKey)
                .filterNotNull()
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
    fun `test batch operations`() = runTest {
        val floatKey = floatPreferencesKey(name = "keyA")
        val doubleKey = doublePreferencesKey(name = "keyB")
        val setKey = stringSetPreferencesKey(name = "keyC")

        preferencesHelper.save(doubleKey, 2.0)
        var deletedDouble: Double? = null
        var updatedSet: Set<String> = emptySet()

        preferencesHelper.batch {
            save(floatKey, 3f)
            deletedDouble = delete(doubleKey)
            updatedSet = update(setKey, default = emptySet()) { set ->
                set.plus("string")
            }
        }

        expectThat(preferencesHelper.get(floatKey)).isEqualTo(3f)
        expectThat(preferencesHelper.get(doubleKey)).isNull()
        expectThat(deletedDouble).isEqualTo(2.0)
        expectThat(preferencesHelper.get(setKey)).isEqualTo(updatedSet)
    }

    @Test
    fun `test clearing preferences`() = runTest {
        val keyA = intPreferencesKey(name = "keyA")
        val keyB = intPreferencesKey(name = "keyB")
        val keyC = intPreferencesKey(name = "keyC")

        preferencesHelper.clear()

        expectThat(preferencesHelper.get(keyA)).isNull()
        expectThat(preferencesHelper.get(keyB)).isNull()
        expectThat(preferencesHelper.get(keyC)).isNull()
    }
}