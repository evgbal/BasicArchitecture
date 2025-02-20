package ru.otus.basicarchitecture.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Делаем доступным на уровне всего приложения
object DatabaseModule {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Проверяем наличие колонок и добавляем, если их нет
            val cursor = db.query("PRAGMA table_info(address_cache)")
            val existingColumns = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                existingColumns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
            cursor.close()

            if (!existingColumns.contains("block_type")) {
                db.execSQL("ALTER TABLE address_cache ADD COLUMN block_type TEXT")
            }
            if (!existingColumns.contains("block")) {
                db.execSQL("ALTER TABLE address_cache ADD COLUMN block TEXT")
            }
            if (!existingColumns.contains("geoLat")) {
                db.execSQL("ALTER TABLE address_cache ADD COLUMN geoLat TEXT")
            }
            if (!existingColumns.contains("geoLon")) {
                db.execSQL("ALTER TABLE address_cache ADD COLUMN geoLon TEXT")
            }
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).addMigrations(MIGRATION_1_2)
            .build()
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
