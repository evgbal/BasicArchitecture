package ru.otus.basicarchitecture

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.otus.basicarchitecture.net.AddressResponse
import ru.otus.basicarchitecture.net.CityData
import ru.otus.basicarchitecture.net.CityLocation
import ru.otus.basicarchitecture.net.CityResponse
import ru.otus.basicarchitecture.net.DaDataService
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AddressSuggestUsecase @Inject constructor(
    private val daDataService: DaDataService
) {
    fun loadAddressSuggestions(query: String): Flow<List<AddressResponse>> {
        return daDataService.getAddressSuggestions(query)
            .flowOn(Dispatchers.IO) // Обработка в IO потоке
    }
    fun loadCountries(query: String): Flow<List<String>> {
        return daDataService.getCountries(query)
            .flowOn(Dispatchers.IO) // Обработка в IO потоке
    }
    fun loadCities(query: String): Flow<List<CityResponse>> {
        return daDataService.getAddressSuggestions(query)
            .flatMapConcat { addresses ->
                flow {
                    val cities = addresses.mapNotNull { address ->
                        val city = address.city_with_type ?: address.region_with_type
                        val country = address.country
                        if (!city.isNullOrBlank() && country.isNotBlank()) {
                            CityResponse(location = CityLocation(value = city, data = CityData(country = country)))
                        } else {
                            null
                        }
                    }.distinctBy { it.location?.value ?: "" } // Убираем дубли по названию города
                    emit(cities)
                }
            }
            .flowOn(Dispatchers.IO)
    }
    fun loadCityByIp(): Flow<CityResponse> {
        return daDataService.getCityByIp("")
            .flowOn(Dispatchers.IO) // Обработка в IO потоке
    }
}