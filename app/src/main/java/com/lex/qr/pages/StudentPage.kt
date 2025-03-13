package com.lex.qr.pages

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lex.qr.R
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.NavButton
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.Red
import com.lex.qr.utils.API
import com.lex.qr.utils.ClassResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.GetClassResponse
import com.lex.qr.utils.JoinClassRequest
import com.lex.qr.utils.User
import kotlinx.coroutines.launch

private enum class CurrentStudentPage: Page {
    MAIN, VISITS
}

@Composable
fun StudentPage(
    api: API,
    user: User,
    geolocationClient: GeolocationClient,
    lastLocation: String,
    changeTitle: (String) -> Unit,
) {
    Box (Modifier.fillMaxSize()) {

        val scanScope  = rememberCoroutineScope()
        val makeRequest = rememberCoroutineScope()

        var page by remember { mutableStateOf(CurrentStudentPage.MAIN) }

        var visits by remember { mutableStateOf<List<ClassResponse>>(emptyList()) }

        var currentClassId by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)

        val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
            if (result.contents != null && geolocationClient.checkGps() && lastLocation != "") {
                scanScope.launch {
                    isLoading = true
                    val response = api.joinClass(
                        JoinClassRequest(
                            publicId = result.contents,
                            studentId = user.id,
                            studentGeolocation = lastLocation
                        )
                    )
                    if (response != null) {
                        currentClassId = response.id
                    }
                    isLoading = false
                }
            }
        }

        if (isLoading) {
            LoadingColumn(
                Modifier
                    //.offset(x = listOffset)
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentPadding = PaddingValues(16.dp)
            )
        }
        else {
            when (page) {
                CurrentStudentPage.MAIN -> {
                    if (currentClassId != null) {
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.baseline_check_circle_outline_24
                            ),
                            contentDescription = "Scan is success",
                            tint = Green
                        )
                    }
                    else {
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.baseline_error_outline_24
                            ),
                            contentDescription = "Scan is fail",
                            tint = Red
                        )
                    }
                }
                CurrentStudentPage.VISITS -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 64.dp)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(visits) { item ->
                            var color = Red
                            if (item.isActive) {
                                color = Green
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .border(
                                        width = 4.dp,
                                        color = color,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                ),
                            ) {
                                Text(
                                    color = color,
                                    text = "${item.subjectName}:\n${item.createdAt}",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(16.dp)
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
        ) {
            makeRequest.launch {
                page = CurrentStudentPage.VISITS
                changeTitle("Посещения")
                isLoading = true
                val response = api.getVisits(user.id)
                response?.let {
                    visits = response
                }
                isLoading = false
            }
        }
        NavButton(
            Modifier.align(Alignment.BottomCenter),
            R.drawable.baseline_qr_code_24,
            "QR Generator or Scan"
        ) {
            if (page == CurrentStudentPage.VISITS) {
                page = CurrentStudentPage.MAIN
            }
            else {
                scanLauncher.launch(options)
            }
        }
    }
}