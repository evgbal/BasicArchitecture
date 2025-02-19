package ru.otus.basicarchitecture.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.otus.basicarchitecture.data.AddressCacheDao
import ru.otus.basicarchitecture.data.CityCacheDao
import ru.otus.basicarchitecture.data.CountryCacheDao
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class ClearOldCacheUseCase @Inject constructor(
    private val addressCacheDao: AddressCacheDao,
    private val countryCacheDao: CountryCacheDao,
    private val cityCacheDao: CityCacheDao
) {
    suspend fun execute(expiryTime: Long) {
        addressCacheDao.clearOldCache(expiryTime)
        countryCacheDao.clearOldCache(expiryTime)
        cityCacheDao.clearOldCache(expiryTime)
    }
}
