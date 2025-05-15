package com.lex.qr.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.Group
import com.lex.qr.utils.Semester
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.CurrentAdminPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Month
import javax.inject.Inject

enum class CurrentArchivePage : Page {
    Semesters, Semester, Subjects, Groups
}

data class ArchiveState(
    val page: CurrentArchivePage = CurrentArchivePage.Semesters,
    val isLoading: Boolean = false,
    val hasActive: Boolean = false,

    val semesters: List<Semester> = emptyList(),
    val selectedSemester: Semester? = null,

    val subjects: List<Subject> = emptyList(),

    val groups: List<Group> = emptyList()
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(private val api: API) : ViewModel(){
    private val _uiState = MutableStateFlow(ArchiveState())
    val uiState: StateFlow<ArchiveState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getSemesters()
    }

    fun writeSemesterName(semester: Semester): String {
        fun writeSeason(month: Month): String {
            return when (month) {
                Month.DECEMBER, Month.JANUARY, Month.FEBRUARY -> { "Зима" }
                Month.MARCH, Month.APRIL, Month.MAY -> { "Весна" }
                Month.JUNE, Month.JULY, Month.AUGUST -> { "Лето" }
                Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER -> { "Осень" }
                else -> { "???" }
            }
        }
        val startDate = semester.createdAt
        val endDate = semester.deletedAt

        val startSeason = writeSeason(startDate.month)
        val startYear = startDate.year

        return if (endDate != null) {
            val endSeason = writeSeason(endDate.month)
            val endYear = endDate.year.toString().takeLast(2)
            "$startSeason-$endSeason ${startYear.toString().takeLast(2)}-$endYear"
        } else {
            "$startSeason $startYear"
        }
    }

    //===навигация===

    fun onBackPressed(){
        viewModelScope.launch {
            when(_uiState.value.page) {
                CurrentArchivePage.Semesters -> {
                    _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
                    _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                }
                CurrentArchivePage.Semester -> {
                    _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Semesters)
                    _uiEvent.send(UiEvent.ChangeTitle("Семестры"))
                }
                CurrentArchivePage.Subjects, CurrentArchivePage.Groups -> {
                    val name = writeSemesterName(_uiState.value.selectedSemester!!)
                    _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Semester)
                    _uiEvent.send(UiEvent.ChangeTitle(name))
                }
            }
        }
    }

    //===методы Page.Semesters===

    private fun getSemesters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.getSemesters()
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(semesters = it)
                    if (it.find { s -> s.isActive } != null)
                        _uiState.value = _uiState.value.copy(hasActive = true)
                    else {
                        _uiState.value = _uiState.value.copy(hasActive = false)
                    }
                },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectSemester(semester: Semester){
        viewModelScope.launch {
            val name = writeSemesterName(semester)
            _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Semester)
            _uiEvent.send(UiEvent.ChangeTitle(name))
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(selectedSemester = semester)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun semestersButton(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            if (_uiState.value.hasActive){
                val response = api.closeSemester()
                response.fold(
                    onSuccess = {
                        _uiEvent.send(UiEvent.ShowToast("Активный семестр в архиве"))
                    },
                    onFailure = { error ->
                        error.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            } else {
                val response = api.openSemester()
                response.fold(
                    onSuccess = {
                        _uiEvent.send(UiEvent.ShowToast("Новый семестр открыт"))
                    },
                    onFailure = { error ->
                        error.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
            val response = api.getSemesters()
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(semesters = it)
                    if (it.find { s -> s.isActive } != null)
                        _uiState.value = _uiState.value.copy(hasActive = true)
                    else {
                        _uiState.value = _uiState.value.copy(hasActive = false)
                    }
                },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    //===методы Page.Semester===

    fun getSemestersSubjects(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Subjects)
            _uiEvent.send(UiEvent.ChangeTitle("Предметы"))
            _uiState.value = _uiState.value.copy(isLoading = true)
            val semester = _uiState.value.selectedSemester!!
            val response = api.getSubjectsInArchive(semester.id)
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(subjects = it)
                },
                onFailure = { error ->
                    error.message?.let { msg ->
                        val name = writeSemesterName(semester)
                        _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Semester)
                        _uiEvent.send(UiEvent.ShowToast(msg))
                        _uiEvent.send(UiEvent.ChangeTitle(name))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun getSemestersGroups(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Groups)
            _uiEvent.send(UiEvent.ChangeTitle("Группы"))
            _uiState.value = _uiState.value.copy(isLoading = true)
            val semester = _uiState.value.selectedSemester!!
            val response = api.getGroupsInArchive(semester.id)
            response.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(groups = it)
                },
                onFailure = { error ->
                    error.message?.let { msg ->
                        val name = writeSemesterName(semester)
                        _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Semester)
                        _uiEvent.send(UiEvent.ShowToast(msg))
                        _uiEvent.send(UiEvent.ChangeTitle(name))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}