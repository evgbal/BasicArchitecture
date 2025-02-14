package ru.otus.basicarchitecture.net

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/*

Поиск страны:

запрос:
curl -X POST -H "Content-Type: application/json" -H "Accept: application/json" -H "Authorization: Token aabb123456789" -d "{ \"query\": \"TH\" }" "http://suggestions.dadata.ru/suggestions/api/4_1/rs/findById/country" -o dadata.countries.res.json

ответ:

{
  "suggestions": [
    {
      "value": "Таиланд"
    }
  ]
}

Поиск адреса:

запрос:
curl -X POST -H "Content-Type: application/json" -H "Accept: application/json" -H "Authorization: Token aabb123456789" -H "X-Secret: aabb9123456789" -d "[ \"Тайланд, Пхукет, Главный пр. 131 кв. 12\" ]" "https://cleaner.dadata.ru/api/v1/clean/address" -o dadata.address.res.json

ответ:
[
  {
    "result": "г Пхукет, пр-кт Главный, д 131, кв 12",
    "country": "Тайланд",
    "region_with_type": "г Пхукет",
    "city_with_type": "г Пхукет",
    "street_with_type": "пр-кт Главный",
    "house_type": "д",
    "house": "131",
    "flat_type": "кв",
    "flat": "12",
    "geo_lat": "29.8514164",
    "geo_lon": "40.2739338"
  }
]


Поиск города по IP адресу:

запрос:
curl -X GET -H "Accept: application/json" -H "Authorization: Token aabb123456789" "http://suggestions.dadata.ru/suggestions/api/4_1/rs/iplocate/address?ip=" -o dadata.cities-empty-ip.res.json

ответ:
{
  "location": {
    "value": "г Пхукет",
    "data": {
      "country": "Тайланд"
    }
  }
}

 */


interface SuggestionsApi {
    @POST("suggestions/api/4_1/rs/suggest/country")
    suspend fun findCountry(@Body request: Map<String, String>): CountryResponse

    @GET("suggestions/api/4_1/rs/iplocate/address")
    suspend fun findCityByIp(@Query("ip") ip: String): CityResponse
}

interface CleanerApi {
    @POST("api/v1/clean/address")
    suspend fun cleanAddress(@Body request: List<String>): List<AddressResponse>
}

private const val QUERY = "query"

@Singleton
class DaDataService @Inject constructor(
    private val suggestionsApi: SuggestionsApi,
    private val cleanerApi: CleanerApi
) {
    fun getCountries(query: String): Flow<List<String>> = flow {
        //val response = suggestionsApi.findCountry(mapOf(QUERY to query))

        val response = CountryResponse(
            listOf(
                CountrySuggestion("Россия"),
                CountrySuggestion("Беларусь"),
                CountrySuggestion("Грузия"),
                CountrySuggestion("Казахстан"),
                CountrySuggestion("ОАЭ")
            ).filter { it.value.contains(query, ignoreCase = true) }
        )
        emit(response.suggestions.map { it.value })
    }

    fun getAddressSuggestions(query: String): Flow<List<AddressResponse>> = flow {
        //val response = cleanerApi.cleanAddress(listOf(query))

        val response = listOf(
            AddressResponse(
                "Россия, г Санкт-Петербург, пр-кт Невский д 123 кв 45",
                "Россия",
                "г Санкт-Петербург",
                null,
                "пр-кт Невский",
                "д",
                "123",
                "кв",
                "45",
                "33.0",
                "45.0"
            )
            , AddressResponse(
                "Россия, г Санкт-Петербург, пр-кт Невский д 12 кв 34",
                "Россия",
                "г Санкт-Петербург",
                null,
                "пр-кт Невский",
                "д",
                "12",
                "кв",
                "34",
                "33.0",
                "45.0"
            )
            , AddressResponse(
                "Россия, г Санкт-Петербург, пр-кт Староневский д 12 кв 34",
                "Россия",
                "г Санкт-Петербург",
                null,
                "пр-кт Староневский",
                "д",
                "12",
                "кв",
                "34",
                "33.0",
                "45.0"
            )
            , AddressResponse(
                "Россия, г Москва, пр-т Ленинский д 123 кв 45",
                "Россия",
                "г Москва",
                null,
                "пр-т Ленинский",
                "д",
                "123",
                "кв",
                "45",
                "45.0",
                "46.0"
            ), AddressResponse(
                "Россия, г Сочи, ул Прибрежная д 123 кв 45",
                "Россия",
                "г Сочи",
                null,
                "ул Прибрежная",
                "д",
                "123",
                "кв",
                "45",
                "55.0",
                "51.0"
            )
        ).filter {
            it.result
                .replace(", г ", ", ")
                .replace(", пр-кт ", ", ")
                .contains(
                    query
                        .replace(", г ", ", ")
                        .replace(", пр-кт ", ", "), ignoreCase = true
                )
        }
        emit(response)
    }

    fun getCityByIp(ip: String): Flow<CityResponse> = flow {
        //val response = suggestionsApi.findCityByIp(ip)
        val response = CityResponse(
            CityLocation(
                "г Санкт-Петербург", CityData("Россия")
            )
        )
        emit(response)
    }
}

// Модели ответов
data class CountryResponse(val suggestions: List<CountrySuggestion>)
data class CountrySuggestion(val value: String)

data class AddressResponse(
    val result: String,
    val country: String,
    val region_with_type: String?,
    val city_with_type: String?,
    val street_with_type: String?,
    val house_type: String?,
    val house: String?,
    val flat_type: String?,
    val flat: String?,
    val geo_lat: String?,
    val geo_lon: String?
)

data class CityResponse(val location: CityLocation?)
data class CityLocation(val value: String, val data: CityData)
data class CityData(val country: String)

