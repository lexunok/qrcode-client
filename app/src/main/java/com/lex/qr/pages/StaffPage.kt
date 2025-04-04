package com.lex.qr.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.NavButton
import com.lex.qr.pages.staff.Classes
import com.lex.qr.pages.staff.CodeForm
import com.lex.qr.pages.staff.Statistics
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.Red
import com.lex.qr.ui.theme.Yellow
import com.lex.qr.utils.CodeResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.CurrentStaffPage
import com.lex.qr.viewmodels.StaffViewModel
import com.lightspark.composeqr.QrCodeView


@Composable
fun StaffPage(
    geolocationClient: GeolocationClient,
    onToast: (String) -> Unit,
    changeTitle: (String) -> Unit
) {
    val viewModel: StaffViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    val changeStaffPage = { newPage: Page -> viewModel.updatePage(newPage) }
    val setCode = { code: CodeResponse -> viewModel.setCode(code) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.ChangeTitle -> changeTitle(event.title)
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        AnimatedContent(
            targetState = uiState.page,
            transitionSpec = {
                getPageTransitionSpec(initialState, targetState)
            },
            modifier = Modifier.fillMaxSize()
        ) { currentPage ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    LoadingColumn(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        contentPadding = PaddingValues(16.dp)
                    )
                } else {
                    when (currentPage) {
                        CurrentStaffPage.ClassList -> {
                            Classes(onToast, changeTitle, changeStaffPage)
                        }
                        CurrentStaffPage.Statistics -> {
                            Statistics(onToast, changeTitle, changeStaffPage)
                        }
                        CurrentStaffPage.CodeForm -> {
                            CodeForm(geolocationClient, onToast, changeTitle, changeStaffPage, setCode)
                        }
                        CurrentStaffPage.Main -> {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures { _, dragAmount ->
                                        if (dragAmount > 0) {
                                            viewModel.toActivity()
                                        }
                                    }
                                }
                            ){
                                uiState.code?.let {
                                    Column (Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                        QrCodeView(
                                            data = it.publicId,
                                            modifier = Modifier.size(300.dp)
                                        )
                                        Text(
                                            color = Blue,
                                            text = "Сделайте свайп вниз чтобы посмотреть присутствующих",
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        CurrentStaffPage.Activity -> {
                            LazyColumn(
                                modifier = Modifier
                                    .padding(top = 64.dp)
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.9f),
                                contentPadding = PaddingValues(16.dp),
                                state = viewModel.lazyListState
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center)
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
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = 2.dp,
                                                    color = Blue,
                                                    shape = CircleShape
                                                ),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(0.7f)
                                                .align(Alignment.CenterVertically)
                                        ){
                                            Text(
                                                textAlign = TextAlign.Center,
                                                color = color,
                                                text = item.firstName,
                                                fontSize = 18.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Text(
                                                textAlign = TextAlign.Center,
                                                color = color,
                                                text = item.lastName,
                                                fontSize = 18.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        Text(
                                            textAlign = TextAlign.Center,
                                            color = Yellow,
                                            text = rating.toString(),
                                            fontSize = 18.sp,
                                            modifier = Modifier.fillMaxWidth(0.1f).align(Alignment.CenterVertically)
                                        )
                                        if (item.isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .fillMaxWidth(1f)
                                                    .fillMaxHeight()
                                                    .align(Alignment.CenterVertically)
                                                    .clickable {
                                                        viewModel.deactivateStudent(item.id)
                                                    }
                                            ) {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_close_40),
                                                    contentDescription = "Kick student from class",
                                                    modifier = Modifier.fillMaxSize().size(40.dp),
                                                    tint = Red
                                                )
                                            }
                                        }
                                        else {
                                            Box(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .fillMaxWidth(1f)
                                                    .fillMaxHeight()
                                                    .align(Alignment.CenterVertically)
                                                    .clickable {
                                                        viewModel.activateStudent(item.id)
                                                    }
                                            ) {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_add_40),
                                                    contentDescription = "Add student to class",
                                                    modifier = Modifier.fillMaxSize().size(40.dp),
                                                    tint = Green
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
        }
        NavButton(
            Modifier.align(Alignment.BottomStart),
            R.drawable.baseline_format_list_bulleted_24,
            "List of Students"
        ) {viewModel.toClassList()}
        NavButton(
            Modifier.align(Alignment.BottomCenter),
            R.drawable.baseline_qr_code_24,
            "QR Generator or Scan"
        ) {viewModel.toMain()}
        NavButton(
            Modifier.align(Alignment.BottomEnd),
            R.drawable.baseline_show_chart_24,
            "Statistics"
        ) {viewModel.toStatistics()}
    }
}