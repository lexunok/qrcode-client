package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.Attendance
import com.lex.qr.utils.StatisticRequest
import com.lex.qr.utils.Group
import com.lex.qr.utils.GroupBar
import com.lex.qr.utils.LineChart
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

enum class CurrentStatisticsPage: Page {
    GroupList, StudentList, UserStatistics, GroupStatistics
}

data class StatisticsState(
    val groups: List<Group> = emptyList(),
    val students: List<StudentStats> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val subjectsHist: List<SubjectHist> = emptyList(),
    val groupBars: List<GroupBar> = emptyList(),
    val lineCharts: List<LineChart> = emptyList(),

    val attendance: Attendance? = null,
    val selectedGroup: Group? = null,
    val selectedStudent: StudentStats? = null,
    val selectedSubject: Subject? = null,
    val page: CurrentStatisticsPage = CurrentStatisticsPage.GroupList,
    val isLoading: Boolean = false,
    val dateFrom: String = "",
    val dateTo: String = "",
    val showDialog: Boolean = false
    )
@HiltViewModel
class StatisticsViewModel @Inject constructor(private val api: API) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsState())
    val uiState: StateFlow<StatisticsState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()
    init {
        getGroups()
    }
    private suspend fun getAttendance() {
        val startDate = if (_uiState.value.dateFrom.length == 10)
            LocalDate.parse(uiState.value.dateFrom, DateTimeFormatter.ofPattern("dd.MM.yyyy")).atStartOfDay().toKotlinLocalDateTime()  else null

        val endDate = if (_uiState.value.dateTo.length == 10)
            LocalDate.parse(uiState.value.dateTo, DateTimeFormatter.ofPattern("dd.MM.yyyy")).atStartOfDay().toKotlinLocalDateTime() else null

        val request = StatisticRequest(
            id = "",
            subjectId = _uiState.value.selectedSubject?.id,
            startDate = startDate,
            endDate = endDate,
        )

        if (uiState.value.page == CurrentStatisticsPage.UserStatistics) {
            _uiState.value.selectedStudent?.let {
                request.id = it.id

                val attendance = api.getStudentAttendance(request)
                attendance.fold(
                    onSuccess = {result->
                        _uiState.value = _uiState.value.copy(attendance = result)
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(attendance = null)
                    }
                )

                val subjectHist = api.getStudentSubjectHist(request)
                subjectHist.fold(
                    onSuccess = {result->
                        _uiState.value = _uiState.value.copy(subjectsHist = result)
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(subjectsHist = emptyList())
                    }
                )

                val lineChart = api.getStudentLineChart(request)
                lineChart.fold(
                    onSuccess = {result->
                        _uiState.value = _uiState.value.copy(lineCharts = result)
                    },
                    onFailure = {result->
                        result.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
        } else {
            _uiState.value.selectedGroup?.let {
                request.id = it.id

                val bars = api.getGroupBars(request)
                bars.fold(
                    onSuccess = {result->
                        _uiState.value = _uiState.value.copy(groupBars = result)
                    },
                    onFailure = {result->
                        result.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )

                val attendance = api.getGroupAttendance(request)
                attendance.fold(
                    onSuccess = {result->
                        _uiState.value = _uiState.value.copy(attendance = result)
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(attendance = null)
                    }
                )

                val subjectHist = api.getGroupSubjectHist(request)
                subjectHist.fold(
                    onSuccess = {result->
                        _uiState.value = _uiState.value.copy(subjectsHist = result)
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(subjectsHist = emptyList())
                    }
                )

                val lineChart = api.getGroupLineChart(request)
                lineChart.fold(
                    onSuccess = {result->
                        _uiState.value = _uiState.value.copy(lineCharts = result)
                    },
                    onFailure = {result->
                        result.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
        }
    }
    fun changeDateFrom(text: String){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                dateFrom = text
            )
            if (text.length == 10 || text.isEmpty()) {
                getAttendance()
            }
        }
    }

    fun changeDateTo(text: String){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                dateTo = text
            )
            if (text.length == 10 || text.isEmpty()) {
                getAttendance()
            }
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

            getAttendance()

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
            getAttendance()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun getSubjectList(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showDialog = true, isLoading = true)
            //ТОЛЬКО ТЕ ПРЕДМЕТЫ КОТОРЫЕ БЫЛИ У ЭТОГО ПОЛЬЗОВАТЕЛЯ/ГРУППЫ
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
            _uiState.value = _uiState.value.copy(selectedSubject = subject, showDialog = false)
            getAttendance()
        }
    }
    fun onBackPressed() {
        viewModelScope.launch {
            when(_uiState.value.page) {
                CurrentStatisticsPage.GroupStatistics -> {
                    if (_uiState.value.showDialog) {
                        _uiState.value = _uiState.value.copy(showDialog = false)
                    } else {
                        _uiState.value.selectedGroup?.let {
                            _uiEvent.send(UiEvent.ChangeTitle(it.name))
                        }
                        _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.StudentList)
                    }
                }
                CurrentStatisticsPage.StudentList -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Группы"))
                    _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.GroupList)
                }
                CurrentStatisticsPage.UserStatistics -> {
                    if (_uiState.value.showDialog) {
                        _uiState.value = _uiState.value.copy(showDialog = false)
                    } else {
                        _uiState.value.selectedGroup?.let {
                            _uiEvent.send(UiEvent.ChangeTitle(it.name))
                        }
                        _uiState.value = _uiState.value.copy(page = CurrentStatisticsPage.StudentList)
                    }
                }
                CurrentStatisticsPage.GroupList -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                    _uiEvent.send(UiEvent.ChangePage(CurrentStaffPage.Main))
                }
            }
        }
    }
}