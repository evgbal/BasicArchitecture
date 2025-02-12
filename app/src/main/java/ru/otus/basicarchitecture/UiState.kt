package ru.otus.basicarchitecture

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data object Success : UiState()
    data class Error(val message: String) : UiState()
}