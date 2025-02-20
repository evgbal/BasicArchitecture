package ru.otus.basicarchitecture.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.otus.basicarchitecture.data.CachedCountry
import ru.otus.basicarchitecture.data.CountryCacheDao
import ru.otus.basicarchitecture.net.DaDataService
import javax.inject.Inject
import javax.inject.Singleton


private const val EMPTY = "EMPTY"

@ExperimentalCoroutinesApi
@Singleton
class CountriesSuggrestUseCase @Inject constructor(
    private val daDataService: DaDataService,
    private val countryCacheDao: CountryCacheDao
) {
    fun execute(query: String): Flow<List<String>> = flow {
        val cached = countryCacheDao.getCountriesByQuery(query)
        if (cached.isNullOrEmpty()) {
            val response = daDataService.getCountries(query).firstOrNull()
            if (!response.isNullOrEmpty()) {
                countryCacheDao.saveCountries(response.map { CachedCountry(it, query) })
                emit(response)
            } else {
                // Сохраняем маркер пустого ответа
                countryCacheDao.saveCountries(listOf(createEmptyCachedCountry(query)))
                emit(listOf())
            }
        } else {
            // Проверяем наличие "пустого" ответа
            if (cached.any { it.name == EMPTY }) {
                emit(emptyList())
            }
            emit(cached.map { it.name })
        }
    }.flowOn(Dispatchers.IO)
}

// Вспомогательная функция для создания "пустого" ответа
private fun createEmptyCachedCountry(query: String) = CachedCountry(
    name = EMPTY,
    query = query
)