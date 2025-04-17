package com.lex.qr.pages.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.R
import com.lex.qr.components.FunctionalButton
import com.lex.qr.components.LoadingColumn
import com.lex.qr.pages.Page
import com.lex.qr.pages.getPageTransitionSpec
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.admin.ArchiveViewModel
import com.lex.qr.viewmodels.admin.CurrentArchivePage

@Composable
fun Archive(
    onToast: (String) -> Unit,
    changeTitle: (String) -> Unit,
    changePage: (Page) -> Unit
){
    val viewModel: ArchiveViewModel = viewModel()
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
    ){ currentPage ->
        Box(modifier = Modifier.fillMaxSize()){
            when(currentPage){
                CurrentArchivePage.Semesters -> {
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
                            items(uiState.semesters) { item ->
                                val color = if (item.isActive) Blue else Color.Gray
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(
                                            width = 4.dp,
                                            color = color,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectSemester(item) },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                ) {
                                    Text(
                                        textAlign = TextAlign.Center,
                                        color = color,
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
                    Button(
                        onClick = { viewModel.archiveActiveSemester() },
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.7f).padding(bottom = 80.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Blue
                        )
                    ) {
                        Text("Архивировать активный семестр", textAlign = TextAlign.Center, fontSize = 18.sp)
                    }
                }
                CurrentArchivePage.Semester -> {
                    val scrollState = rememberScrollState()
                    if (uiState.isLoading) {
                        Box (modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                color = Blue,
                                modifier = Modifier.size(100.dp).align(Alignment.Center),
                                strokeWidth = 12.dp
                            )
                        }
                    } else {
                        uiState.selectedSemester?.let { semester ->
                            Column(
                                modifier = Modifier
                                    .padding(top = 64.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.9f)
                                    .verticalScroll(scrollState)
                            ) {
                                Text(
                                    text = semester.name,
                                    textAlign = TextAlign.Start,
                                    fontSize = 20.sp,
                                    color = Blue,
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                )
                                Text(
                                    text = "Статус: ${if (semester.isActive) "Активный" else "В архиве"}",
                                    textAlign = TextAlign.Start,
                                    fontSize = 20.sp,
                                    color = Blue,
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                )
                                Spacer(Modifier.height(40.dp))
                                FunctionalButton("Предметы"){
                                    viewModel.getSemestersSubjects()
                                }
                                FunctionalButton("Группы"){
                                    viewModel.getSemestersGroups()
                                }
                            }
                        }
                    }
                }
                CurrentArchivePage.Subjects, CurrentArchivePage.Groups -> {
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
                            items(if (uiState.page == CurrentArchivePage.Subjects) uiState.subjects
                                    else uiState.groups) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(
                                            width = 4.dp,
                                            color = Blue,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
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
            }
        }
    }
}