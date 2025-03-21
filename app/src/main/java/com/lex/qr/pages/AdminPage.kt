package com.lex.qr.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.R
import com.lex.qr.components.AdminCardCategory
import com.lex.qr.components.LazyListContent
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.NavButton
import com.lex.qr.components.RadioSelect
import com.lex.qr.components.getTransitionDirection
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.LightGray
import com.lex.qr.utils.API
import com.lex.qr.utils.CreateGroupRequest
import com.lex.qr.utils.CreateSubjectRequest
import com.lex.qr.utils.CreateUserRequest
import com.lex.qr.utils.Group
import com.lex.qr.utils.Role
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UpdateUserRequest
import com.lex.qr.utils.User
import kotlinx.coroutines.launch

enum class CurrentAdminPage : Page {
    EDITOR, LIST, CATEGORY, MAIN, CREATE, SELECT_USER_GROUP
}

private enum class ObjectType{
    SUBJECT, GROUP, USER, USERS, NULL
}


@Composable
private fun CreatePageText(text: String) {
    Text(
        text = text,
        color = Blue,
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    )
}

@Composable
private fun CreatePageInput(
    value: String,
    suffix: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit
){
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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
        suffix = { suffix?.invoke() }
    )
}

@Composable
private fun CreatePageButton(
    text: String,
    onClick: () -> Unit
){
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        animationSpec = tween(durationMillis = 50)
    )

    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
            .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
    ) {
        Text(
            text = text,
            color = Blue,
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }
}

