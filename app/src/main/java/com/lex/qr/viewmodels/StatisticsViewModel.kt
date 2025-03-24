package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.CurrentStaffPage
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.Group
import com.lex.qr.utils.StudentStats
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class CurrentStatisticsPage: Page {
    GroupList, StudentList, UserStatistics, GroupStatistics
}

data class StatisticsState(
    val groups: List<Group> = emptyList(),
    val students: List<StudentStats> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val subjectsHist: List<SubjectHist> = emptyList(),
    val groupBars: List<GroupBar> = emptyList(),

    val attendance: Attendance? = null,
    val selectedGroup: Group? = null,
    val selectedStudent: StudentStats? = null,
    val selectedSubject: Subject? = null,
    val page: CurrentStatisticsPage = CurrentStatisticsPage.GroupList,
    val isLoading: Boolean = false,
    val dateFromString: String = "",
    val dateToString: String = "",
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null
    )
data class SubjectHist(val id: String,  val name: String, val total: Int, val count: Int)
data class Attendance(val total: Int, val count: Int, val visits: List<VisitsPerDate>)
data class VisitsPerDate(val date: LocalDate, val count: Int)
data class GroupBar(val title: String, val count: Int)

@HiltViewModel
class StatisticsViewModel @Inject constructor(private val api: API) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsState())
    val uiState: StateFlow<StatisticsState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun changeDateFrom(text: String){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                dateFromString = text,
                dateFrom =
                if (text.length == 10) LocalDate.parse(text, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                else null
            )
        }
    }

    fun changeDateTo(text: String){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                dateToString = text,
                dateTo =
                if (text.length == 10) LocalDate.parse(text, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                else null
            )
        }
    }

    fun getGroups() {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Группы"))
            _uiState.value = _uiState.value.copy(
                page = CurrentStatisticsPage.GroupList,
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
    fun getStudents(group: Group) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ChangeTitle(group.name))
            _uiState.value = _uiState.value.copy(
                selectedGroup = group,
                page = CurrentStatisticsPage.StudentList,
                isLoading = true
            )
            val response = api.getStudentsByGroup(group.id)
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
    fun getUserStatistics(user: StudentStats) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ChangeTitle("Статистика"))
            _uiState.value = _uiState.value.copy(
                selectedStudent = user,
                page = CurrentStatisticsPage.UserStatistics,
                isLoading = true
            )
            //Запрос
            _uiState.value = _uiState.value.copy(
                subjectsHist = listOf(
                    SubjectHist("1","Разработка мобильных приложений",20, 11),
                    SubjectHist("1","Матеша",20, 11),
                    SubjectHist("1","Физра",10, 5),
                    SubjectHist("1","Предмет какойто",13, 2),
                    SubjectHist("1","Электив",5, 4),
                    SubjectHist("1","Разработка мобильных приложений",20, 11),
                    SubjectHist("1","Матеша",20, 11),
                    SubjectHist("1","Физра",10, 5),
                    SubjectHist("1","Предмет какойто",13, 2),
                    SubjectHist("1","Электив",5, 4),
                )
            )
            //Запрос
            val startDate = LocalDate.now().minusWeeks(12) // Начало 3 месяца назад
            _uiState.value = _uiState.value.copy(
                attendance = Attendance(visits = List(13) { i ->
                    val date = startDate.plusWeeks(i.toLong())
                    val visits = (0..10).random() // Случайное количество посещений
                    VisitsPerDate(date, visits)
                }, total = 100, count = 72
                )
            )
//            val response = api.getGroups()
//            response.fold(
//                onSuccess = {
//                    _uiState.value = _uiState.value.copy(groups = it)
//                },
//                onFailure = {
//                    it.message?.let { msg ->
//                        _uiEvent.send(UiEvent.ShowToast(msg))
//                    }
//                }
//            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun getGroupStatistics() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ChangeTitle("Статистика"))
            _uiState.value = _uiState.value.copy(
                page = CurrentStatisticsPage.GroupStatistics,
                isLoading = true
            )

            _uiState.value = _uiState.value.copy(
                subjectsHist = listOf(
                    SubjectHist("1","Разработка мобильных приложений",20, 11),
                    SubjectHist("1","Матеша",20, 11),
                    SubjectHist("1","Физра",10, 5),
                    SubjectHist("1","Предмет какойто",13, 2),
                    SubjectHist("1","Электив",5, 4),
                    SubjectHist("1","Разработка мобильных приложений",20, 11),
                    SubjectHist("1","Матеша",20, 11),
                    SubjectHist("1","Физра",10, 5),
                    SubjectHist("1","Предмет какойто",13, 2),
                    SubjectHist("1","Электив",5, 4),
                )
            )

            val startDate = LocalDate.now().minusWeeks(12) // Начало 3 месяца назад
            _uiState.value = _uiState.value.copy(
                attendance = Attendance(visits = List(13) { i ->
                    val date = startDate.plusWeeks(i.toLong())
                    val visits = (0..10).random() // Случайное количество посещений
                    VisitsPerDate(date, visits)
                }, total = 100, count = 72
                )
            )

            _uiState.value = _uiState.value.copy(
                groupBars = listOf(
                    GroupBar("0-20", 8),
                    GroupBar("20-40", 0),
                    GroupBar("40-60", 14),
                    GroupBar("60-80", 15),
                    GroupBar("80-100", 4),
                )
            )
//            val response = api.getGroups()
//            response.fold(
//                onSuccess = {
//                    _uiState.value = _uiState.value.copy(groups = it)
//                },
//                onFailure = {
//                    it.message?.let { msg ->
//                        _uiEvent.send(UiEvent.ShowToast(msg))
//                    }
//                }
//            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun getSubjectList(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
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
    fun setSelectedSubject(subject: Subject?){
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ChangeTitle("Статистика"))
            _uiState.value = _uiState.value.copy(
                page = if (_uiState.value.selectedStudent != null) CurrentStatisticsPage.UserStatistics
                    else CurrentStatisticsPage.GroupStatistics,
                selectedSubject = subject
            )
        }
    }
    fun onBackPressed() {
        viewModelScope.launch {
            when(_uiState.value.page) {
                CurrentStatisticsPage.GroupStatistics -> {
                    _uiState.value.selectedGroup?.let {
                        _uiEvent.send(UiEvent.ChangeTitle(it.name))
                    }
                    _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.StudentList)
                }
                CurrentStatisticsPage.StudentList -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Группы"))
                    _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.GroupList)
                }
                CurrentStatisticsPage.UserStatistics -> {
                    _uiState.value.selectedGroup?.let {
                        _uiEvent.send(UiEvent.ChangeTitle(it.name))
                    }
                    _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.StudentList)
                }
                CurrentStatisticsPage.GroupList -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                    _uiEvent.send(UiEvent.ChangePage(CurrentStaffPage.QRCODE))
                }
            }
        }
    }
}