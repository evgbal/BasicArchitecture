package ru.otus.basicarchitecture.net

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor {
        return AuthInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Добавляем наш интерцептор
            .build()
    }

    @Provides
    @Singleton
    @Named("cleaner")
    fun provideCleanerRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://cleaner.dadata.ru")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    @Named("suggestions")
    fun provideSuggestionsRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://suggestions.dadata.ru")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideCleanerApi(@Named("cleaner") retrofit: Retrofit): CleanerApi {
        return retrofit.create(CleanerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSuggestionsApi(@Named("suggestions") retrofit: Retrofit): SuggestionsApi {
        return retrofit.create(SuggestionsApi::class.java)
    }
}
