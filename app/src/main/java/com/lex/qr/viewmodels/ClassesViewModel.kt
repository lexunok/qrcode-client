package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.Attendance
import com.lex.qr.utils.GetClassRequest
import com.lex.qr.utils.GetClassResponse
import com.lex.qr.utils.StatisticRequest
import com.lex.qr.utils.Group
import com.lex.qr.utils.GroupBar
import com.lex.qr.utils.LineChart
import com.lex.qr.utils.Student
import com.lex.qr.utils.StudentStats
import com.lex.qr.utils.Subject
import com.lex.qr.utils.SubjectHist
import com.lex.qr.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.datetime.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class CurrentClassesPage: Page {
    SubjectList, GroupList, Classes, Visits
}

data class ClassesState(
    val groups: List<Group> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val classes: List<GetClassResponse> = emptyList(),
    val students: List<Student> = emptyList(),

    val selectedSubject: Subject? = null,
    val page: CurrentClassesPage = CurrentClassesPage.SubjectList,
    val isLoading: Boolean = false,
    )
@HiltViewModel
class ClassesViewModel @Inject constructor(private val api: API) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassesState())
    val uiState: StateFlow<ClassesState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()


    fun getSubjectList() {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Выберите предмет"))
            _uiState.value = _uiState.value.copy(
                page = CurrentClassesPage.SubjectList,
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
                page = CurrentClassesPage.GroupList,
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
    fun getClasses(groupId: String) {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Проведенные пары"))
            _uiState.value = _uiState.value.copy(
                page = CurrentClassesPage.Classes,
                isLoading = true
            )
            _uiState.value.selectedSubject?.let { subject ->
                val response = api.getClasses(
                    GetClassRequest(
                        subjectId = subject.id,
                        groupId = groupId
                    )
                )
                response.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(classes = it)
                    },
                    onFailure = {
                        it.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun getStudents(publicId: String) {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Присутствующие"))
            _uiState.value = _uiState.value.copy(
                page = CurrentClassesPage.Visits,
                isLoading = true
            )

            val response = api.getStudents(publicId)
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(students = it)
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

    fun onBackPressed() {
        viewModelScope.launch {
            when(_uiState.value.page) {
                CurrentClassesPage.SubjectList -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                    _uiEvent.send(UiEvent.ChangePage(CurrentStaffPage.Main))
                }
                CurrentClassesPage.GroupList -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Выберите предмет"))
                    _uiState.value = _uiState.value.copy(
                        page = CurrentClassesPage.SubjectList,
                    )
                }
                CurrentClassesPage.Classes -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Выберите группу"))
                    _uiState.value = _uiState.value.copy(
                        page = CurrentClassesPage.GroupList,
                    )
                }
                CurrentClassesPage.Visits -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Проведенные пары"))
                    _uiState.value = _uiState.value.copy(
                        page = CurrentClassesPage.Classes,
                    )
                }
            }
        }
    }
}