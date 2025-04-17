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
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class CurrentStatisticsPage: Page {
    GroupList, StudentList, UserStatistics, GroupStatistics
}

enum class GraphZoomLevel{
    DAYS, WEEKS, MONTHS
}

data class StatisticsState(
    val groups: List<Group> = emptyList(),
    val students: List<StudentStats> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val subjectsHist: List<SubjectHist> = emptyList(),
    val groupBars: List<GroupBar> = emptyList(),
    val lineCharts: List<LineChart> = emptyList(),
    val changedLineCharts: List<LineChart> = emptyList(),

    val attendance: Attendance? = null,
    val selectedGroup: Group? = null,
    val selectedStudent: StudentStats? = null,
    val selectedSubject: Subject? = null,
    val page: CurrentStatisticsPage = CurrentStatisticsPage.GroupList,
    val isLoading: Boolean = false,
    val dateFrom: String = "",
    val dateTo: String = "",
    val showDialog: Boolean = false,
    val zoomLevel: GraphZoomLevel = GraphZoomLevel.DAYS
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
                        _uiState.value = _uiState.value.copy(lineCharts = result, changedLineCharts = result)
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
                        _uiState.value = _uiState.value.copy(lineCharts = result, changedLineCharts = result)
                    },
                    onFailure = {result->
                        result.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
//                val testList = listOf(
//                    LineChart(5, LocalDateTime.of(2025, 2, 1, 0, 0, 0)),
//                    LineChart(3, LocalDateTime.of(2025, 2, 2, 0, 0, 0)),
//                    LineChart(7, LocalDateTime.of(2025, 2, 3, 0, 0, 0)),
//                    LineChart(2, LocalDateTime.of(2025, 2, 4, 0, 0, 0)),
//                    LineChart(8, LocalDateTime.of(2025, 2, 5, 0, 0, 0)),
//                    LineChart(4, LocalDateTime.of(2025, 2, 6, 0, 0, 0)),
//                    LineChart(6, LocalDateTime.of(2025, 2, 7, 0, 0, 0)),
//                    LineChart(1, LocalDateTime.of(2025, 2, 8, 0, 0, 0)),
//                    LineChart(9, LocalDateTime.of(2025, 2, 9, 0, 0, 0)),
//                    LineChart(10, LocalDateTime.of(2025, 2, 10, 0, 0, 0)),
//                    LineChart(3, LocalDateTime.of(2025, 2, 11, 0, 0, 0)),
//                    LineChart(5, LocalDateTime.of(2025, 2, 12, 0, 0, 0)),
//                    LineChart(7, LocalDateTime.of(2025, 2, 13, 0, 0, 0)),
//                    LineChart(2, LocalDateTime.of(2025, 2, 14, 0, 0, 0)),
//                    LineChart(6, LocalDateTime.of(2025, 2, 15, 0, 0, 0)),
//                    LineChart(4, LocalDateTime.of(2025, 2, 16, 0, 0, 0)),
//                    LineChart(8, LocalDateTime.of(2025, 2, 17, 0, 0, 0)),
//                    LineChart(1, LocalDateTime.of(2025, 2, 18, 0, 0, 0)),
//                    LineChart(9, LocalDateTime.of(2025, 2, 19, 0, 0, 0)),
//                    LineChart(10, LocalDateTime.of(2025, 2, 20, 0, 0, 0)),
//                    LineChart(2, LocalDateTime.of(2025, 2, 21, 0, 0, 0)),
//                    LineChart(5, LocalDateTime.of(2025, 2, 22, 0, 0, 0)),
//                    LineChart(3, LocalDateTime.of(2025, 2, 23, 0, 0, 0)),
//                    LineChart(7, LocalDateTime.of(2025, 2, 24, 0, 0, 0)),
//                    LineChart(6, LocalDateTime.of(2025, 2, 25, 0, 0, 0)),
//                    LineChart(4, LocalDateTime.of(2025, 2, 26, 0, 0, 0)),
//                    LineChart(8, LocalDateTime.of(2025, 2, 27, 0, 0, 0)),
//                    LineChart(1, LocalDateTime.of(2025, 2, 28, 0, 0, 0)),
//                    LineChart(9, LocalDateTime.of(2025, 3, 1, 0, 0, 0)),
//                    LineChart(10, LocalDateTime.of(2025, 3, 2, 0, 0, 0)),
//                    LineChart(3, LocalDateTime.of(2025, 3, 3, 0, 0, 0)),
//                    LineChart(5, LocalDateTime.of(2025, 3, 4, 0, 0, 0)),
//                    LineChart(7, LocalDateTime.of(2025, 3, 5, 0, 0, 0)),
//                    LineChart(2, LocalDateTime.of(2025, 3, 6, 0, 0, 0)),
//                    LineChart(6, LocalDateTime.of(2025, 3, 7, 0, 0, 0)),
//                    LineChart(4, LocalDateTime.of(2025, 3, 8, 0, 0, 0)),
//                    LineChart(8, LocalDateTime.of(2025, 3, 9, 0, 0, 0)),
//                    LineChart(1, LocalDateTime.of(2025, 3, 10, 0, 0, 0)),
//                    LineChart(9, LocalDateTime.of(2025, 3, 11, 0, 0, 0)),
//                    LineChart(10, LocalDateTime.of(2025, 3, 12, 0, 0, 0)),
//                    LineChart(2, LocalDateTime.of(2025, 3, 13, 0, 0, 0)),
//                    LineChart(5, LocalDateTime.of(2025, 3, 14, 0, 0, 0)),
//                    LineChart(3, LocalDateTime.of(2025, 3, 15, 0, 0, 0)),
//                    LineChart(7, LocalDateTime.of(2025, 3, 16, 0, 0, 0)),
//                    LineChart(6, LocalDateTime.of(2025, 3, 17, 0, 0, 0)),
//                    LineChart(4, LocalDateTime.of(2025, 3, 18, 0, 0, 0)),
//                    LineChart(8, LocalDateTime.of(2025, 3, 19, 0, 0, 0)),
//                    LineChart(1, LocalDateTime.of(2025, 3, 20, 0, 0, 0)),
//                    LineChart(9, LocalDateTime.of(2025, 3, 21, 0, 0, 0)),
//                    LineChart(10, LocalDateTime.of(2025, 3, 22, 0, 0, 0)),
//                    LineChart(3, LocalDateTime.of(2025, 3, 23, 0, 0, 0)),
//                    LineChart(5, LocalDateTime.of(2025, 3, 24, 0, 0, 0)),
//                    LineChart(7, LocalDateTime.of(2025, 3, 25, 0, 0, 0)),
//                    LineChart(2, LocalDateTime.of(2025, 3, 26, 0, 0, 0)),
//                    LineChart(6, LocalDateTime.of(2025, 3, 27, 0, 0, 0)),
//                    LineChart(4, LocalDateTime.of(2025, 3, 28, 0, 0, 0)),
//                    LineChart(8, LocalDateTime.of(2025, 3, 29, 0, 0, 0)),
//                    LineChart(1, LocalDateTime.of(2025, 3, 30, 0, 0, 0)),
//                    LineChart(9, LocalDateTime.of(2025, 3, 31, 0, 0, 0)),
//                    LineChart(10, LocalDateTime.of(2025, 4, 1, 0, 0, 0)),
//                    LineChart(2, LocalDateTime.of(2025, 4, 2, 0, 0, 0)),
//                    LineChart(5, LocalDateTime.of(2025, 4, 3, 0, 0, 0)),
//                    LineChart(3, LocalDateTime.of(2025, 4, 4, 0, 0, 0)),
//                    LineChart(7, LocalDateTime.of(2025, 4, 5, 0, 0, 0)),
//                    LineChart(6, LocalDateTime.of(2025, 4, 6, 0, 0, 0)),
//                    LineChart(4, LocalDateTime.of(2025, 4, 7, 0, 0, 0)),
//                    LineChart(8, LocalDateTime.of(2025, 4, 8, 0, 0, 0)),
//                    LineChart(1, LocalDateTime.of(2025, 4, 9, 0, 0, 0)),
//                    LineChart(9, LocalDateTime.of(2025, 4, 10, 0, 0, 0))
//                )
//                _uiState.value = _uiState.value.copy(lineCharts = testList, changedLineCharts = testList)
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
    fun changeZoom(newZoom: GraphZoomLevel){
        viewModelScope.launch {
            when(newZoom){
                GraphZoomLevel.DAYS -> {
                    _uiState.value = _uiState.value.copy(
                        changedLineCharts = _uiState.value.lineCharts,
                        zoomLevel = newZoom
                    )
                }
                GraphZoomLevel.WEEKS -> {
                    val newList = _uiState.value.lineCharts.groupBy {
                        it.date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    }.map { (weekStart, visits) -> LineChart(visits.sumOf { it.visitCount }, weekStart) }
                    _uiState.value = _uiState.value.copy(
                        changedLineCharts = newList,
                        zoomLevel = newZoom
                    )
                }
                GraphZoomLevel.MONTHS -> {
                    val newList = _uiState.value.lineCharts.groupBy {
                        it.date.with(TemporalAdjusters.firstDayOfMonth())
                    }.map { (weekStart, visits) -> LineChart(visits.sumOf { it.visitCount }, weekStart) }
                    _uiState.value = _uiState.value.copy(
                        changedLineCharts = newList,
                        zoomLevel = newZoom
                    )
                }
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