package com.lex.qr.pages

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.lex.qr.R
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.NavButton
import com.lex.qr.components.getTransitionDirection
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.Red
import com.lex.qr.ui.theme.Yellow
import com.lex.qr.utils.API
import com.lex.qr.utils.Claims
import com.lex.qr.utils.CreateClassRequest
import com.lex.qr.utils.CreateClassResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.GetClassRequest
import com.lex.qr.utils.GetClassResponse
import com.lex.qr.utils.Group
import com.lex.qr.utils.Student
import com.lex.qr.utils.Subject
import com.lex.qr.utils.User
import com.lex.qr.utils.avatarUrl
import com.lex.qr.utils.formatDateTime
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

enum class CurrentStaffPage: Page {
    QRCODE, SUBJECT, GROUP, CLASSES, VISITS, ACTIVITY
}

@Composable
fun StaffPage(
    api: API,
    user: Claims,
    geolocationClient: GeolocationClient,
    lastLocation: String,
    onToast: (String?) -> Unit,
    changeTitle: (String) -> Unit
) {
    val makeRequest = rememberCoroutineScope()

    var page by remember { mutableStateOf(CurrentStaffPage.QRCODE) }

    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }
    var classes by remember { mutableStateOf<List<GetClassResponse>>(emptyList()) }
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }

    var createClassResponse by remember { mutableStateOf<CreateClassResponse?>(null) }

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isListClicked by remember { mutableStateOf(false) }

//    AnimatedContent(
//        targetState = page,
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
//        modifier = Modifier.fillMaxSize()
//    ) { currentPage ->
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (page == CurrentStaffPage.QRCODE || page == CurrentStaffPage.ACTIVITY) {
                        if (dragAmount > 0 && createClassResponse!=null) {
                            page = CurrentStaffPage.ACTIVITY
                            changeTitle("Присутствующие")
                            createClassResponse?.let {
                                makeRequest.launch {
                                    isLoading = true
                                    val response = api.getStudents(it.publicId)
                                    response.fold(
                                        onSuccess = {
                                            students = it
                                        },
                                        onFailure = {
                                            onToast(it.message)
                                        }
                                    )
                                    isLoading = false
                                }
                            }
                            //Область видимости плохая
                        } else if (dragAmount < 0) {
                            page = CurrentStaffPage.QRCODE
                            changeTitle("Главная")
                        }
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
                when(page) {
                    CurrentStaffPage.QRCODE -> {
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
                    CurrentStaffPage.ACTIVITY -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(students) { item ->
                                var color = Red
                                var isActive by remember { mutableStateOf(item.isActive) }
                                if (isActive) {
                                    color = Green
                                }
                                var rating = 0
                                if (item.rating != null) {
                                    rating = item.rating
                                }
                                val painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data("$avatarUrl/${item.studentId}")
                                        .addHeader("Authorization", "Bearer ${api.getToken()}")
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
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .align(Alignment.CenterHorizontally)
                                                .clickable {
                                                    makeRequest.launch {
                                                        isLoading = true
                                                        val response = api.deactivateStudent(item.id)
                                                        response.fold(
                                                            onSuccess = {
                                                                isActive = it.isActive
                                                            },
                                                            onFailure = {
                                                                onToast(it.message)
                                                            }
                                                        )
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
                    CurrentStaffPage.SUBJECT -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(subjects) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .clickable {
                                            makeRequest.launch {
                                                page = CurrentStaffPage.GROUP
                                                changeTitle("Выберите группу")
                                                selectedSubject = item
                                                isLoading = true
                                                val response = api.getGroups()
                                                response.fold(
                                                    onSuccess = {
                                                        groups = it
                                                    },
                                                    onFailure = {
                                                        onToast(it.message)
                                                    }
                                                )
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
                                        textAlign = TextAlign.Center,
                                        color = Blue,
                                        text = item.name,
                                        fontSize = 18.sp,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                    CurrentStaffPage.GROUP -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f),
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
                                                        val response = api.getClasses(request)
                                                        response.fold(
                                                            onSuccess = {
                                                                classes = it
                                                            },
                                                            onFailure = {
                                                                onToast(it.message)
                                                            }
                                                        )
                                                    }
                                                    page = CurrentStaffPage.CLASSES
                                                    isLoading = false
                                                }
                                            } else if (geolocationClient.checkGps() && lastLocation != "") {
                                                makeRequest.launch {
                                                    isLoading = true
                                                    selectedSubject?.let { subject ->
                                                        val request = CreateClassRequest(
                                                            subjectId = subject.id,
                                                            groupId = item.id,
                                                            geolocation = lastLocation
                                                        )
                                                        val response = api.createClass(request)
                                                        response.fold(
                                                            onSuccess = {
                                                                createClassResponse = it
                                                            },
                                                            onFailure = {
                                                                onToast(it.message)
                                                            }
                                                        )
                                                    }
                                                    page = CurrentStaffPage.QRCODE
                                                    changeTitle("Главная")
                                                    isLoading = false
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
                                        textAlign = TextAlign.Center,
                                        color = Blue,
                                        text = item.name,
                                        fontSize = 18.sp,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                    CurrentStaffPage.CLASSES -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(classes) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .clickable {
                                            makeRequest.launch {
                                                page = CurrentStaffPage.VISITS
                                                changeTitle("Присутствующие")
                                                isLoading = true

                                                val response = api.getStudents(item.publicId)
                                                response.fold(
                                                    onSuccess = {
                                                        students = it
                                                    },
                                                    onFailure = {
                                                        onToast(it.message)
                                                    }
                                                )

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
                                        textAlign = TextAlign.Center,
                                        color = Blue,
                                        text = formatDateTime(item.createdAt),
                                        fontSize = 18.sp,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)
                                    )
                                    item.rating?.let {
                                        Text(
                                            textAlign = TextAlign.Center,
                                            color = Yellow,
                                            text = "Средний рейтинг: $it",
                                            fontSize = 18.sp,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    CurrentStaffPage.VISITS -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(students) { item ->
                                var color = Red
                                if (item.isActive) {
                                    color = Green
                                }
                                var rating = 0
                                if (item.rating != null) {
                                    rating = item.rating
                                }

                                val painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data("$avatarUrl/${item.studentId}")
                                        .addHeader("Authorization", "Bearer ${api.getToken()}")
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
                                            .border(width = 2.dp, color = Blue, shape = CircleShape),
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
            NavButton(
                Modifier.align(Alignment.BottomStart),
                R.drawable.baseline_format_list_bulleted_24,
                "List of Students"
            ) {
                makeRequest.launch {
                    isListClicked = true
                    page = CurrentStaffPage.SUBJECT
                    changeTitle("Выберите предмет")
                    isLoading = true
                    val response = api.getSubjects()
                    response.fold(
                        onSuccess = {
                            subjects = it
                        },
                        onFailure = {
                            onToast(it.message)
                        }
                    )
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
                    if (page != CurrentStaffPage.QRCODE) {
                        page = CurrentStaffPage.QRCODE
                        changeTitle("Главная")
                    }
                    else {
                        page = CurrentStaffPage.SUBJECT
                        changeTitle("Выберите предмет")
                        isLoading = true
                        val response = api.getSubjects()
                        response.fold(
                            onSuccess = {
                                subjects = it
                            },
                            onFailure = {
                                onToast(it.message)
                            }
                        )
                        isLoading = false
                    }
                }
            }
        }
}