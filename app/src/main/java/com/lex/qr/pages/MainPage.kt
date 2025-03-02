package com.lex.qr.pages


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lex.qr.R
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.NavButton
import com.lex.qr.components.RadioSelect
import com.lex.qr.components.Title
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.Red
import com.lex.qr.utils.API
import com.lex.qr.utils.CreateClassRequest
import com.lex.qr.utils.CreateClassResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.Group
import com.lex.qr.utils.JoinClassRequest
import com.lex.qr.utils.Role
import com.lex.qr.utils.Student
import com.lex.qr.utils.Subject
import com.lex.qr.utils.User
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

enum class StaffPage {
    MAIN, SUBJECT, GROUP
}
enum class AdminPage {
    MAIN, LIST, CREATE
}
enum class CreateType{
    SUBJECT, GROUP
}
@Composable
fun MainPage(
    api: API,
    geolocationClient: GeolocationClient,
    user: User,
    key: String?,
    lastLocation: String,
    isLoading: Boolean,
    onLogout: (User?) -> Unit,
    onLoading: (Boolean) -> Unit,
    changeKey: (String?) -> Unit
) {
    LaunchedEffect(Unit) {
        geolocationClient.checkGps()
    }

    var title by remember { mutableStateOf("${user.firstName} ${user.lastName}") }

    Box (modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 64.dp)
        .shadow(12.dp)
        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
        .padding(20.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var isPressed by remember { mutableStateOf(false) }

                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 1.1f else 1f,
                    animationSpec = tween(durationMillis = 50)
                )

                Title(title, Modifier.weight(6f).padding(end = 8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f) // 1/5 ширины
                        .scale(scale)
                        .aspectRatio(1f)
                        .background(Color.Transparent, CircleShape)
                        .clickable { user.role = Role.ADMIN }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                    user.role = Role.ADMIN
                                }
                            )
                        }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_account_circle_24),
                        contentDescription = "Кнопка профиля",
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        tint = Blue
                    )
                }
            }
        }

