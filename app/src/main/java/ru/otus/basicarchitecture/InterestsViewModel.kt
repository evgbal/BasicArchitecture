package ru.otus.basicarchitecture

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InterestsViewModel @Inject constructor(
    wizardCache: WizardCache
) : ViewModel() {

    val interests = wizardCache.interests

    fun toggleInterest(interest: String) {
        val updatedList = interests.value.toMutableList()
        if (updatedList.contains(interest)) {
            updatedList.remove(interest)
        } else {
            updatedList.add(interest)
        }
        interests.value = updatedList
    }
}