@Composable
fun AdminPage(
    api: API,
    onToast: (String?) -> Unit,
    changeTitle: (String) -> Unit
) {
    var page by remember { mutableStateOf(CurrentAdminPage.MAIN) }
    var isLoading by remember { mutableStateOf(false) }

    val getUsersScope = rememberCoroutineScope()
    val getGroupsScope = rememberCoroutineScope()
    val getGroupScope = rememberCoroutineScope()
    val getSubjectsScope = rememberCoroutineScope()

    val postObjectScope = rememberCoroutineScope()
    val putObjectScope = rememberCoroutineScope()
    val deleteObjectScope = rememberCoroutineScope()

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

    var selectedUser by remember { mutableStateOf<User?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    var selectedOption by remember { mutableStateOf(ObjectType.NULL) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.STUDENT) }
    var userGroup by remember { mutableStateOf<Group?>(null) }
    var name by remember { mutableStateOf("") }

    var fileContent by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()){
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
            Box(modifier = Modifier.fillMaxSize()){
                when(currentPage) {
                    CurrentAdminPage.EDITOR -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(0.85f)
                                .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                                .padding(10.dp)
                                .heightIn(max = 500.dp)
                                .verticalScroll(rememberScrollState())
                            ) {
                                selectedUser?.let { user ->
                                    getGroupScope.launch {
                                        val response = userGroup?.let { api.getGroup(it.id) }
                                        response?.fold(
                                            onSuccess = { userGroup = it },
                                            onFailure = { onToast(it.message) }
                                        )
                                    }
                                    CreatePageText("Почта")
                                    CreatePageInput(
                                        email,
                                        onValueChange = { newEmail -> email = newEmail }
                                    )
                                    CreatePageText("Имя")
                                    CreatePageInput(firstName) { newFirstName -> firstName = newFirstName }
                                    CreatePageText("Фамилия")
                                    CreatePageInput(lastName) { newLastName -> lastName = newLastName }
                                    CreatePageText("Роль")
                                    RadioSelect(
                                        role == Role.STAFF,
                                        "Преподаватель"
                                    ) { role = Role.STAFF }
                                    RadioSelect(
                                        role == Role.STUDENT,
                                        "Студент"
                                    ) { role = Role.STUDENT }
                                    CreatePageButton(text = userGroup?.name ?: "Выбор группы") {
                                        getGroupsScope.launch {
                                            page = CurrentAdminPage.SELECT_USER_GROUP
                                            changeTitle("Группы")
                                        }
                                    }
                                    CreatePageButton("Сохранить") {
                                        putObjectScope.launch {
                                            val response = api.updateUser(
                                                user.id,
                                                UpdateUserRequest(email, password, firstName, lastName, role.name,
                                                    userGroup?.id
                                                )
                                            )
                                            response.fold(
                                                onSuccess = { },
                                                onFailure = {
                                                    onToast(it.message)
                                                }
                                            )
                                            page = CurrentAdminPage.MAIN
                                            changeTitle("Главная")
                                        }
                                    }
                                    CreatePageButton("Удалить") {
                                        deleteObjectScope.launch {
                                            val response = api.deleteUser(user.id)
                                            response.fold(
                                                onSuccess = { },
                                                onFailure = {
                                                    onToast(it.message)
                                                }
                                            )
                                            page = CurrentAdminPage.MAIN
                                            changeTitle("Главная")
                                        }
                                    }
                                }
                                selectedGroup?.let { group ->
                                    CreatePageText("Название")
                                    CreatePageInput(name) { newName -> name = newName }
                                    CreatePageButton("Сохранить") {
                                        putObjectScope.launch {
                                            val response = api.updateGroup(group.id, CreateGroupRequest(name))
                                            response.fold(
                                                onSuccess = { },
                                                onFailure = {
                                                    onToast(it.message)
                                                }
                                            )
                                            page = CurrentAdminPage.MAIN
                                            changeTitle("Главная")
                                        }
                                    }
                                    CreatePageButton("Удалить") {
                                        deleteObjectScope.launch {
                                            val response = api.deleteGroup(group.id)
                                            response.fold(
                                                onSuccess = { },
                                                onFailure = {
                                                    onToast(it.message)
                                                }
                                            )
                                            page = CurrentAdminPage.MAIN
                                            changeTitle("Главная")
                                        }
                                    }
                                }
                                selectedSubject?.let { subject ->
                                    CreatePageText("Название")
                                    CreatePageInput(name) { newName -> name = newName }
                                    CreatePageButton("Сохранить") {
                                        putObjectScope.launch {
                                            val response = api.updateSubject(subject.id, CreateSubjectRequest(name))
                                            response.fold(
                                                onSuccess = { },
                                                onFailure = {
                                                    onToast(it.message)
                                                }
                                            )
                                            page = CurrentAdminPage.MAIN
                                            changeTitle("Главная")
                                        }
                                    }
                                    CreatePageButton("Удалить") {
                                        deleteObjectScope.launch {
                                            val response = api.deleteSubject(subject.id)
                                            response.fold(
                                                onSuccess = { },
                                                onFailure = {
                                                    onToast(it.message)
                                                }
                                            )
                                            page = CurrentAdminPage.MAIN
                                            changeTitle("Главная")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    CurrentAdminPage.LIST -> {
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
                                                isLoading = true
                                                selectedUser = item
                                                email = item.email.substringBefore("@")
                                                firstName = item.firstName
                                                lastName = item.lastName
                                                role = item.role
                                                userGroup = item.groupId?.let {
                                                    Group(it, "", "", false)
                                                }
                                                page = CurrentAdminPage.EDITOR
                                                changeTitle("Редактор пользователя")
                                                isLoading = false
                                            }
                                            is Group -> {
                                                isLoading = true
                                                selectedGroup = item
                                                name = item.name
                                                page = CurrentAdminPage.EDITOR
                                                changeTitle("Редактор группы")
                                                isLoading = false
                                            }
                                            is Subject -> {
                                                isLoading = true
                                                selectedSubject = item
                                                name = item.name
                                                page = CurrentAdminPage.EDITOR
                                                changeTitle("Редактор предмета")
                                                isLoading = false
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
                    CurrentAdminPage.CATEGORY -> {
                        Box(modifier = Modifier.fillMaxSize()){
                            Column(
                                modifier = Modifier
                                    .padding(top = 64.dp)
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                AdminCardCategory("Пользователи") {
                                    getUsersScope.launch {
                                        page = CurrentAdminPage.LIST
                                        changeTitle("Пользователи")
                                        isLoading = true
                                        try {
                                            val response = api.getUsers()
                                            response.fold(
                                                onSuccess = { users = it },
                                                onFailure = { onToast(it.message) }
                                            )
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                                AdminCardCategory("Группы") {
                                    getGroupsScope.launch {
                                        page = CurrentAdminPage.LIST
                                        changeTitle("Группы")
                                        isLoading = true
                                        try {
                                            val response = api.getGroups()
                                            response.fold(
                                                onSuccess = { groups = it },
                                                onFailure = { onToast(it.message) }
                                            )
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                                AdminCardCategory("Предметы") {
                                    getSubjectsScope.launch {
                                        page = CurrentAdminPage.LIST
                                        changeTitle("Предметы")
                                        isLoading = true
                                        try {
                                            val response = api.getSubjects()
                                            response.fold(
                                                onSuccess = { subjects = it },
                                                onFailure = { onToast(it.message) }
                                            )
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                    CurrentAdminPage.MAIN -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                        }
                    }
                    CurrentAdminPage.CREATE -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(0.85f)
                                .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                                .padding(10.dp)
                                .heightIn(max = 500.dp)
                                .verticalScroll(rememberScrollState())
                            ) {
                                CreatePageText("Выберите тип")
                                RadioSelect(
                                    selectedOption == ObjectType.USER,
                                    "Пользователь"
                                ) { selectedOption = ObjectType.USER }
                                RadioSelect(
                                    selectedOption == ObjectType.USERS,
                                    "Пользователи(csv)"
                                ) { selectedOption = ObjectType.USERS }
                                RadioSelect(
                                    selectedOption == ObjectType.GROUP,
                                    "Группа"
                                ) { selectedOption = ObjectType.GROUP }
                                RadioSelect(
                                    selectedOption == ObjectType.SUBJECT,
                                    "Предмет"
                                ) { selectedOption = ObjectType.SUBJECT }
                                when (selectedOption) {
                                    ObjectType.USER -> {
                                        CreatePageText("Введите почту")
                                        CreatePageInput(
                                            email,
                                            onValueChange = { newEmail -> email = newEmail }
                                        )
                                        CreatePageText("Введите пароль")
                                        CreatePageInput(password) { newPassword -> password = newPassword }
                                        CreatePageText("Введите имя")
                                        CreatePageInput(firstName) { newFirstName -> firstName = newFirstName }
                                        CreatePageText("Введите фамилию")
                                        CreatePageInput(lastName) { newLastName -> lastName = newLastName }
                                        CreatePageText("Выберите роль")
                                        RadioSelect(
                                            role == Role.ADMIN,
                                            "Админ"
                                        ) { role = Role.ADMIN }
                                        RadioSelect(
                                            role == Role.STAFF,
                                            "Преподаватель"
                                        ) { role = Role.STAFF }
                                        RadioSelect(
                                            role == Role.STUDENT,
                                            "Студент"
                                        ) { role = Role.STUDENT }
                                        CreatePageButton(text = userGroup?.name ?: "Выбор группы") {
                                            getGroupsScope.launch {
                                                getGroupsScope.launch {
                                                    page = CurrentAdminPage.SELECT_USER_GROUP
                                                    changeTitle("Группы")
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
                                        }
                                    }
                                    ObjectType.USERS -> {
                                        val context = LocalContext.current
                                        val launcher = rememberLauncherForActivityResult(
                                            contract = ActivityResultContracts.GetContent(),
                                            onResult = { uri ->
                                                uri?.let { selectedFileUri ->
                                                    try {
                                                        isLoading = true
                                                        fileContent = context.contentResolver.openInputStream(selectedFileUri)?.use { inputStream ->
                                                            inputStream.bufferedReader().use { it.readText() }
                                                        }
                                                    } catch (e: Exception) {
                                                        errorMessage = "Ошибка чтения файла"
                                                        fileContent = null
                                                    } finally {
                                                        isLoading = false
                                                    }
                                                }
                                            }
                                        )
                                        CreatePageButton("Выберите csv файл") {
                                            launcher.launch("text/*")
                                        }
                                        CreatePageText(
                                            when {
                                                isLoading && fileContent == null -> "Загрузка..."
                                                !isLoading && fileContent != null -> "Файл успешно загружен"
                                                isLoading && fileContent != null -> "Отправка..."
                                                errorMessage.isNotEmpty() -> errorMessage
                                                else -> { "" }
                                            }
                                        )
                                    }
                                    ObjectType.SUBJECT, ObjectType.GROUP -> {
                                        CreatePageText("Введите название")
                                        CreatePageInput(name) { newName -> name = newName }
                                    }
                                    ObjectType.NULL -> {}
                                }
                                CreatePageButton(if (selectedOption != ObjectType.USERS) "Создать" else "Отправить") {
                                    postObjectScope.launch {
                                        when (selectedOption) {
                                            ObjectType.USER -> {
                                                api.createUser(
                                                    CreateUserRequest(email, password, firstName, lastName,
                                                        role.name, userGroup?.id
                                                    )
                                                )
                                            }
                                            ObjectType.USERS -> {
                                                if (fileContent != null) {
                                                    isLoading = true
                                                    val response = api.createUsers(fileContent!!)
                                                    response.fold(
                                                        onSuccess = { onToast("Успешно") },
                                                        onFailure = { onToast(it.message) }
                                                    )
                                                    isLoading = false
                                                }
                                            }
                                            ObjectType.GROUP -> api.createGroup(CreateGroupRequest(name))
                                            ObjectType.SUBJECT -> api.createSubject(CreateSubjectRequest(name))
                                            ObjectType.NULL -> {}
                                        }
                                        page = CurrentAdminPage.MAIN
                                        changeTitle("Главная")
                                    }
                                }
                            }
                        }
                    }
                    CurrentAdminPage.SELECT_USER_GROUP -> {
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
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                                .border(width = 4.dp, color = if (userGroup?.id == item.id) Green else Blue, shape = RoundedCornerShape(8.dp))
                                                .clickable {
                                                    isLoading = true
                                                    userGroup = item
                                                    if (selectedUser == null) {
                                                        page = CurrentAdminPage.CREATE
                                                        changeTitle("Окно создания")
                                                    }
                                                    else {
                                                        page = CurrentAdminPage.EDITOR
                                                        changeTitle("Редактор пользователя")
                                                    }
                                                    isLoading = false
                                                },
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                color = if (userGroup?.id == item.id) Green else Blue,
                                                text = item.name,
                                                fontSize = 24.sp,
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
            email = ""
            firstName = ""
            lastName = ""
            role = Role.STUDENT
            userGroup = null
            name = ""
            page = CurrentAdminPage.CATEGORY
            changeTitle("Выберите категорию")
        }

        NavButton(
            Modifier.align(Alignment.BottomCenter),
            R.drawable.baseline_add_24,
            "Add new object"
        ) {
            groups = emptyList()
            email = ""
            password = ""
            firstName = ""
            lastName = ""
            role = Role.STUDENT
            userGroup = null
            name = ""
            page = CurrentAdminPage.CREATE
            changeTitle("Окно создания")
        }
    }
}