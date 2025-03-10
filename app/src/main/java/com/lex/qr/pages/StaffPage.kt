package com.lex.qr.pages

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.R
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.NavButton
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.Red
import com.lex.qr.utils.API
import com.lex.qr.utils.CreateClassRequest
import com.lex.qr.utils.CreateClassResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.GetClassRequest
import com.lex.qr.utils.GetClassResponse
import com.lex.qr.utils.Group
import com.lex.qr.utils.Student
import com.lex.qr.utils.Subject
import com.lex.qr.utils.User
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

private enum class CurrentPage: Page {
    QRCODE, SUBJECT, GROUP, CLASSES, VISITS, ACTIVITY
}

@Composable
fun StaffPage(
    api: API,
    user: User,
    geolocationClient: GeolocationClient,
    lastLocation: String,
    changeTitle: (String) -> Unit
) {
    val makeRequest = rememberCoroutineScope()

    var page by remember { mutableStateOf(CurrentPage.QRCODE) }

    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }
    var classes by remember { mutableStateOf<List<GetClassResponse>>(emptyList()) }
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }

    var createClassResponse by remember { mutableStateOf<CreateClassResponse?>(null) }

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isListClicked by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = page,
//        transitionSpec = {
//            when (getTransitionDirection(initialState, targetState)) {
//                PageTransitionDirection.LEFT -> {
//                    (slideInHorizontally { width -> -width } + fadeIn())
//                        .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
//                }
//                PageTransitionDirection.RIGHT -> {
//                    (slideInHorizontally { width -> width } + fadeIn())
//                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
//                }
//                PageTransitionDirection.UP -> {
//                    (slideInVertically { height -> -height } + fadeIn())
//                        .togetherWith(slideOutVertically { height -> height } + fadeOut())
//                }
//                PageTransitionDirection.DOWN -> {
//                    (slideInVertically { height -> height } + fadeIn())
//                        .togetherWith(slideOutVertically { height -> -height } + fadeOut())
//                }
//            }
//        },
        modifier = Modifier.fillMaxSize()
    ) { currentPage ->
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 0) {
                        page = CurrentPage.ACTIVITY
                        changeTitle("Присутствующие")
                        createClassResponse?.let {
                            makeRequest.launch {
                                isLoading = true
                                api.getStudents(it.publicId)?.let { response ->
                                    students = response
                                }
                                isLoading = false
                            }
                        }
                    } else if (dragAmount < 0) {
                        page = CurrentPage.QRCODE
                        changeTitle("Главная")
                    }
                }
            }){
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
                when(currentPage) {
                    CurrentPage.QRCODE -> {
    //                    val qrOffset by animateDpAsState(
    //                        targetValue = if ((students.isEmpty() && !isLoading) || page != CurrentPage.MAIN) 0.dp else (-400).dp,
    //                        animationSpec = tween(durationMillis = 300)
    //                    )

    //                    val listOffset by animateDpAsState(
    //                        targetValue = if (students.isNotEmpty() || (isLoading && page == CurrentPage.MAIN)) 0.dp else 400.dp,
    //                        animationSpec = tween(durationMillis = 300)
    //                    )

                        createClassResponse?.let {
                                QrCodeView(
                                    data = it.publicId,
                                    modifier = Modifier
                                        //.offset(x = qrOffset)
                                        .size(300.dp)
                                        .align(Alignment.Center)
                                )
                        }
                    }
                    CurrentPage.ACTIVITY -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(students) { item ->
                                var color = Red
                                var isActive by remember { mutableStateOf(item.isActive) }
                                if (isActive) {
                                    color = Green
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .border(
                                            width = 4.dp,
                                            color = color,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Text(
                                        color = color,
                                        text = "${item.firstName} ${item.lastName}",
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(8.dp).fillMaxWidth(0.8f)
                                    )
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 8.dp, end = 8.dp, bottom = 8.dp)
                                                .clickable {
                                                    makeRequest.launch {
                                                        isLoading = true
                                                        val response = api.deactivateStudent(item.id)
                                                        response?.let {
                                                            isActive = response.isActive
                                                        }
                                                        isLoading = false
                                                    }
                                                }
                                        ) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_close_40),
                                                contentDescription = "Kick student from class",
                                                modifier = Modifier.fillMaxSize(),
                                                tint = Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    CurrentPage.SUBJECT -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(subjects) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .clickable {
                                            makeRequest.launch {
                                                page = CurrentPage.GROUP
                                                changeTitle("Выберите группу")
                                                selectedSubject = item
                                                isLoading = true
                                                val response = api.getGroups()
                                                response?.let {
                                                    groups = response
                                                }
                                                isLoading = false
                                            }
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
                                        color = Blue,
                                        text = item.name,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    CurrentPage.GROUP -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(groups) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .clickable {
                                            if (isListClicked) {
                                                makeRequest.launch {
                                                    changeTitle("Проведенные пары")
                                                    isLoading = true
                                                    selectedSubject?.let { subject ->
                                                        val request = GetClassRequest(
                                                            staffId = user.id,
                                                            subjectId = subject.id,
                                                            groupId = item.id,
                                                        )
                                                        try {
                                                            val response = api.getClasses(request)
                                                            response?.let {
                                                                classes = it
                                                            }
                                                        } catch (e: Exception) {
                                                            Log.i("ERROR", e.toString())
                                                        }
                                                    }
                                                    page = CurrentPage.CLASSES
                                                    isLoading = false
                                                }
                                            } else {
                                                if (geolocationClient.checkGps() && lastLocation != "") {
                                                    makeRequest.launch {
                                                        isLoading = true
                                                        selectedSubject?.let { subject ->
                                                            val request = CreateClassRequest(
                                                                staffId = user.id,
                                                                subjectId = subject.id,
                                                                groupId = item.id,
                                                                geolocation = lastLocation
                                                            )
                                                            val response: CreateClassResponse? =
                                                                api.createClass(request)
                                                            response?.let {
                                                                createClassResponse = it
                                                            }
                                                        }
                                                        page = CurrentPage.QRCODE
                                                        changeTitle("Главная")
                                                        isLoading = false
                                                    }
                                                }
                                            }
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
                                        color = Blue,
                                        text = item.name,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    CurrentPage.CLASSES -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(classes) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .clickable {
                                            makeRequest.launch {
                                                page = CurrentPage.VISITS
                                                changeTitle("Присутствующие")
                                                isLoading = true
                                                try {
                                                    val response = api.getStudents(item.publicId)
                                                    response?.let {
                                                        students = response
                                                    }
                                                } catch (e: Exception) {
                                                    Log.i("ERROR", e.toString())
                                                }
                                                isLoading = false
                                            }
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
                                        color = Blue,
                                        text = item.createdAt,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    CurrentPage.VISITS -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(students) { item ->
                                var color = Red
                                if (item.isActive) {
                                    color = Green
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .clickable {
                                            isListClicked = false
                                            page = CurrentPage.QRCODE
                                            changeTitle("Главная")
                                        }
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
                                        text = "${item.firstName} ${item.lastName}",
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
                    isListClicked = true
                    page = CurrentPage.SUBJECT
                    changeTitle("Выберите предмет")
                    isLoading = true
                    val response = api.getSubjects()
                    response?.let {
                        subjects = response
                    }
                    isLoading = false
                }
            }
            NavButton(
                Modifier.align(Alignment.BottomCenter),
                R.drawable.baseline_qr_code_24,
                "QR Generator or Scan"
            ) {
                makeRequest.launch {
                    isListClicked = false
                    if (page != CurrentPage.QRCODE) {
                        page = CurrentPage.QRCODE
                        changeTitle("Главная")
                    }
                    else {
                        page = CurrentPage.SUBJECT
                        changeTitle("Выберите предмет")
                        isLoading = true
                        val response = api.getSubjects()
                        response?.let {
                            subjects = response
                        }
                        isLoading = false
                    }
                }
            }
        }
    }
}