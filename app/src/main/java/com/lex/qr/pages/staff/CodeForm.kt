package com.lex.qr.pages.staff

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.components.staff.BaseList
import com.lex.qr.pages.Page
import com.lex.qr.pages.getPageTransitionSpec
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.BaseItem
import com.lex.qr.utils.CodeResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.Group
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.CodeFormViewModel
import com.lex.qr.viewmodels.CurrentCodeFormPage


@Composable
fun CodeForm(
    geolocationClient: GeolocationClient,
    onToast: (String) -> Unit,
    changeTitle: (String) -> Unit,
    changePage: (Page) -> Unit,
    setCode: (CodeResponse) -> Unit
    ) {
    val viewModel: CodeFormViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.ChangeTitle -> changeTitle(event.title)
                is UiEvent.ChangePage -> changePage(event.page)
                is UiEvent.CodeCreated -> setCode(event.code)
                else -> {}
            }
        }
    }
    AnimatedContent(
        targetState = uiState.page,
        transitionSpec = {
            getPageTransitionSpec(initialState, targetState)
        },
        modifier = Modifier.fillMaxSize()
    ) { currentPage ->
        Box(modifier = Modifier.fillMaxSize()){
            when(currentPage) {
                CurrentCodeFormPage.SubjectList -> {
                    BaseList(uiState.subjects) {
                            item: BaseItem -> viewModel.getGroupList(item as Subject)
                    }
                }
                CurrentCodeFormPage.GroupList -> {
                    BaseList(uiState.groups) {
                            item: BaseItem -> viewModel.selectLifeTime(item as Group)
                    }
                }
                CurrentCodeFormPage.LifeTime -> {
                    val timeList = listOf(5, 10, 15, 20, 25, 30)
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 64.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(timeList) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .clickable {
                                        val isGpsEnabled = geolocationClient.checkGps(context)
                                        val lastLocation = geolocationClient.lastLocation
                                        viewModel.createCode(isGpsEnabled, lastLocation, item)
                                    }
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
                                    text = "$item минут",
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth()
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