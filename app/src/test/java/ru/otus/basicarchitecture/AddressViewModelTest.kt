package ru.otus.basicarchitecture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import ru.otus.basicarchitecture.data.WizardCache
import ru.otus.basicarchitecture.net.AddressResponse
import ru.otus.basicarchitecture.net.CityData
import ru.otus.basicarchitecture.net.CityLocation
import ru.otus.basicarchitecture.net.CityResponse
import ru.otus.basicarchitecture.usecase.AddressSuggestUseCase
import ru.otus.basicarchitecture.usecase.CitiesSuggrestUseCase
import ru.otus.basicarchitecture.usecase.CityByIpUseCase
import ru.otus.basicarchitecture.usecase.ClearOldCacheUseCase
import ru.otus.basicarchitecture.usecase.CountriesSuggrestUseCase


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
@Config(application = HiltTestApplication::class, sdk = [34])
class AddressViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var wizardCache: WizardCache

    @Mock
    private lateinit var addressSuggestUseCase: AddressSuggestUseCase

    @Mock
    private lateinit var citiesSuggestUseCase: CitiesSuggrestUseCase

    @Mock
    private lateinit var countriesSuggestUseCase: CountriesSuggrestUseCase

    @Mock
    private lateinit var cityByIpUseCase: CityByIpUseCase

    @Mock
    private lateinit var clearOldCacheUseCase: ClearOldCacheUseCase

    private lateinit var viewModel: AddressViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        wizardCache = WizardCache()
        viewModel = AddressViewModel(
            wizardCache,
            addressSuggestUseCase,
            citiesSuggestUseCase,
            countriesSuggestUseCase,
            cityByIpUseCase,
            clearOldCacheUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAddressSuggestions emits success state with data`() = runTest {
        val mockAddress = AddressResponse(
            "Country, City, Street h 3, kv 4 k 3"
            , "Country"
            , "City"
            , "City"
            , "Street"
            , "h"
            , "3"
            , "kv"
            , "4"
            , "k"
            , "3")

        doReturn(flowOf(listOf(mockAddress)))
            .whenever(addressSuggestUseCase)
            .execute("test")

        viewModel.loadAddressSuggestions("test")
        advanceUntilIdle() // Process all coroutines

        viewModel.uiState.test {
            assertEquals(UiState.Success, awaitItem())
        }
    }

    @Test
    fun `loadAddressSuggestions emits error state on failure`() = runTest {
        Mockito.`when`(addressSuggestUseCase.execute("test"))
            .thenThrow(RuntimeException("Network error"))

        viewModel.loadAddressSuggestions("test")
        advanceUntilIdle() // Process all coroutines

        viewModel.uiState.test {
            while (true) {
                val item = awaitItem()
                if (item !is UiState.Loading) {
                    val errorState = item as UiState.Error
                    assertEquals("Network error", errorState.message)
                    break
                }
            }
        }
    }

    @Test
    fun `loadCountries emits success state`() = runTest {
        // Arrange: Mock the use case to return a Flow with "Russia"
        doReturn(flowOf(listOf("Russia")))
            .whenever(countriesSuggestUseCase)
            .execute("ru")
        viewModel.loadCountries("ru")
        advanceUntilIdle() // Process all coroutines
        viewModel.uiState.test {
           while (true) {
                val item = awaitItem()
                if (item !is UiState.Loading) {
                    assertEquals(UiState.Success, item)
                    break
                }
            }
        }
    }


    @Test
    fun `loadCountries emits error state`() = runTest {
        Mockito.`when`(countriesSuggestUseCase.execute("test"))
            .thenThrow(RuntimeException("Network error"))

        viewModel.loadCountries("test")
        advanceUntilIdle() // Process all coroutines

        viewModel.uiState.test {
            while (true) {
                val item = awaitItem()
                if (item !is UiState.Loading) {
                    val errorState = item as UiState.Error
                    assertEquals("Network error", errorState.message)
                    break
                }
            }
        }
    }


    @Test
    fun `loadCityByIp success`() = runTest {
        val mockResponse = CityResponse(CityLocation("Moscow", CityData("Russia")))
        doReturn(flowOf(mockResponse))
            .whenever(cityByIpUseCase)
            .execute()
        viewModel.loadCityByIp()
        advanceUntilIdle() // Process all coroutines
        viewModel.uiState.test {
            while (true) {
                val item = awaitItem()
                if (item !is UiState.Loading) {
                    assertEquals(UiState.Success, item)
                    break
                }
            }
        }
    }

    @Test
    fun `loadCityByIp emits handles error`() = runTest {
        Mockito.`when`(cityByIpUseCase.execute())
            .thenThrow(RuntimeException("IP error"))
        viewModel.loadCityByIp()
        advanceUntilIdle() // Process all coroutines
        viewModel.uiState.test {
            while (true) {
                val item = awaitItem()
                if (item !is UiState.Loading) {
                    val errorState = item as UiState.Error
                    assertEquals("IP error", errorState.message)
                    break
                }
            }
        }
    }
}