//        if (isLoading) {
//            CircularProgressIndicator(color = Blue, modifier = Modifier.size(100.dp).align(Alignment.Center), strokeWidth = 12.dp)
//        }

        NavButton(
            Modifier.align(Alignment.BottomEnd),
            R.drawable.baseline_logout_24,
            "Logout"
        ) { onLogout(null) }

        when (user.role) {
            Role.ADMIN -> {
                var page by remember { mutableStateOf(AdminPage.MAIN) }
                Crossfade(targetState = page, animationSpec = tween(durationMillis = 1000)) { currentPage ->
                    NavButton(
                        Modifier.align(Alignment.BottomStart),
                        R.drawable.baseline_format_list_bulleted_24,
                        "List of objects"
                    ) {
                        page = AdminPage.LIST
                    }

                    NavButton(
                        Modifier.align(Alignment.BottomCenter),
                        R.drawable.baseline_add_24,
                        "Add new object"
                    ) {
                        page = AdminPage.CREATE
                    }
                    when(currentPage) {
                        AdminPage.MAIN -> {
                            AnimatedVisibility(
                                visible = page == AdminPage.MAIN,
                                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                }
                            }
                        }
                        AdminPage.LIST -> {

                        }
                        AdminPage.CREATE -> {
                            AnimatedVisibility(
                                visible = page == AdminPage.CREATE,
                                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                var text by remember { mutableStateOf("") }
                                var isFocused by remember { mutableStateOf(false) }
                                var selectedOption by remember { mutableStateOf(CreateType.GROUP) }
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth(0.85f)
                                        .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Введите название",
                                            color = Blue,
                                            textAlign = TextAlign.Center,
                                            fontSize = 16.sp
                                        )
                                        OutlinedTextField(
                                            value = text,
                                            onValueChange = { newText -> text = newText },
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .fillMaxWidth(0.85f)
                                                .background(
                                                    if (isFocused) Color.Transparent else Color.LightGray,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .onFocusChanged { focusState ->
                                                    isFocused = focusState.isFocused
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Blue,
                                                unfocusedBorderColor = Color.Gray,
                                                focusedLabelColor = Blue,
                                                unfocusedLabelColor = Color.Gray,
                                                cursorColor = Blue,
                                                focusedTextColor = Blue,
                                                unfocusedTextColor = Color.Gray,
                                            ),
                                            singleLine = true,
                                        )
                                        Text(
                                            text = "Выберите тип",
                                            color = Blue,
                                            textAlign = TextAlign.Center,
                                            fontSize = 16.sp
                                        )
                                        RadioSelect(
                                            selectedOption == CreateType.GROUP,
                                            "Группа"
                                        ) { selectedOption = CreateType.GROUP }
                                        RadioSelect(
                                            selectedOption == CreateType.SUBJECT,
                                            "Предмет"
                                        ) { selectedOption = CreateType.SUBJECT }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Role.STAFF -> {
                val createClassScope = rememberCoroutineScope()
                val getSubjectsScope = rememberCoroutineScope()
                val getGroupsScope = rememberCoroutineScope()

                var createClassResponse by remember { mutableStateOf<CreateClassResponse?>(null) }
                var page by remember { mutableStateOf(StaffPage.MAIN) }
                var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
                var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

                var selectedSubject by remember { mutableStateOf<Subject?>(null) }
                var selectedGroup by remember { mutableStateOf<Group?>(null) }

                Crossfade(targetState = page, animationSpec = tween(durationMillis = 1000)) { currentPage ->
                    when(currentPage) {
                        StaffPage.MAIN -> {
                            AnimatedVisibility(
                                visible = page == StaffPage.MAIN,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(modifier = Modifier.fillMaxSize()){
                                    val getStudentsScope = rememberCoroutineScope()
                                    var students by remember { mutableStateOf<List<Student>>(emptyList()) }

                                    val qrOffset by animateDpAsState(
                                        targetValue = if ((students.isEmpty() && !isLoading) || page != StaffPage.MAIN) 0.dp else (-400).dp,
                                        animationSpec = tween(durationMillis = 300)
                                    )

                                    val listOffset by animateDpAsState(
                                        targetValue = if (students.isNotEmpty() || (isLoading && page == StaffPage.MAIN)) 0.dp else 400.dp,
                                        animationSpec = tween(durationMillis = 300)
                                    )

                                    if (key != null)
                                    {
                                        if (students.isEmpty()) {
                                            QrCodeView(
                                                data = key,
                                                modifier = Modifier
                                                    .offset(x = qrOffset)
                                                    .size(300.dp)
                                                    .align(Alignment.Center)
                                            )
                                        }
                                        if (isLoading) {
                                            LoadingColumn(
                                                Modifier
                                                    .offset(x = listOffset)
                                                    .fillMaxWidth()
                                                    .align(Alignment.Center),
                                                contentPadding = PaddingValues(16.dp)
                                            )
                                        }
                                        if (students.isNotEmpty()) {
                                            LazyColumn(
                                                modifier = Modifier
                                                    .offset(x = listOffset)
                                                    .fillMaxWidth()
                                                    .align(Alignment.Center),
                                                contentPadding = PaddingValues(16.dp)
                                            ) {
                                                items(students) { item ->
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp)
                                                            .border(width = 4.dp, color = Blue, shape = RoundedCornerShape(8.dp))
                                                        ,
                                                        elevation = CardDefaults.cardElevation(
                                                            defaultElevation = 4.dp
                                                        ),
                                                    ) {
                                                        var color = Color.Red
                                                        if (item.isActive) {
                                                            color = Green
                                                        }
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
                                    NavButton(
                                        Modifier.align(Alignment.BottomStart),
                                        R.drawable.baseline_format_list_bulleted_24,
                                        "List of Students"
                                    ) {
                                        getStudentsScope.launch {
                                            if (students.isNotEmpty()) {
                                                students = emptyList()
                                            }
                                            else {
                                                key?.let {
                                                    onLoading(true)
                                                    val response = api.getStudents(key)
                                                    response?.let {
                                                        students = response
                                                    }
                                                    onLoading(false)
                                                }
                                            }
                                        }
                                    }
                                    NavButton(
                                        Modifier.align(Alignment.BottomCenter),
                                        R.drawable.baseline_qr_code_24,
                                        "QR Generator or Scan"
                                    ) {
                                        getSubjectsScope.launch {
                                            page = StaffPage.SUBJECT
                                            title = "Выберите предмет"
                                            onLoading(true)
                                            val response = api.getSubjects()
                                            response?.let {
                                                subjects = response
                                            }
                                            onLoading(false)
                                        }
                                    }
                                }
                            }
                        }
                        StaffPage.SUBJECT -> {
                            AnimatedVisibility(
                                visible = page == StaffPage.SUBJECT,
                                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { -it }),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if(isLoading){
                                        LoadingColumn(
                                            Modifier
                                                .padding(top = 24.dp)
                                                .fillMaxWidth(),
                                            contentPadding = PaddingValues(16.dp)
                                        )
                                    }
                                    if (subjects.isNotEmpty() && !isLoading) {
                                        LazyColumn(
                                            modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                                            contentPadding = PaddingValues(16.dp)
                                        ) {
                                            items(subjects) { item ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    modifier = Modifier.clickable {
                                                        getGroupsScope.launch {
                                                            page = StaffPage.GROUP
                                                            title = "Выберите группу"
                                                            selectedSubject = item
                                                            onLoading(true)
                                                            val response = api.getGroups()
                                                            response?.let {
                                                                groups = response
                                                            }
                                                            onLoading(false)
                                                        }
                                                    }
                                                        .fillMaxWidth()
                                                        .padding(8.dp)
                                                        .border(width = 4.dp, color = Blue, shape = RoundedCornerShape(8.dp)),
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
                                }
                            }

                        }
                        StaffPage.GROUP -> {
                            AnimatedVisibility(
                                visible = page == StaffPage.GROUP,
                                enter = slideInHorizontally(initialOffsetX = { it }),
                                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxSize()){
                                    if (isLoading) {
                                        LoadingColumn(
                                            Modifier
                                                .padding(top = 24.dp)
                                                .fillMaxWidth(),
                                            contentPadding = PaddingValues(16.dp)
                                        )
                                    }
                                    if (groups.isNotEmpty() && !isLoading) {
                                        LazyColumn(
                                            modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                                            contentPadding = PaddingValues(16.dp)
                                        ) {
                                            items(groups) { item ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    modifier = Modifier.clickable {
                                                        if (geolocationClient.checkGps() && lastLocation != "") {
                                                            createClassScope.launch {
                                                                onLoading(true)
                                                                selectedGroup = item
                                                                val request = CreateClassRequest(
                                                                    staffId = user.id,
                                                                    subjectId = item.id,
                                                                    groupId = item.id,
                                                                    geolocation = lastLocation
                                                                )
                                                                val response: CreateClassResponse? =
                                                                    api.createClass(request)
                                                                response?.let {
                                                                    createClassResponse = response
                                                                    changeKey(createClassResponse!!.publicId)
                                                                }
                                                                page = StaffPage.MAIN
                                                                title = "${user.firstName} ${user.lastName}"
                                                                onLoading(false)
                                                            }
                                                        }
                                                    }
                                                        .fillMaxWidth()
                                                        .padding(8.dp)
                                                        .border(width = 4.dp, color = Blue, shape = RoundedCornerShape(8.dp)),
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
                                }
                            }
                        }
                    }
                }
            }
            Role.STUDENT -> {
                val scanScope  = rememberCoroutineScope()

                var isSuccessJoining by remember { mutableStateOf<Boolean?>(null) }

                val options = ScanOptions()
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                options.setPrompt("")
                options.setCameraId(0)
                options.setBeepEnabled(false)
                options.setBarcodeImageEnabled(true)

                val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
                    if (result.contents != null && geolocationClient.checkGps() && lastLocation != "") {
                        scanScope.launch {
                            onLoading(true)
                            val response = api.joinClass(
                                JoinClassRequest(
                                    classId = result.contents,
                                    studentId = user.id,
                                    studentGeolocation = lastLocation
                                )
                            )
                            if (response != null) {
                                isSuccessJoining = response.isSuccess
                            }
                            onLoading(false)
                        }
                    }
                }
                isSuccessJoining?.let {
                    if (isSuccessJoining as Boolean) {
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
                NavButton(
                    Modifier.align(Alignment.BottomCenter),
                    R.drawable.baseline_qr_code_24,
                    "QR Generator or Scan"
                ) { scanLauncher.launch(options) }
            }
        }
    }
}