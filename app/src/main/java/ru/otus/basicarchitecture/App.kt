package ru.otus.basicarchitecture

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    // Delete when https://github.com/google/dagger/issues/3601 is resolved.
    @Inject @ApplicationContext lateinit var context: Context
}

