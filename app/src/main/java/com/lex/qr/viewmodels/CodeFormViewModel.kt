package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.CodeRequest
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.GetClassRequest
import com.lex.qr.utils.GetClassResponse
import com.lex.qr.utils.Group
import com.lex.qr.utils.Student
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CurrentCodeFormPage: Page {
    SubjectList, GroupList, LifeTime
}

data class CodeFormState(
    val groups: List<Group> = emptyList(),
    val subjects: List<Subject> = emptyList(),

    val selectedSubject: Subject? = null,
    val selectedGroup: Group? = null,

    val page: CurrentCodeFormPage = CurrentCodeFormPage.SubjectList,
    val isLoading: Boolean = false,
    )
@HiltViewModel
class CodeFormViewModel @Inject constructor(private val api: API, private val geolocationClient: GeolocationClient) : ViewModel() {

    private val _uiState = MutableStateFlow(CodeFormState())
    val uiState: StateFlow<CodeFormState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()


    fun getSubjectList() {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Выберите предмет"))
            _uiState.value = _uiState.value.copy(
                page = CurrentCodeFormPage.SubjectList,
                isLoading = true
            )
            val response = api.getSubjects()
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(subjects = it)
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
    fun getGroupList(subject: Subject) {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Выберите группу"))
            _uiState.value = _uiState.value.copy(
                selectedSubject = subject,
                page = CurrentCodeFormPage.GroupList,
                isLoading = true
            )
            val response = api.getGroups()
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(groups = it)
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
    fun selectLifeTime(group: Group) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ChangeTitle("Время"))
            _uiState.value = _uiState.value.copy(
                selectedGroup = group,
                page = CurrentCodeFormPage.LifeTime,
            )
        }
    }
    fun createCode(lifetime: Int, lastLocation: String) {
        viewModelScope.launch {
            if(geolocationClient.checkGps() && lastLocation.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = true)

                _uiState.value.selectedSubject?.let { subject ->
                    _uiState.value.selectedGroup?.let { group ->
                        val request = CodeRequest(
                            subjectId = subject.id,
                            groupId = group.id,
                            geolocation = lastLocation,
                            lifetime = lifetime
                        )
                        val response = api.createClass(request)
                        response.fold(
                            onSuccess = {
                                _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                                _uiEvent.send(UiEvent.ChangePage(CurrentStaffPage.Main))
                                _uiEvent.send(UiEvent.CodeCreated(it))
                            },
                            onFailure = {
                                it.message?.let { msg ->
                                    _uiEvent.send(UiEvent.ShowToast(msg))
                                }
                            }
                        )
                    }
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            when(_uiState.value.page) {
                CurrentCodeFormPage.SubjectList -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                    _uiEvent.send(UiEvent.ChangePage(CurrentStaffPage.Main))
                }
                CurrentCodeFormPage.GroupList -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Выберите предмет"))
                    _uiState.value = _uiState.value.copy(
                        page = CurrentCodeFormPage.SubjectList,
                    )
                }
                CurrentCodeFormPage.LifeTime -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Выберите группу"))
                    _uiState.value = _uiState.value.copy(
                        page = CurrentCodeFormPage.GroupList,
                    )
                }
            }
        }
    }
}