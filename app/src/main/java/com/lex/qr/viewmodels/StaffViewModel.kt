package com.lex.qr.viewmodels

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.CodeResponse
import com.lex.qr.utils.Student
import com.lex.qr.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CurrentStaffPage: Page {
    Main, ClassList, Activity, Statistics, CodeForm
}

data class StaffState(
    val students: List<Student> = emptyList(),
    val code: CodeResponse? = null,
    val page: CurrentStaffPage = CurrentStaffPage.Main,
    val isLoading: Boolean = false,
    )

@HiltViewModel
class StaffViewModel @Inject constructor(private val api: API) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffState())
    val uiState: StateFlow<StaffState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    val lazyListState = LazyListState()

    init {
        observeScroll()
    }

    private fun observeScroll() {
        viewModelScope.launch {
            snapshotFlow { lazyListState.isScrollInProgress }
                .collect { isScrolling ->
                    if (isScrolling) {
                        if (!lazyListState.canScrollForward) {
                            _uiState.value = _uiState.value.copy(page = CurrentStaffPage.Main)
                            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                        }
                    } else {
                        if (!lazyListState.canScrollForward && !lazyListState.canScrollBackward) {
                            _uiState.value = _uiState.value.copy(page = CurrentStaffPage.Main)
                            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                        }
                    }
                }
        }
    }

    fun toMain() {
        viewModelScope.launch {
            if (_uiState.value.page != CurrentStaffPage.Main) {
                _uiState.value = _uiState.value.copy(page = CurrentStaffPage.Main)
                _uiEvent.send(UiEvent.ChangeTitle("Главная"))
            } else {
                _uiState.value = _uiState.value.copy(page = CurrentStaffPage.CodeForm)
            }
        }
    }
    fun toClassList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentStaffPage.ClassList)
        }
    }
    fun toStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentStaffPage.Statistics)
        }
    }
    fun updatePage(newPage: Page) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = newPage as CurrentStaffPage)
        }
    }
    fun setCode(code: CodeResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(code = code)
        }
    }
    fun toActivity() {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Присутствующие"))
            _uiState.value = _uiState.value.copy(page = CurrentStaffPage.Activity, isLoading = true)
            _uiState.value.code?.let {
                val response = api.getStudents(it.publicId)
                response.fold(
                    onSuccess = {result->
                        _uiState.value = _uiState.value.copy(students = result)
                    },
                    onFailure = {result->
                        result.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun deactivateStudent(id: String) {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value.code?.let {
                val response = api.deactivateStudent(id)
                response.fold(
                    onSuccess = {
                        val students = api.getStudents(it.publicId)
                        students.fold(
                            onSuccess = {result->
                                _uiState.value = _uiState.value.copy(students = result)
                            },
                            onFailure = {result->
                                result.message?.let { msg ->
                                    _uiEvent.send(UiEvent.ShowToast(msg))
                                }
                            }
                        )
                    },
                    onFailure = {result->
                        result.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun activateStudent(id: String) {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value.code?.let {
                val response = api.activateStudent(id)
                response.fold(
                    onSuccess = {
                        val students = api.getStudents(it.publicId)
                        students.fold(
                            onSuccess = {result->
                                _uiState.value = _uiState.value.copy(students = result)
                            },
                            onFailure = {result->
                                result.message?.let { msg ->
                                    _uiEvent.send(UiEvent.ShowToast(msg))
                                }
                            }
                        )
                    },
                    onFailure = {result->
                        result.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}