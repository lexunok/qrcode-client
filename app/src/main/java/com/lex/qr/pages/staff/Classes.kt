package com.lex.qr.pages.staff

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.lex.qr.R
import com.lex.qr.components.staff.BaseList
import com.lex.qr.pages.Page
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.Red
import com.lex.qr.ui.theme.Yellow
import com.lex.qr.utils.BaseItem
import com.lex.qr.utils.GetClassRequest
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import com.lex.qr.utils.formatDateTime
import com.lex.qr.viewmodels.ClassesViewModel
import com.lex.qr.viewmodels.CurrentClassesPage
import com.lex.qr.viewmodels.StatisticsViewModel
import kotlinx.coroutines.launch


@Composable
fun Classes(
    onToast: (String?) -> Unit,
    changeTitle: (String) -> Unit,
    changePage: (Page) -> Unit
) {
    val viewModel: ClassesViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    LaunchedEffect(Unit) {
        viewModel.getSubjectList()
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.ChangeTitle -> changeTitle(event.title)
                is UiEvent.ChangePage -> changePage(event.page)
                else -> {}
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        when(uiState.page) {
            CurrentClassesPage.SubjectList -> {
                BaseList(uiState.subjects) {
                    item: BaseItem -> viewModel.getGroupList(item as Subject)
                }
            }
            CurrentClassesPage.GroupList -> {
                BaseList(uiState.groups) {
                    item: BaseItem -> viewModel.getClasses(item.id)
                }
            }
            CurrentClassesPage.Classes -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.classes) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .clickable {viewModel.getStudents(item.publicId)}
                                .fillMaxWidth()
                                .padding(8.dp)
                                .border(
                                    width = 4.dp,
                                    color = Blue,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            ),
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                color = Blue,
                                text = formatDateTime(item.createdAt),
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                            )
                            item.rating?.let {
                                Text(
                                    textAlign = TextAlign.Center,
                                    color = Yellow,
                                    text = "Средний рейтинг: $it",
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            CurrentClassesPage.Visits -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.students) { item ->
                        val color = if (item.isActive) Green else Red
                        val rating = item.rating ?: 0

                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.avatarUrl)
                                .error(R.drawable.baseline_account_circle_24)
                                .placeholder(R.drawable.baseline_account_circle_24)
                                .build(),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .border(
                                    width = 4.dp,
                                    color = color,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = "Аватарка",
                                modifier = Modifier
                                    .padding(4.dp)
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
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    color = color,
                                    text = "${item.firstName} ${item.lastName}",
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                )
                                Text(
                                    textAlign = TextAlign.Center,
                                    color = Yellow,
                                    text = rating.toString(),
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}