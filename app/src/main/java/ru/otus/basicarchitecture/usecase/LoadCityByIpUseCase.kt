package ru.otus.basicarchitecture.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import ru.otus.basicarchitecture.net.CityResponse
import ru.otus.basicarchitecture.net.DaDataService
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class CityByIpUseCase @Inject constructor(
    private val daDataService: DaDataService
) {
    fun execute(): Flow<CityResponse> = daDataService.getCityByIp("").flowOn(Dispatchers.IO)
}