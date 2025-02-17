package ru.otus.basicarchitecture.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Делаем доступным на уровне всего приложения
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideAddressDao(database: AppDatabase): AddressCacheDao {
        return database.addressCacheDao()
    }

    @Provides
    fun provideCountryDao(database: AppDatabase): CountryCacheDao {
        return database.countryCacheDao()
    }

    @Provides
    fun provideCityDao(database: AppDatabase): CityCacheDao {
        return database.cityCacheDao()
    }
}
