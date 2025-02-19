package ru.otus.basicarchitecture.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.otus.basicarchitecture.data.CachedCity
import ru.otus.basicarchitecture.data.CityCacheDao
import ru.otus.basicarchitecture.net.AddressResponse
import ru.otus.basicarchitecture.net.CityData
import ru.otus.basicarchitecture.net.CityLocation
import ru.otus.basicarchitecture.net.CityResponse
import ru.otus.basicarchitecture.net.DaDataService
import javax.inject.Inject
import javax.inject.Singleton

private const val EMPTY = "EMPTY"

@ExperimentalCoroutinesApi
@Singleton
class CitiesSuggrestUseCase @Inject constructor(
    private val daDataService: DaDataService,
    private val cityCacheDao: CityCacheDao
) {
    fun execute(query: String): Flow<List<CityResponse>> = flow {
        val cached = cityCacheDao.getCitiesByQuery(query)
        if (!cached.isNullOrEmpty()) {
            // Проверяем наличие "пустого" ответа
            if (cached.any { it.name == EMPTY }) {
                emit(emptyList())
                return@flow
            }
            emit(cached.map { CityResponse(CityLocation(it.name, CityData(it.country))) })
            return@flow
        }

        val response = daDataService.getAddressSuggestions(query).firstOrNull()
        if (!response.isNullOrEmpty()) {
            val cities = response.mapNotNull { it.toCachedCity(query) }.distinctBy { it.name }
            cityCacheDao.saveCities(cities)
            emit(cities.map { CityResponse(CityLocation(it.name, CityData(it.country))) })
        } else {
            // Сохраняем маркер пустого ответа
            cityCacheDao.saveCities(listOf(createEmptyCachedCity(query)))
            emit(listOf())
        }
    }.flowOn(Dispatchers.IO)
}

private fun AddressResponse.toCachedCity(query: String): CachedCity? {
    val city = city_with_type ?: region_with_type
    return if (!city.isNullOrBlank() && country.isNotBlank()) {
        CachedCity(name = city, country = country, query = query)
    } else null
}

// Вспомогательная функция для создания "пустого" ответа
private fun createEmptyCachedCity(query: String) = CachedCity(
    id = 0,
    name = EMPTY,
    country = "",
    query = query
)