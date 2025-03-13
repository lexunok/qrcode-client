package com.lex.qr.pages

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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.lex.qr.ui.theme.LightGray
import com.lex.qr.utils.API
import com.lex.qr.utils.CreateGroupRequest
import com.lex.qr.utils.CreateSubjectRequest
import com.lex.qr.utils.CreateUserRequest
import com.lex.qr.utils.Group
import com.lex.qr.utils.Role
import com.lex.qr.utils.Subject
import com.lex.qr.utils.User
import kotlinx.coroutines.launch

enum class CurrentAdminPage : Page {
    EDITOR, LIST, CATEGORY, MAIN, CREATE
}

private enum class ObjectType{
    SUBJECT, GROUP, USER, NULL
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
    changeTitle: (String) -> Unit
) {
    val getUsersScope = rememberCoroutineScope()
    val getGroupsScope = rememberCoroutineScope()
    val getSubjectsScope = rememberCoroutineScope()

    val postObjectScope = rememberCoroutineScope()
    val putObjectScope = rememberCoroutineScope()
    val deleteObjectScope = rememberCoroutineScope()

    var page by remember { mutableStateOf(CurrentAdminPage.MAIN) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

    var selectedUser by remember { mutableStateOf<User?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    var isLoading by remember { mutableStateOf(false) }

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
                                var email by remember { mutableStateOf(user.email) }
                                var firstName by remember { mutableStateOf(user.firstName) }
                                var lastName by remember { mutableStateOf(user.lastName) }
                                var role by remember { mutableStateOf(user.role) }
                                CreatePageText("Почта")
                                CreatePageInput(email) { newEmail -> email = newEmail }
                                CreatePageText("Имя")
                                CreatePageInput(firstName) { newFirstName -> firstName = newFirstName }
                                CreatePageText("Фамилия")
                                CreatePageInput(lastName) { newLastName -> lastName = newLastName }
                                CreatePageText("Роль")
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
                            }
                            selectedGroup?.let { group ->
                                var name by remember { mutableStateOf(group.name) }
                                CreatePageText("Название")
                                CreatePageInput(name) { newName -> name = newName }
                                CreatePageButton("Сохранить") {
                                    putObjectScope.launch {
                                        api.updateGroup(group.id, CreateGroupRequest(name))
                                        groups = emptyList()
                                        selectedGroup = null
                                        page = CurrentAdminPage.CATEGORY
                                    }
                                }
                                CreatePageButton("Удалить") {
                                    deleteObjectScope.launch {
                                        api.deactivateGroup(group.id)
                                        groups = emptyList()
                                        selectedGroup = null
                                        page = CurrentAdminPage.CATEGORY
                                    }
                                }
                            }
                            selectedSubject?.let { subject ->
                                var name by remember { mutableStateOf(subject.name) }
                                CreatePageText("Название")
                                CreatePageInput(name) { newName -> name = newName }
                                CreatePageButton("Сохранить") {
                                    putObjectScope.launch {
                                        api.updateSubject(subject.id, CreateSubjectRequest(name))
                                        subjects = emptyList()
                                        selectedSubject = null
                                        page = CurrentAdminPage.CATEGORY
                                    }
                                }
                                CreatePageButton("Удалить") {
                                    deleteObjectScope.launch {
                                        api.deactivateSubject(subject.id)
                                        subjects = emptyList()
                                        selectedSubject = null
                                        page = CurrentAdminPage.CATEGORY
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
                                            page = CurrentAdminPage.EDITOR
                                            changeTitle("Редактор пользователя")
                                            isLoading = false
                                        }
                                        is Group -> {
                                            isLoading = true
                                            selectedGroup = item
                                            page = CurrentAdminPage.EDITOR
                                            changeTitle("Редактор группы")
                                            isLoading = false
                                        }
                                        is Subject -> {
                                            isLoading = true
                                            selectedSubject = item
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
                                    val response = api.getUsers()
                                    response?.let {
                                        users = response
                                    }
                                    isLoading = false
                                }
                            }
                            AdminCardCategory("Группы") {
                                getGroupsScope.launch {
                                    page = CurrentAdminPage.LIST
                                    changeTitle("Группы")
                                    isLoading = true
                                    val response = api.getGroups()
                                    response?.let {
                                        groups = response
                                    }
                                    isLoading = false
                                }
                            }
                            AdminCardCategory("Предметы") {
                                getSubjectsScope.launch {
                                    page = CurrentAdminPage.LIST
                                    changeTitle("Предметы")
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
                CurrentAdminPage.MAIN -> {
                    Box(modifier = Modifier.fillMaxSize()) {

                    }
                }
                CurrentAdminPage.CREATE -> {
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    var firstName by remember { mutableStateOf("") }
                    var lastName by remember { mutableStateOf("") }
                    var role by remember { mutableStateOf(Role.STUDENT) }
                    var name by remember { mutableStateOf("") }
                    var selectedOption by remember { mutableStateOf(ObjectType.NULL) }

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
                                    CreatePageInput(email) { newEmail -> email = newEmail }
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
                                }
                                ObjectType.SUBJECT, ObjectType.GROUP -> {
                                    CreatePageText("Введите название")
                                    CreatePageInput(name) { newName -> name = newName }
                                }
                                ObjectType.NULL -> {}
                            }
                            CreatePageButton("Создать") {
                                postObjectScope.launch {
                                    when (selectedOption) {
                                        ObjectType.USER -> {
                                            api.createUser(
                                                CreateUserRequest(email, password, firstName, lastName, role.toString(), null)
                                            )
                                        }
                                        ObjectType.GROUP -> api.createGroup(CreateGroupRequest(name))
                                        ObjectType.SUBJECT -> api.createSubject(CreateSubjectRequest(name))
                                        ObjectType.NULL -> {}
                                    }
                                    page = CurrentAdminPage.MAIN
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
                page = CurrentAdminPage.CATEGORY
                changeTitle("Выберите категорию")
            }

            NavButton(
                Modifier.align(Alignment.BottomCenter),
                R.drawable.baseline_add_24,
                "Add new object"
            ) {
                page = CurrentAdminPage.CREATE
                changeTitle("Окно создания")
            }
        }
    }
}