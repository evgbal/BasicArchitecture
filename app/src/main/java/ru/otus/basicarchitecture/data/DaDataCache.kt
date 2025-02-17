package ru.otus.basicarchitecture.data


import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "address_cache")
data class CachedAddress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "query") val query: String, // Запрос
    val result: String,
    val country: String,
    val region_with_type: String?,
    val city_with_type: String?,
    val street_with_type: String?,
    val house: String?,
    val house_type: String?,
    val flat: String?,
    val flat_type: String?,
    val geoLat: String?,
    val geoLon: String?,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "country_cache")
data class CachedCountry(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "query") val query: String, // Связь с запросом
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "city_cache")
data class CachedCity(
    @PrimaryKey val id: Long = 0,
    @ColumnInfo(name = "query") val query: String,
    val name: String,
    val country: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AddressCacheDao {
    @Query("SELECT * FROM address_cache WHERE [query] = :query")
    suspend fun getCachedAddresses(query: String): List<CachedAddress>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAddresses(addresses: List<CachedAddress>)

    @Query("DELETE FROM address_cache WHERE timestamp < :expiryTime")
    suspend fun clearOldCache(expiryTime: Long)
}

@Dao
interface CountryCacheDao {
    @Query("SELECT * FROM country_cache WHERE [query] = :query")
    suspend fun getCountriesByQuery(query: String): List<CachedCountry>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCountries(countries: List<CachedCountry>)

    @Query("DELETE FROM country_cache WHERE timestamp < :expiryTime")
    suspend fun clearOldCache(expiryTime: Long)
}

@Dao
interface CityCacheDao {
    @Query("SELECT * FROM city_cache WHERE [query] = :query")
    suspend fun getCitiesByQuery(query: String): List<CachedCity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCities(cities: List<CachedCity>)

    @Query("DELETE FROM city_cache WHERE timestamp < :expiryTime")
    suspend fun clearOldCache(expiryTime: Long)
}

@Database(
      entities = [CachedAddress::class, CachedCountry::class, CachedCity::class]
    , version = 1
    , exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun addressCacheDao(): AddressCacheDao
    abstract fun countryCacheDao(): CountryCacheDao
    abstract fun cityCacheDao(): CityCacheDao
}



//@Singleton
//class CacheCleaner @Inject constructor(
//    private val addressCacheDao: AddressCacheDao,
//    private val countryCacheDao: CountryCacheDao,
//    private val cityCacheDao: CityCacheDao
//) {
//    suspend fun clearOldCache() {
////        val expiryTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1) // 1 день
////        addressCacheDao.clearOldCache(expiryTime)
////        countryCacheDao.clearOldCache(expiryTime)
////        cityCacheDao.clearOldCache(expiryTime)
//    }
//}