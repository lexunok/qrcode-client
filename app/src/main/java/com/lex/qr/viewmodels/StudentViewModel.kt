package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import com.lex.qr.utils.API
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.ClassResponse
import com.lex.qr.utils.JoinClassRequest
import com.lex.qr.utils.Rating
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CurrentStudentPage: Page {
    MAIN, VISITS
}

data class UiState(
    val visits: List<ClassResponse> = emptyList(),
    val isLoading: Boolean = false,
    val page: CurrentStudentPage = CurrentStudentPage.MAIN,
    val currentClassId: String? = null,
    val currentRating: Int = 0
)

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ChangeTitle(val title: String) : UiEvent()
}
@HiltViewModel
class StudentViewModel @Inject constructor(private val api: API) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun getVisits(id: String) {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Посещения"))
            _uiState.value = _uiState.value.copy(page = CurrentStudentPage.VISITS)

            _uiState.value = _uiState.value.copy(isLoading = true)
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
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun getCurrent() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.getCurrent()
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(currentClassId = it.id)
                    _uiState.value = _uiState.value.copy(currentRating = it.rating)
                },
                onFailure = {}
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun joinClass(request: JoinClassRequest) {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.joinClass(request)
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(currentClassId = it.id)
                },
                onFailure = {
                    it.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun evaluate(star: Int) {
        viewModelScope.launch {
            _uiState.value.currentClassId?.let { id ->
                val response = api.evaluate(Rating(id, star))
                response.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(currentRating = it.rating)
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
    fun toMain() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentStudentPage.MAIN)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
        }
    }
}