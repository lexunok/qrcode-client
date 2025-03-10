package com.lex.qr.pages


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.lex.qr.components.AdminCardCategory
import com.lex.qr.components.LazyListContent
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.NavButton
import com.lex.qr.components.RadioSelect
import com.lex.qr.components.Title
import com.lex.qr.components.getTransitionDirection
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.LightGray
import com.lex.qr.ui.theme.Red
import com.lex.qr.utils.API
import com.lex.qr.utils.CreateClassRequest
import com.lex.qr.utils.CreateClassResponse
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.Group
import com.lex.qr.utils.JoinClassRequest
import com.lex.qr.utils.LoginRequest
import com.lex.qr.utils.Role
import com.lex.qr.utils.Student
import com.lex.qr.utils.Subject
import com.lex.qr.utils.User
import com.lex.qr.utils.UserPreferences
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

interface Page

enum class StaffPage : Page {
    MAIN, SUBJECT, GROUP
}
enum class AdminPage : Page {
    EDITOR, LIST, CATEGORY, MAIN, CREATE
}

enum class CreateType{
    SUBJECT, GROUP, NULL
}
enum class PageTransitionDirection {
    LEFT, RIGHT, UP, DOWN
}
@Composable
fun MainPage(
    api: API,
    geolocationClient: GeolocationClient,
    userPrefs: UserPreferences,
    user: User,
    key: String?,
    lastLocation: String,
    isLoading: Boolean,
    onLogout: (Boolean) -> Unit,
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
            .padding(8.dp)
            .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                var isPressed by remember { mutableStateOf(false) }

                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 1.1f else 1f,
                    animationSpec = tween(durationMillis = 50)
                )

                Title(title, Modifier.weight(8f))
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .scale(scale)
                        .size(64.dp)
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
                        modifier = Modifier.fillMaxSize(),
                        tint = Blue
                    )
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(color = Blue, modifier = Modifier.size(100.dp).align(Alignment.Center), strokeWidth = 12.dp)
        }

        NavButton(
            Modifier.align(Alignment.BottomEnd),
            R.drawable.baseline_logout_24,
            "Logout"
        ) {
            userPrefs.clearUser()
            onLogout(false)
        }

        when (user.role) {
            Role.ADMIN -> {
                val getUsersScope = rememberCoroutineScope()
                val getGroupsScope = rememberCoroutineScope()
                val getSubjectsScope = rememberCoroutineScope()

                var page by remember { mutableStateOf(AdminPage.MAIN) }
                var users by remember { mutableStateOf<List<User>>(emptyList()) }
                var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
                var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

                var selectedUser by remember { mutableStateOf<User?>(null) }
                var selectedGroup by remember { mutableStateOf<Group?>(null) }
                var selectedSubject by remember { mutableStateOf<Subject?>(null) }

                NavButton(
                    Modifier.align(Alignment.BottomStart),
                    R.drawable.baseline_format_list_bulleted_24,
                    "List of objects"
                ) {
                    users = emptyList()
                    groups = emptyList()
                    subjects = emptyList()
                    selectedUser = null
                    selectedSubject = null
                    selectedGroup = null
                    page = AdminPage.CATEGORY
                    title = "Выберите категорию"
                }

                NavButton(
                    Modifier.align(Alignment.BottomCenter),
                    R.drawable.baseline_add_24,
                    "Add new object"
                ) {
                    page = AdminPage.CREATE
                    title = "Окно создания"
                }

                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        when (getTransitionDirection(initialState, targetState)) {
                            PageTransitionDirection.LEFT -> {
                                (slideInHorizontally { width -> -width } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                            }
                            PageTransitionDirection.RIGHT -> {
                                (slideInHorizontally { width -> width } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                            }
                            PageTransitionDirection.UP -> {
                                (slideInVertically { height -> -height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> height } + fadeOut())
                            }
                            PageTransitionDirection.DOWN -> {
                                (slideInVertically { height -> height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { currentPage ->
                    when(currentPage) {
                        AdminPage.EDITOR -> {
                            Box(modifier = Modifier.fillMaxSize()) {

                            }
                        }
                        AdminPage.LIST -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if(isLoading){
                                    LoadingColumn(
                                        Modifier
                                            .padding(top = 64.dp)
                                            .fillMaxWidth(),
                                        contentPadding = PaddingValues(16.dp)
                                    )
                                }
                                if (!isLoading) {
                                    LazyListContent(
                                        items = when {
                                            users.isNotEmpty() -> users
                                            groups.isNotEmpty() -> groups
                                            subjects.isNotEmpty() -> subjects
                                            else -> emptyList()
                                        },
                                        Modifier
                                            .padding(top = 64.dp)
                                            .fillMaxWidth(),
                                        PaddingValues(16.dp),
                                        onItemClick = { item ->
                                            when (item) {
                                                is User -> {
                                                    onLoading(true)
                                                    selectedUser = item
                                                    page = AdminPage.EDITOR
                                                    title = "Редактор пользователя"
                                                    onLoading(false)
                                                }
                                                is Group -> {
                                                    onLoading(true)
                                                    selectedGroup = item
                                                    page = AdminPage.EDITOR
                                                    title = "Редактор группы"
                                                    onLoading(false)
                                                }
                                                is Subject -> {
                                                    onLoading(true)
                                                    selectedSubject = item
                                                    page = AdminPage.EDITOR
                                                    title = "Редактор предмета"
                                                    onLoading(false)
                                                }
                                            }
                                        }
                                    ) { item ->
                                        Text(
                                            color = Blue,
                                            text = when (item) {
                                                is User -> "${item.firstName} ${item.lastName}"
                                                is Group -> item.name
                                                is Subject -> item.name
                                                else -> ""
                                            },
                                            fontSize = 24.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        AdminPage.CATEGORY -> {
                            Box(modifier = Modifier.fillMaxSize()){
                                Column(
                                    modifier = Modifier
                                        .padding(top = 64.dp)
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    AdminCardCategory("Пользователи") {
                                        getUsersScope.launch {
                                            page = AdminPage.LIST
                                            title = "Пользователи"
                                            onLoading(true)
                                            val response = api.getUsers()
                                            response?.let {
                                                users = response
                                            }
                                            onLoading(false)
                                        }
                                    }
                                    AdminCardCategory("Группы") {
                                        getGroupsScope.launch {
                                            page = AdminPage.LIST
                                            title = "Группы"
                                            onLoading(true)
                                            val response = api.getGroups()
                                            response?.let {
                                                groups = response
                                            }
                                            onLoading(false)
                                        }
                                    }
                                    AdminCardCategory("Предметы") {
                                        getSubjectsScope.launch {
                                            page = AdminPage.LIST
                                            title = "Предметы"
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
                        AdminPage.MAIN -> {
                            Box(modifier = Modifier.fillMaxSize()) {

                            }
                        }
                        AdminPage.CREATE -> {
                            var text by remember { mutableStateOf("") }
                            var selectedOption by remember { mutableStateOf(CreateType.NULL) }

                            var isPressed by remember { mutableStateOf(false) }

                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 1.1f else 1f,
                                animationSpec = tween(durationMillis = 50)
                            )

                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth(0.85f)
                                    .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Введите название",
                                        color = Blue,
                                        textAlign = TextAlign.Center,
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                    )
                                    OutlinedTextField(
                                        value = text,
                                        onValueChange = { newText -> text = newText },
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                            .background(Color.Transparent),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Blue,
                                            unfocusedBorderColor = LightGray,
                                            focusedLabelColor = Blue,
                                            unfocusedLabelColor = Color.Transparent,
                                            cursorColor = Blue,
                                            focusedTextColor = Blue,
                                            unfocusedTextColor = Blue,
                                        ),
                                        singleLine = true,
                                    )
                                    Text(
                                        text = "Выберите тип",
                                        color = Blue,
                                        textAlign = TextAlign.Center,
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                    )
                                    RadioSelect(
                                        selectedOption == CreateType.GROUP,
                                        "Группа"
                                    ) { selectedOption = CreateType.GROUP }
                                    RadioSelect(
                                        selectedOption == CreateType.SUBJECT,
                                        "Предмет"
                                    ) { selectedOption = CreateType.SUBJECT }
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                            .scale(scale)
                                            .clickable { page = AdminPage.MAIN }
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onPress = {
                                                        isPressed = true
                                                        tryAwaitRelease()
                                                        isPressed = false
                                                        page = AdminPage.MAIN
                                                    }
                                                )
                                            }
                                            .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                                    ) {
                                        Text(
                                            text = "Создать",
                                            color = Blue,
                                            textAlign = TextAlign.Center,
                                            fontSize = 24.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                        )
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

                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        when (getTransitionDirection(initialState, targetState)) {
                            PageTransitionDirection.LEFT -> {
                                (slideInHorizontally { width -> -width } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                            }
                            PageTransitionDirection.RIGHT -> {
                                (slideInHorizontally { width -> width } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                            }
                            PageTransitionDirection.UP -> {
                                (slideInVertically { height -> -height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> height } + fadeOut())
                            }
                            PageTransitionDirection.DOWN -> {
                                (slideInVertically { height -> height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { currentPage ->
                    when(currentPage) {
                        StaffPage.MAIN -> {
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
                        StaffPage.SUBJECT -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if(isLoading){
                                    LoadingColumn(
                                        Modifier
                                            .padding(top = 64.dp)
                                            .fillMaxWidth(),
                                        contentPadding = PaddingValues(16.dp)
                                    )
                                }
                                if (subjects.isNotEmpty() && !isLoading) {
                                    LazyColumn(
                                        modifier = Modifier.padding(top = 64.dp).fillMaxWidth(),
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
                        StaffPage.GROUP -> {
                            Box(modifier = Modifier.fillMaxSize()){
                                if (isLoading) {
                                    LoadingColumn(
                                        Modifier
                                            .padding(top = 64.dp)
                                            .fillMaxWidth(),
                                        contentPadding = PaddingValues(16.dp)
                                    )
                                }
                                if (groups.isNotEmpty() && !isLoading) {
                                    LazyColumn(
                                        modifier = Modifier.padding(top = 64.dp).fillMaxWidth(),
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