package ru.otus.basicarchitecture

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import ru.otus.basicarchitecture.data.WizardCache
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    wizardCache: WizardCache
) : ViewModel() {
    val name = wizardCache.name
    val surname = wizardCache.surname
    val birthDate = wizardCache.birthDate
    val address = MutableStateFlow(
        "${wizardCache.country.value}, ${wizardCache.city.value}, ${wizardCache.address.value}")
    val interests = wizardCache.interests
}