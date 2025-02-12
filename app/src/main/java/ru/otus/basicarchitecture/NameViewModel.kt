package ru.otus.basicarchitecture

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NameViewModel @Inject constructor(
    wizardCache: WizardCache
) : ViewModel() {
    val name = wizardCache.name
    val surname = wizardCache.surname
    val birthDate = wizardCache.birthDate
}