package com.lex.qr.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.lex.qr.utils.API
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lex.qr.pages.Page
import com.lex.qr.utils.Claims
import com.lex.qr.utils.ClassResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.JoinClassRequest
import com.lex.qr.utils.Rating
import com.lex.qr.utils.TimerWorker
import com.lex.qr.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class CurrentStudentPage: Page {
    MAIN, VISITS
}

data class StudentState(
    val visits: List<ClassResponse> = emptyList(),
    val isLoading: Boolean = false,
    val page: CurrentStudentPage = CurrentStudentPage.MAIN,
    val currentClassId: String? = null,
    val currentRating: Int = 0,
    val isTimer: Boolean = false,
)

@HiltViewModel
class StudentViewModel @Inject constructor(private val api: API, private val workManager: WorkManager) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentState())
    val uiState: StateFlow<StudentState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getCurrent()
    }

    fun getVisits(user: Claims) {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Посещения"))
            _uiState.value = _uiState.value.copy(page = CurrentStudentPage.VISITS, isLoading = true)

            val response = api.getVisits(user.id)
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
                    _uiState.value = _uiState.value.copy(
                        isTimer = it.isTimer,
                        currentClassId = it.id,
                        currentRating = it.rating)
                },
                onFailure = {}
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun joinClass(isGpsEnabled: Boolean, lastLocation: String?, text:String, device: String) {
        viewModelScope.launch {
            if (text.isNotEmpty() && isGpsEnabled && lastLocation!=null) {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val response = api.joinClass(
                    JoinClassRequest(
                        publicId = text,
                        studentGeolocation = lastLocation,
                        device = device
                    )
                )
                response.fold(
                    onSuccess = {
                        getCurrent()

                        _uiState.value = _uiState.value.copy(isTimer = true, isLoading = false)

                        val workRequest = OneTimeWorkRequestBuilder<TimerWorker>()
                            .setInitialDelay(30, TimeUnit.MINUTES)
                            .build()

                        workManager.enqueue(workRequest)

                        workManager.getWorkInfoByIdFlow(workRequest.id)
                            .filter { it.state == WorkInfo.State.SUCCEEDED }
                            .collect {
                                _uiState.value = _uiState.value.copy(isTimer = false)
                            }
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(isLoading = false)

                        it.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
            else if (!isGpsEnabled) {
                _uiEvent.send(UiEvent.ShowToast("Геолокация выключена"))
            }
            else if (lastLocation==null) {
                _uiEvent.send(UiEvent.ShowToast("Геолокация не найдена"))
            }
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