package com.lex.qr.viewmodels

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.CurrentStaffPage
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.ClassResponse
import com.lex.qr.utils.Group
import com.lex.qr.utils.LoginRequest
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import com.lex.qr.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class CurrentStatisticsPage: Page {
    GroupList, StudentList, UserStatistics, GroupStatistics
}

data class StatisticsState(
    val groups: List<Group> = emptyList(),
    val students: List<StudentStats> = emptyList(),
    val subjectsHist: List<SubjectHist> = emptyList(),
    val groupBars: List<GroupBar> = emptyList(),

    val attendance: Attendance? = null,
    val selectedGroup: Group? = null,
    val selectedStudent: StudentStats? = null,
    val selectedSubject: Subject? = null,
    val page: CurrentStatisticsPage = CurrentStatisticsPage.GroupList,
    val isLoading: Boolean = false,
    )
data class StudentStats(val id: String, val avatarUrl: String, val firstName: String, val lastName: String)
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

    fun getGroups() {
        viewModelScope.launch {

            _uiEvent.send(UiEvent.ChangeTitle("Группы"))
            _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.GroupList)
            _uiState.value = _uiState.value.copy(isLoading = true)
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
            _uiState.value = _uiState.value.copy(selectedGroup = group)
            _uiEvent.send(UiEvent.ChangeTitle(group.name))
            _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.StudentList)
            _uiState.value = _uiState.value.copy(isLoading = true)
            //Запрос
            _uiState.value = _uiState.value.copy(
                students = listOf(
                    StudentStats("1","url","Александр","Черненко"),
                    StudentStats("1","url","Вадим","Власов"),
                    StudentStats("1","url","Чел","Крутой"),
                    StudentStats("1","url","Александр","Черненко"),
                    StudentStats("1","url","Вадим","Власов"),
                    StudentStats("1","url","Чел","Крутой"),
                    StudentStats("1","url","Александр","Черненко"),
                    StudentStats("1","url","Вадим","Власов"),
                    StudentStats("1","url","Чел","Крутой"),
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
    fun getUserStatistics(user: StudentStats) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedStudent = user)
            _uiEvent.send(UiEvent.ChangeTitle("Статистика"))
            _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.UserStatistics)
            _uiState.value = _uiState.value.copy(isLoading = true)
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
            _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.GroupStatistics)
            _uiState.value = _uiState.value.copy(isLoading = true)

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