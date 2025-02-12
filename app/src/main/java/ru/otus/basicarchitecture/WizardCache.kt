package ru.otus.basicarchitecture

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WizardCache @Inject constructor() {
    val name = MutableStateFlow("")
    val surname = MutableStateFlow("")
    val birthDate = MutableStateFlow("")
    val country = MutableStateFlow("")
    val city = MutableStateFlow("")
    val address = MutableStateFlow("")
    val interests = MutableStateFlow<List<String>>(emptyList())
}