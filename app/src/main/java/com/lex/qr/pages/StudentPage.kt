package com.lex.qr.pages

import android.annotation.SuppressLint
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lex.qr.R
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.NavButton
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.LightGray
import com.lex.qr.ui.theme.Red
import com.lex.qr.ui.theme.Yellow
import com.lex.qr.utils.API
import com.lex.qr.utils.Claims
import com.lex.qr.utils.ClassResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.GetClassResponse
import com.lex.qr.utils.JoinClassRequest
import com.lex.qr.utils.Rating
import com.lex.qr.utils.User
import com.lex.qr.utils.formatDateTime
import com.lex.qr.viewmodels.StudentViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.viewmodels.CurrentStudentPage
import com.lex.qr.viewmodels.UiEvent

@SuppressLint("HardwareIds")
@Composable
fun StudentPage(
    user: Claims,
    geolocationClient: GeolocationClient,
    lastLocation: String,
    onToast: (String?) -> Unit,
    changeTitle: (String) -> Unit,
    ) {
    Box (Modifier.fillMaxSize()) {

        val viewModel: StudentViewModel = viewModel()

        val device = Settings.Secure.getString(LocalContext.current.contentResolver, Settings.Secure.ANDROID_ID)

        val uiState by viewModel.uiState.collectAsState()

        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)

        val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
            if (result.contents != null && geolocationClient.checkGps() && lastLocation != "") {
                viewModel.joinClass(
                    JoinClassRequest(
                        publicId = result.contents,
                        studentGeolocation = lastLocation,
                        device = device
                    )
                )
            }
        }

        LaunchedEffect(Unit) {

            viewModel.uiEvent.collect { event ->
                when (event) {
                    is UiEvent.ShowToast -> onToast(event.message)
                    is UiEvent.ChangeTitle -> changeTitle(event.title)
                }
            }

            viewModel.getCurrent()
        }

        if (uiState.isLoading) {
            LoadingColumn(
                Modifier.fillMaxWidth().align(Alignment.Center),
                contentPadding = PaddingValues(16.dp)
            )
        }
        else {
            when (uiState.page) {
                CurrentStudentPage.MAIN -> {
                    if (uiState.currentClassId != null) {
                        if(uiState.currentRating == 0) {
                            Text(
                                color = Blue,
                                text = "Поставьте оценку",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp).align(Alignment.Center).offset(y =48.dp)
                            )
                        }
                        Row(modifier = Modifier.align(Alignment.Center)) {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= uiState.currentRating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Оценка $star",
                                    tint = if (star <= uiState.currentRating) Yellow else LightGray,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(4.dp)
                                        .clickable {viewModel.evaluate(star)}
                                )
                            }
                        }
                    }
                }
                CurrentStudentPage.VISITS -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 64.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.visits) { item ->
                            var color = Red
                            if (item.isActive) {
                                color = Green
                            }
                            var rating = 0
                            if (item.rating != null) {
                                rating = item.rating
                            }
                            Text(
                                textAlign = TextAlign.Center,
                                color = Blue,
                                text = formatDateTime(item.createdAt),
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .border(
                                        width = 4.dp,
                                        color = color,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                            ) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    color = color,
                                    text = item.subjectName,
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth(0.85f).padding(vertical = 8.dp)
                                )
                                Text(
                                    textAlign = TextAlign.Center,
                                    color = Yellow,
                                    text = rating.toString(),
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )
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
        ) { viewModel.getVisits(user.id) }

        NavButton(
            Modifier.align(Alignment.BottomCenter),
            R.drawable.baseline_qr_code_24,
            "QR Generator or Scan"
        ) {
            if (uiState.page == CurrentStudentPage.VISITS) {
                viewModel.toMain()
            }
            else {
                scanLauncher.launch(options)
            }
        }
    }
}