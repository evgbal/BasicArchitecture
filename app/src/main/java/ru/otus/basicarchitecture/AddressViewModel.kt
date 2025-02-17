package ru.otus.basicarchitecture

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.otus.basicarchitecture.data.ConfirmedAddress
import ru.otus.basicarchitecture.data.WizardCache
import ru.otus.basicarchitecture.net.CityResponse
import ru.otus.basicarchitecture.usecase.AddressSuggestUseCase
import ru.otus.basicarchitecture.usecase.CitiesSuggrestUseCase
import ru.otus.basicarchitecture.usecase.CityByIpUseCase
import ru.otus.basicarchitecture.usecase.ClearOldCacheUseCase
import ru.otus.basicarchitecture.usecase.CountriesSuggrestUseCase
import javax.inject.Inject

private const val UNKNOWN_ERROR = "Unknown error"
private const val TAG = "AddressViewModel"
private const val CACHE_LIFE_TIME_IN_MILLISECONDS = 7 * 24 * 60 * 60 * 1000

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddressViewModel @Inject constructor(
    private val wizardCache: WizardCache,
    private val addressSuggestUseCase: AddressSuggestUseCase,
    private val citiesSuggrestUseCase: CitiesSuggrestUseCase,
    private val countriesSuggrestUseCase: CountriesSuggrestUseCase,
    private val loadCityByIpUseCase: CityByIpUseCase,
    private val clearOldCacheUseCase: ClearOldCacheUseCase

) : ViewModel() {
    val country = wizardCache.country
    val city = wizardCache.city
    val address = wizardCache.address
    val confirmedAddress = wizardCache.confirmedAddress

    init {
        viewModelScope.launch {
            launch { clearOldCache(System.currentTimeMillis() - CACHE_LIFE_TIME_IN_MILLISECONDS) }
        }
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<String>>(emptyList())
    val addressSuggestions: StateFlow<List<String>> = _addressSuggestions.asStateFlow()

    private var addressSuggestJob: Job? = null

    fun loadAddressSuggestions(query: String) {
        // Минимальное количество символов перед запросом
        if (query.isEmpty()) {
            _citySuggestions.value = listOf()
            return
        }
        addressSuggestJob?.cancel()
        addressSuggestJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                addressSuggestUseCase.execute(query)
                    .catch { e -> _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR) }
                    .collect { result ->
                        _addressSuggestions.value = result.map {
                            val fullAddress = ConfirmedAddress(
                                country = it.country,
                                city = it.city_with_type ?: it.region_with_type ?: "",
                                streetWithHouseAndFlat = ("${it.street_with_type} ${it.house_type}" +
                                        " ${it.house} ${it.flat_type} ${it.flat}")
                                    .trim().trimEnd().trimStart()
                            )
                            updateConfirmedAddress(fullAddress)

                            fullAddress.streetWithHouseAndFlat
                        }
                        _uiState.value = UiState.Success
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR)
            }
        }
    }

    private val _countrySuggestions = MutableStateFlow<List<String>>(emptyList())
    val countrySuggestions: StateFlow<List<String>> = _countrySuggestions.asStateFlow()

    private var countrySuggestJob: Job? = null

    fun loadCountries(query: String) {
        // Минимальное количество символов перед запросом
        if (query.isEmpty()) {
            _citySuggestions.value = listOf()
            return
        }
        countrySuggestJob?.cancel()
        countrySuggestJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                countriesSuggrestUseCase.execute(query)
                    .catch { e ->
                        _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR)
                        Log.d(TAG, e.message + "; " + e.stackTraceToString())
                    }
                    .collect { result ->
                        _countrySuggestions.value = result
                        _uiState.value = UiState.Success
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR)
            }
        }
    }


    private val _citySuggestions = MutableStateFlow<List<CityResponse>>(emptyList())
    val citySuggestions: StateFlow<List<CityResponse>> = _citySuggestions.asStateFlow()

    private var citySuggestJob: Job? = null

    fun loadCities(query: String) {
        // Минимальное количество символов перед запросом
        if (query.isEmpty()) {
            _citySuggestions.value = listOf()
            return
        }
        citySuggestJob?.cancel()
        citySuggestJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                citiesSuggrestUseCase.execute(query)
                    .catch { e -> _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR) }
                    .collect { result ->
                        _citySuggestions.value = result
                        _uiState.value = UiState.Success
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR)
            }
        }
    }

    fun loadCityByIp() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                loadCityByIpUseCase.execute()
                    .catch { e -> _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR) }
                    .collect { result ->
                        result.location?.let {
                            updateCity(it.value)
                            updateCountry(it.data.country)
                        }
                        _uiState.value = UiState.Success
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR)
            }
        }
    }

    fun clearOldCache(expiryTime: Long) {
        viewModelScope.launch {
            try {
                clearOldCacheUseCase.execute(expiryTime)
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache: ${e.message}")
            }
        }
    }

    fun updateCountry(value: String) {
        wizardCache.country.value = value
    }

    fun updateCity(value: String) {
        wizardCache.city.value = value
    }

    fun updateAddress(value: String) {
        wizardCache.address.value = value
    }

    fun updateConfirmedAddress(value: ConfirmedAddress) {
        wizardCache.confirmedAddress.value = value
    }
}