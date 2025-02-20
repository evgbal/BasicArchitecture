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


private const val EMPTY = "EMPTY"

@ExperimentalCoroutinesApi
@Singleton
class AddressSuggestUseCase @Inject constructor(
    private val daDataService: DaDataService,
    private val addressCacheDao: AddressCacheDao
) {
    fun execute(query: String): Flow<List<AddressResponse>> = flow {
        val cached = addressCacheDao.getCachedAddresses(query)
        if (cached.isNullOrEmpty()) {
            val response = daDataService.getAddressSuggestions(query).firstOrNull()
            if (!response.isNullOrEmpty()) {
                addressCacheDao.saveAddresses(response.map { it.toCachedAddress(query) })
                if (response.size == 1) {
                    addressCacheDao.saveAddresses(response.map { it.toCachedAddress(it.result ?: "") })
                }
                emit(response)
            } else {
                // Сохраняем маркер пустого ответа
                addressCacheDao.saveAddresses(listOf(createEmptyCachedAddress(query)))
                emit(listOf())
            }
        } else {
            // Если найден "пустой" кэш, вернуть пустой список
            if (cached.any { it.result == EMPTY }) {
                emit(emptyList())
            } else {
                emit(cached.map { it.toAddressResponse() })
            }
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
    block_type = block_type,
    block = block
)

private fun AddressResponse.toCachedAddress(query: String) = CachedAddress(
    query = query,
    result = result ?: "",
    country = country ?: "",
    region_with_type = region_with_type,
    city_with_type = city_with_type,
    street_with_type = street_with_type,
    house_type = house_type,
    house = house,
    flat_type = flat_type,
    flat = flat,
    geoLat = null,
    geoLon = null,
    block_type = block_type,
    block = block
)

// Вспомогательная функция для создания "пустого" ответа
private fun createEmptyCachedAddress(query: String) = CachedAddress(
    query = query,
    result = EMPTY,
    country = "",
    region_with_type = null,
    city_with_type = null,
    street_with_type = null,
    house = null,
    house_type = null,
    flat = null,
    flat_type = null,
    geoLat = null,
    geoLon = null,
    block_type = null,
    block = null
)