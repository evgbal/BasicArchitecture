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
import ru.otus.basicarchitecture.net.CityResponse
import javax.inject.Inject

private const val UNKNOWN_ERROR = "Unknown error"
private const val TAG = "AddressViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddressViewModel @Inject constructor(
    private val wizardCache: WizardCache,
    private val addressSuggestUsecase: AddressSuggestUsecase
) : ViewModel() {
    val country = wizardCache.country
    val city = wizardCache.city
    val address = wizardCache.address

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
                addressSuggestUsecase.loadAddressSuggestions(query)
                    .catch { e -> _uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR) }
                    .collect { result ->
                        _addressSuggestions.value = result.map {
                            ("${it.street_with_type} ${it.house_type}" +
                             " ${it.house} ${it.flat_type} ${it.flat}")
                                .trim().trimEnd().trimStart()
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
                addressSuggestUsecase.loadCountries(query)
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
                addressSuggestUsecase.loadCities(query)
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

    fun updateCountry(value: String) {
        wizardCache.country.value = value
    }

    fun updateCity(value: String) {
        wizardCache.city.value = value
    }

    fun updateAddress(value: String) {
        wizardCache.address.value = value
    }
}