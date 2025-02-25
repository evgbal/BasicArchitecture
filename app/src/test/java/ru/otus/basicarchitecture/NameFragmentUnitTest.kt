package ru.otus.basicarchitecture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, sdk = [34])
class NameFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun validateBirthDate_validDate_returnsTrue() {
        val fragment = NameFragment()
        assertTrue(fragment.validateBirthDate("15.08.1990"))
    }

    @Test
    fun validateBirthDate_invalidDate_returnsFalse() {
        val fragment = NameFragment()
        assertFalse(fragment.validateBirthDate("32.13.2020"))
    }

    @Test
    fun validateBirthDate_under18_returnsFalse() {
        val fragment = NameFragment()
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -17) // Младше 18 лет
        val date = sdf.format(calendar.time)
        assertFalse(fragment.validateBirthDate(date))
    }

    @Test
    fun validateBirthDate_exactly18_returnsTrue() {
        val fragment = NameFragment()
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        val date = sdf.format(calendar.time)
        assertTrue(fragment.validateBirthDate(date))
    }
}
