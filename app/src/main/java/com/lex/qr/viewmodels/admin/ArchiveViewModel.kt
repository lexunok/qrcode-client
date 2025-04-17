package com.lex.qr.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.Group
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.CurrentAdminPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.serialization.Serializable
import javax.inject.Inject

enum class CurrentArchivePage : Page {
    Semesters, Semester, Subjects, Groups
}

//TODO: у предметов будет id семестра?
//TODO: у групп будет id семестра?
@Serializable
data class Semester(
    val id: String,
    val name: String,
    val isActive: Boolean
)

data class ArchiveState(
    val page: CurrentArchivePage = CurrentArchivePage.Semesters,
    val isLoading: Boolean = false,

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

    private var localSemestersList: List<Semester> = listOf(
        Semester("1","Осень 24-25",false),
        Semester("2","Весна 25-25",true)
    )

    private var localSubjectsList1: List<Subject> = listOf(
        Subject("1","Методы оптимизации", LocalDate.now().toString(),false),
        Subject("2","Web-программирование", LocalDate.now().toString(),false),
    )
    private var localSubjectsList2: List<Subject> = listOf(
        Subject("3","Мобильная разработка", LocalDate.now().toString(),false),
        Subject("4","Web-программирование", LocalDate.now().toString(),false),
    )

    private var localGroupsList1: List<Group> = listOf(
        Group("1","ПКТб-22-1",LocalDate.now().toString(),false),
        Group("2","МКМб-22-1",LocalDate.now().toString(),false),
    )
    private var localGroupsList2: List<Group> = listOf(
        Group("3","ПКТб-22-1",LocalDate.now().toString(),false),
        Group("4","МКМб-22-1",LocalDate.now().toString(),false),
    )

    init {
        getSemesters()
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
                    _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Semester)
                    _uiEvent.send(UiEvent.ChangeTitle(_uiState.value.selectedSemester!!.name))
                }
            }
        }
    }

    //===методы Page.Semesters===

    private fun getSemesters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            //TODO: получение списка всех семестров
            _uiState.value = _uiState.value.copy(semesters = localSemestersList)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectSemester(semester: Semester){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Semester)
            _uiEvent.send(UiEvent.ChangeTitle(semester.name))
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(selectedSemester = semester)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun archiveActiveSemester(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            //TODO: запрос на архивацию активного семестра
            if (localSemestersList.find { it.isActive } != null){
                val updatedSemesters = localSemestersList.map { semester ->
                    if (semester.isActive) semester.copy(isActive = false) else semester
                }
                //TODO: получение списка всех семестров
                _uiState.value = _uiState.value.copy(semesters = updatedSemesters)
                localSemestersList = updatedSemesters
                _uiEvent.send(UiEvent.ShowToast("Активный семестр в архиве"))
            } else {
                _uiEvent.send(UiEvent.ShowToast("Нет активного семестра"))
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    //===методы Page.Semesters===

    fun getSemestersSubjects(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Subjects)
            _uiEvent.send(UiEvent.ChangeTitle("Предметы"))
            _uiState.value = _uiState.value.copy(isLoading = true)
            //TODO: запрос на получение предметов семестра
            if (_uiState.value.selectedSemester?.id == "1")
                _uiState.value = _uiState.value.copy(subjects = localSubjectsList1)
            else if (_uiState.value.selectedSemester?.id == "2")
                _uiState.value = _uiState.value.copy(subjects = localSubjectsList2)

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun getSemestersGroups(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentArchivePage.Groups)
            _uiEvent.send(UiEvent.ChangeTitle("Группы"))
            _uiState.value = _uiState.value.copy(isLoading = true)
            //TODO: запрос на получение групп семестра
            if (_uiState.value.selectedSemester?.id == "1")
                _uiState.value = _uiState.value.copy(groups = localGroupsList1)
            else if (_uiState.value.selectedSemester?.id == "2")
                _uiState.value = _uiState.value.copy(groups = localGroupsList2)

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}