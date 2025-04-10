package com.lex.qr.pages.admin

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.components.FunctionalButton
import com.lex.qr.components.RadioSelect
import com.lex.qr.components.admin.CreatePageButton
import com.lex.qr.components.admin.CreatePageInput
import com.lex.qr.components.admin.CreatePageText
import com.lex.qr.components.admin.ShowGroupList
import com.lex.qr.pages.Page
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.Role
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.admin.CreateViewModel
import com.lex.qr.viewmodels.admin.ObjectType

@Composable
fun Create(
    onToast: (String) -> Unit,
    changeTitle: (String) -> Unit,
    changePage: (Page) -> Unit
){

    val viewModel: CreateViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.ChangeTitle -> changeTitle(event.title)
                is UiEvent.ChangePage -> changePage(event.page)
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
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
                uiState.selectedOption == ObjectType.USER,
                "Пользователь"
            ) { viewModel.changeSelectedOption(ObjectType.USER) }
            RadioSelect(
                uiState.selectedOption == ObjectType.USERS,
                "Пользователи(csv)"
            ) { viewModel.changeSelectedOption(ObjectType.USERS) }
            RadioSelect(
                uiState.selectedOption == ObjectType.GROUP,
                "Группа"
            ) { viewModel.changeSelectedOption(ObjectType.GROUP) }
            RadioSelect(
                uiState.selectedOption == ObjectType.SUBJECT,
                "Предмет"
            ) { viewModel.changeSelectedOption(ObjectType.SUBJECT) }
            when (uiState.selectedOption) {
                ObjectType.USER -> {
                    CreatePageText("Введите почту")
                    CreatePageInput(uiState.email){
                            newEmail -> viewModel.changeEmail(newEmail)
                    }
                    CreatePageText("Введите пароль")
                    CreatePageInput(uiState.password) {
                            newPassword -> viewModel.changePassword(newPassword)
                    }
                    CreatePageText("Введите имя")
                    CreatePageInput(uiState.firstName) {
                            newFirstName -> viewModel.changeFirstName(newFirstName)
                    }
                    CreatePageText("Введите фамилию")
                    CreatePageInput(uiState.lastName) {
                            newLastName -> viewModel.changeLastName(newLastName)
                    }
                    CreatePageText("Выберите роль")
                    RadioSelect(
                        uiState.role == Role.ADMIN,
                        "Админ"
                    ) { viewModel.changeRole(Role.ADMIN) }
                    RadioSelect(
                        uiState.role == Role.STAFF,
                        "Преподаватель"
                    ) { viewModel.changeRole(Role.STAFF) }
                    RadioSelect(
                        uiState.role == Role.STUDENT,
                        "Студент"
                    ) { viewModel.changeRole(Role.STUDENT) }
                    FunctionalButton(text = uiState.userGroup?.name ?: "Выбор группы") {
                        viewModel.getGroupList()
                    }
                    ShowGroupList(
                        uiState.showDialog,
                        uiState.isLoading,
                        uiState.groups,
                        uiState.selectedGroup,
                        {group -> viewModel.setSelectedGroup(group)},
                        {viewModel.onBackPressed()}
                    )
                }
                ObjectType.USERS -> {
                    val context = LocalContext.current
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent(),
                        onResult = { uri ->
                            uri?.let { selectedFileUri ->
                                viewModel.selectFile(context, selectedFileUri)
                            }
                        }
                    )
                    CreatePageButton("Выберите csv файл") {
                        launcher.launch("text/*")
                    }
                    if (uiState.isLoading || uiState.fileContent != null || uiState.errorMessage.isNotEmpty()){
                        CreatePageText(
                            when {
                                uiState.isLoading && uiState.fileContent == null -> "Загрузка..."
                                !uiState.isLoading && uiState.fileContent != null -> "Файл успешно загружен"
                                uiState.isLoading && uiState.fileContent != null -> "Отправка..."
                                uiState.errorMessage.isNotEmpty() -> uiState.errorMessage
                                else -> { "" }
                            }
                        )
                    }
                }
                ObjectType.SUBJECT, ObjectType.GROUP -> {
                    CreatePageText("Введите название")
                    CreatePageInput(uiState.name) {
                            newName -> viewModel.changeName(newName)
                    }
                }
                ObjectType.NULL -> {}
            }
            CreatePageButton(if (uiState.selectedOption != ObjectType.USERS) "Создать" else "Отправить") {
                when (uiState.selectedOption) {
                    ObjectType.USER -> viewModel.createUser()
                    ObjectType.GROUP -> viewModel.createGroup()
                    ObjectType.SUBJECT -> viewModel.createSubject()
                    ObjectType.USERS -> viewModel.createUsers()
                    ObjectType.NULL -> {}
                }
            }
        }
    }
}