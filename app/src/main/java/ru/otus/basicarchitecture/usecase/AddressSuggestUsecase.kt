package ru.otus.basicarchitecture.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.otus.basicarchitecture.data.AddressCacheDao
import ru.otus.basicarchitecture.data.CachedAddress
import ru.otus.basicarchitecture.net.AddressResponse
import ru.otus.basicarchitecture.net.DaDataService
import javax.inject.Inject
import javax.inject.Singleton

//@ExperimentalCoroutinesApi
//class AddressSuggestUsecase @Inject constructor(
//    private val daDataService: DaDataService
//) {
//    fun loadAddressSuggestions(query: String): Flow<List<AddressResponse>> {
//        return daDataService.getAddressSuggestions(query)
//            .flowOn(Dispatchers.IO) // Обработка в IO потоке
//    }
//    fun loadCountries(query: String): Flow<List<String>> {
//        return daDataService.getCountries(query)
//            .flowOn(Dispatchers.IO) // Обработка в IO потоке
//    }
//    fun loadCities(query: String): Flow<List<CityResponse>> {
//        return daDataService.getAddressSuggestions(query)
//            .flatMapConcat { addresses ->
//                flow {
//                    val cities = addresses.mapNotNull { address ->
//                        val city = address.city_with_type ?: address.region_with_type
//                        val country = address.country
//                        if (!city.isNullOrBlank() && country.isNotBlank()) {
//                            CityResponse(location = CityLocation(value = city, data = CityData(country = country)))
//                        } else {
//                            null
//                        }
//                    }.distinctBy { it.location?.value ?: "" } // Убираем дубли по названию города
//                    emit(cities)
//                }
//            }
//            .flowOn(Dispatchers.IO)
//    }
//    fun loadCityByIp(): Flow<CityResponse> {
//        return daDataService.getCityByIp("")
//            .flowOn(Dispatchers.IO) // Обработка в IO потоке
//    }
//}

/*
@ExperimentalCoroutinesApi
class AddressSuggestUsecase @Inject constructor(
    private val daDataService: DaDataService,
    private val addressCacheDao: AddressCacheDao,
    private val countryCacheDao: CountryCacheDao,
    private val cityCacheDao: CityCacheDao
) {
    fun loadAddressSuggestions(query: String): Flow<List<AddressResponse>> = flow {
        val cached = addressCacheDao.getCachedAddresses(query)
        if (!cached.isNullOrEmpty()) {
            emit(cached.map {
                AddressResponse(
                    result = it.result,
                    country = it.country,
                    region_with_type = it.region_with_type,
                    city_with_type = it.city_with_type,
                    street_with_type = it.street_with_type,
                    house_type = it.house_type,
                    house = it.house,
                    flat_type = it.flat_type,
                    flat = it.flat,
                    geo_lat = it.geoLat,
                    geo_lon = it.geoLon
                )
            })
            return@flow
        }

        val response = daDataService.getAddressSuggestions(query).firstOrNull()
        if (!response.isNullOrEmpty()) {
            addressCacheDao.saveAddresses(response.map {
                CachedAddress(
                    query = query,
                    result = it.result,
                    country = it.country,
                    region_with_type = it.region_with_type,
                    city_with_type = it.city_with_type,
                    street_with_type = it.street_with_type,
                    house_type = it.house_type,
                    house = it.house,
                    flat_type = it.flat_type,
                    flat = it.flat,
                    geoLat = it.geo_lat,
                    geoLon = it.geo_lon
                )
            })
            emit(response)
        }
    }.flowOn(Dispatchers.IO)

    fun loadCountries(query: String): Flow<List<String>> = flow {
        val cached = countryCacheDao.getCountriesByQuery(query)
        if (!cached.isNullOrEmpty()) {
            emit(cached.map { it.name })
            return@flow
        }

        val response = daDataService.getCountries(query).firstOrNull()
        if (!response.isNullOrEmpty()) {
            countryCacheDao.saveCountries(response.map {
                CachedCountry(name = it, query = query)
            })
            emit(response)
        }
    }.flowOn(Dispatchers.IO)

    fun loadCities(query: String): Flow<List<CityResponse>> = flow {
        val cached = cityCacheDao.getCitiesByQuery(query)
        if (!cached.isNullOrEmpty()) {
            emit(cached.map { CityResponse(CityLocation(it.name, CityData(it.country))) })
            return@flow
        }

        val response = daDataService.getAddressSuggestions(query).firstOrNull()
        if (!response.isNullOrEmpty()) {
            val cities = response.mapNotNull { address ->
                val city = address.city_with_type ?: address.region_with_type
                val country = address.country
                if (!city.isNullOrBlank() && country.isNotBlank()) {
                    CachedCity(name = city, country = country, query = query)
                } else null
            }.distinctBy { it.name }

            cityCacheDao.saveCities(cities)
            emit(cities.map { CityResponse(CityLocation(it.name, CityData(it.country))) })
        }
    }.flowOn(Dispatchers.IO)

    suspend fun clearOldCache(expiryTime: Long) {
        addressCacheDao.clearOldCache(expiryTime)
        countryCacheDao.clearOldCache(expiryTime)
        cityCacheDao.clearOldCache(expiryTime)
    }

    fun loadCityByIp(): Flow<CityResponse> {
        return daDataService.getCityByIp("")
            .flowOn(Dispatchers.IO) // Обработка в IO потоке
    }
}
*/

@ExperimentalCoroutinesApi
@Singleton
class AddressSuggestUseCase @Inject constructor(
    private val daDataService: DaDataService,
    private val addressCacheDao: AddressCacheDao
) {
    fun execute(query: String): Flow<List<AddressResponse>> = flow {
        val cached = addressCacheDao.getCachedAddresses(query)
        if (!cached.isNullOrEmpty()) {
            emit(cached.map { it.toAddressResponse() })
            return@flow
        }

        val response = daDataService.getAddressSuggestions(query).firstOrNull()
        if (!response.isNullOrEmpty()) {
            addressCacheDao.saveAddresses(response.map { it.toCachedAddress(query) })
            emit(response)
        }
    }.flowOn(Dispatchers.IO)
}

private fun CachedAddress.toAddressResponse() = AddressResponse(
    result = result,
    country = country,
    region_with_type = region_with_type,
    city_with_type = city_with_type,
    street_with_type = street_with_type,
    house_type = house_type,
    house = house,
    flat_type = flat_type,
    flat = flat,
    geo_lat = geoLat,
    geo_lon = geoLon
)

private fun AddressResponse.toCachedAddress(query: String) = CachedAddress(
    query = query,
    result = result,
    country = country,
    region_with_type = region_with_type,
    city_with_type = city_with_type,
    street_with_type = street_with_type,
    house_type = house_type,
    house = house,
    flat_type = flat_type,
    flat = flat,
    geoLat = geo_lat,
    geoLon = geo_lon
)