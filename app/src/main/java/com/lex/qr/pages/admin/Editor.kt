package com.lex.qr.pages.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.R
import com.lex.qr.components.FunctionalButton
import com.lex.qr.components.LazyListContent
import com.lex.qr.components.LoadingColumn
import com.lex.qr.components.RadioSelect
import com.lex.qr.components.admin.AdminCardCategory
import com.lex.qr.components.admin.CreatePageButton
import com.lex.qr.components.admin.CreatePageInput
import com.lex.qr.components.admin.CreatePageText
import com.lex.qr.components.admin.ShowGroupList
import com.lex.qr.pages.Page
import com.lex.qr.pages.getPageTransitionSpec
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.Group
import com.lex.qr.utils.Role
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import com.lex.qr.utils.User
import com.lex.qr.viewmodels.admin.CurrentEditorPage
import com.lex.qr.viewmodels.admin.EditorViewModel

@Composable
fun Editor(
    onToast: (String) -> Unit,
    changeTitle: (String) -> Unit,
    changePage: (Page) -> Unit,
){
    val viewModel: EditorViewModel = viewModel()
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

    IconButton(onClick = {viewModel.onBackPressed()}) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_40),
            contentDescription = "Go back",
            modifier = Modifier.size(40.dp),
            tint = Blue
        )
    }

    AnimatedContent(
        targetState = uiState.page,
        transitionSpec = {
            getPageTransitionSpec(initialState, targetState)
        },
        modifier = Modifier.fillMaxSize()
    ){ currentPage ->
        Box(modifier = Modifier.fillMaxSize()){
            when(currentPage){
                CurrentEditorPage.Editor -> {
                    if (
                        uiState.selectedUser != null
                        || uiState.selectedGroup != null
                        || uiState.selectedSubject != null
                    ){
                        Column(modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.85f)
                            .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState())
                        ) {
                            uiState.selectedUser?.let {
                                CreatePageText("Почта")
                                CreatePageInput(uiState.email) {
                                        newEmail -> viewModel.changeEmail(newEmail)
                                }
                                CreatePageText("Имя")
                                CreatePageInput(uiState.firstName) {
                                        newFirstName -> viewModel.changeFirstName(newFirstName)
                                }
                                CreatePageText("Фамилия")
                                CreatePageInput(uiState.lastName) {
                                        newLastName -> viewModel.changeLastName(newLastName)
                                }
                                CreatePageText("Роль")
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
                                CreatePageButton("Сохранить") {
                                    viewModel.updateUser()
                                }
                                CreatePageButton("Удалить") {
                                    viewModel.deleteUser()
                                }
                            }
                            uiState.selectedGroup?.let {
                                CreatePageText("Название")
                                CreatePageInput(uiState.name) {
                                        newName -> viewModel.changeName(newName)
                                }
                                CreatePageButton("Сохранить") {
                                    viewModel.updateGroup()
                                }
                                CreatePageButton("Удалить") {
                                    viewModel.deleteGroup()
                                }
                            }
                            uiState.selectedSubject?.let {
                                CreatePageText("Название")
                                CreatePageInput(uiState.name) {
                                        newName -> viewModel.changeName(newName)
                                }
                                CreatePageButton("Сохранить") {
                                    viewModel.updateSubject()
                                }
                                CreatePageButton("Удалить") {
                                    viewModel.deleteSubject()
                                }
                            }
                        }
                    }
                }
                CurrentEditorPage.ObjectList -> {
                    if(uiState.isLoading){
                        LoadingColumn(
                            Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        )
                    }
                    if (!uiState.isLoading) {
                        LazyListContent(
                            items = when {
                                uiState.users.isNotEmpty() -> uiState.users
                                uiState.groups.isNotEmpty() -> uiState.groups
                                uiState.subjects.isNotEmpty() -> uiState.subjects
                                else -> emptyList()
                            },
                            Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f),
                            PaddingValues(16.dp),
                            onItemClick = { item ->
                                when (item) {
                                    is User -> {
                                        viewModel.selectUser(item)
                                    }
                                    is Group -> {
                                        viewModel.selectGroup(item)
                                    }
                                    is Subject -> {
                                        viewModel.selectSubject(item)
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
                CurrentEditorPage.Category -> {
                    Column(
                        modifier = Modifier
                            .padding(top = 64.dp)
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        AdminCardCategory("Пользователи") {
                            viewModel.selectUsers()
                        }
                        AdminCardCategory("Группы") {
                            viewModel.selectGroups()
                        }
                        AdminCardCategory("Предметы") {
                            viewModel.selectSubjects()
                        }
                    }
                }
            }
        }
    }
}