package com.lex.qr.pages.staff

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.lex.qr.R
import com.lex.qr.components.FunctionalButton
import com.lex.qr.components.statistics.GroupBarChart
import com.lex.qr.components.statistics.GroupStatistics
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.SelectDates
import com.lex.qr.components.ShowSubjectList
import com.lex.qr.components.statistics.SubjectHistChart
import com.lex.qr.components.statistics.UserStatistics
import com.lex.qr.components.statistics.ZoomLineChart
import com.lex.qr.pages.Page
import com.lex.qr.pages.getPageTransitionSpec
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.Attendance
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.CurrentStatisticsPage
import com.lex.qr.viewmodels.StatisticsViewModel

@Composable
fun Statistics(
    onToast: (String) -> Unit,
    changeTitle: (String) -> Unit,
    changePage: (Page) -> Unit
) {
    val viewModel: StatisticsViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.ChangeTitle -> changeTitle(event.title)
                is UiEvent.ChangePage -> changePage(event.page)
                else -> {}
            }
        }
    }
    IconButton(onClick = {viewModel.onBackPressed()}) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_40),
            contentDescription = "Go back",
            modifier = Modifier.size(40.dp),
            tint = Blue
        )
    }
    AnimatedContent(
        targetState = uiState.page,
        transitionSpec = {
            getPageTransitionSpec(initialState, targetState)
        },
        modifier = Modifier.fillMaxSize()
    ) { currentPage ->
        Box (Modifier.fillMaxSize()) {
            when (currentPage) {
                CurrentStatisticsPage.GroupList -> {
                    if (uiState.isLoading) {
                        LoadingColumn(
                            Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f),
                            contentPadding = PaddingValues(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(uiState.groups) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(
                                            width = 4.dp,
                                            color = Blue,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.getStudents(item) },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                ) {
                                    Text(
                                        textAlign = TextAlign.Center,
                                        color = Blue,
                                        text = item.name,
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                CurrentStatisticsPage.StudentList -> {
                    if (uiState.isLoading) {
                        LoadingColumn(
                            Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.75f),
                            contentPadding = PaddingValues(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.75f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(uiState.students) { item ->
                                val painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.avatarUrl)
                                        .error(R.drawable.baseline_account_circle_24)
                                        .placeholder(R.drawable.baseline_account_circle_24)
                                        .build(),
                                    contentScale = ContentScale.Crop
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(
                                            width = 4.dp,
                                            color = Blue,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            viewModel.getUserStatistics(item)
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                ) {
                                    Image(
                                        painter = painter,
                                        contentDescription = "Аватарка",
                                        modifier = Modifier
                                            .padding(top = 12.dp)
                                            .align(Alignment.CenterHorizontally)
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 2.dp,
                                                color = Blue,
                                                shape = CircleShape
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        textAlign = TextAlign.Center,
                                        color = Blue,
                                        text = "${item.firstName} ${item.lastName}",
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 12.dp)
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {viewModel.getGroupStatistics()},
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.7f).padding(bottom = 80.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                contentColor = Color.White,
                                containerColor = Blue
                            )
                        ) {
                            Text("Общая статистика", textAlign = TextAlign.Center, fontSize = 18.sp)
                        }
                    }
                    Button(
                        onClick = {viewModel.getGroupStatistics()},
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.7f).padding(bottom = 80.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Blue
                        )
                    ) {
                        Text("Общая статистика", textAlign = TextAlign.Center, fontSize = 18.sp)
                    }
                }
                CurrentStatisticsPage.UserStatistics -> {
                    val scrollState = rememberScrollState()
                    if (uiState.isLoading && !uiState.showDialog) {
                        Box (modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                color = Blue,
                                modifier = Modifier.size(100.dp).align(Alignment.Center),
                                strokeWidth = 12.dp
                            )
                        }
                    } else {
                        uiState.selectedStudent?.let { student ->
                            Column(
                                modifier = Modifier
                                    .padding(top = 64.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.9f)
                                    .verticalScroll(scrollState)
                            ) {
                                UserStatistics(student, uiState.attendance?: Attendance(0,0))
                                Spacer(Modifier.height(40.dp))
                                FunctionalButton(uiState.selectedSubject?.name ?: "Выберите предмет"){
                                    viewModel.getSubjectList()
                                }
                                ShowSubjectList(
                                    uiState.showDialog,
                                    uiState.isLoading,
                                    uiState.subjects,
                                    uiState.selectedSubject,
                                    {subject -> viewModel.setSelectedSubject(subject)},
                                    {viewModel.onBackPressed()}
                                )
                                SelectDates(
                                    uiState.dateFrom,
                                    uiState.dateTo,
                                    { text -> viewModel.changeDateFrom(text) },
                                    { text -> viewModel.changeDateTo(text) }
                                )
                                Spacer(Modifier.height(40.dp))

                                if (uiState.changedLineCharts.size > 1) {
                                    ZoomLineChart(
                                        uiState.zoomLevel,
                                        uiState.changedLineCharts
                                    ) { zoomSize -> viewModel.changeZoom(zoomSize) }
                                    Spacer(Modifier.height(40.dp))
                                }

                                if (uiState.selectedSubject == null && uiState.subjectsHist.isNotEmpty()) {
                                    SubjectHistChart(uiState.subjectsHist)
                                    Spacer(Modifier.height(40.dp))
                                }
                            }
                        }
                    }
                }
                CurrentStatisticsPage.GroupStatistics -> {
                    val scrollState = rememberScrollState()
                    if (uiState.isLoading && !uiState.showDialog) {
                        Box (modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                color = Blue,
                                modifier = Modifier.size(100.dp).align(Alignment.Center),
                                strokeWidth = 12.dp
                            )
                        }
                    } else {
                        uiState.selectedGroup?.let { group ->
                            Column(
                                modifier = Modifier
                                    .padding(top = 64.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.9f)
                                    .verticalScroll(scrollState)
                            ) {
                                GroupStatistics(group, uiState.attendance?: Attendance(0,0))
                                Spacer(Modifier.height(40.dp))
                                FunctionalButton(uiState.selectedSubject?.name ?: "Выберите предмет"){
                                    viewModel.getSubjectList()
                                }
                                ShowSubjectList(
                                    uiState.showDialog,
                                    uiState.isLoading,
                                    uiState.subjects,
                                    uiState.selectedSubject,
                                    {subject -> viewModel.setSelectedSubject(subject)},
                                    {viewModel.onBackPressed()}
                                )
                                SelectDates(
                                    uiState.dateFrom,
                                    uiState.dateTo,
                                    { text -> viewModel.changeDateFrom(text) },
                                    { text -> viewModel.changeDateTo(text) }
                                )

                                Spacer(Modifier.height(40.dp))
                                GroupBarChart(uiState.groupBars)
                                Spacer(Modifier.height(40.dp))

                                if (uiState.lineCharts.size > 1) {
                                    ZoomLineChart(
                                        uiState.zoomLevel,
                                        uiState.changedLineCharts
                                    ) { zoomSize -> viewModel.changeZoom(zoomSize) }
                                    Spacer(Modifier.height(40.dp))
                                }

                                if (uiState.selectedSubject == null && uiState.subjectsHist.isNotEmpty()) {
                                    SubjectHistChart(uiState.subjectsHist)
                                    Spacer(Modifier.height(40.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}