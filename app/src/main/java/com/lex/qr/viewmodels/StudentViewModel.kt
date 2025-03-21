package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import com.lex.qr.utils.API
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.lex.qr.utils.ClassResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val visits: List<ClassResponse> = emptyList(),
)

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}
@HiltViewModel
class StudentViewModel @Inject constructor(private val api: API) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun getVisits(id: String) {
        viewModelScope.launch {
            val response = api.getVisits(id)
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(visits = it)
                },
                onFailure = {
                    it.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
        }
    }
}