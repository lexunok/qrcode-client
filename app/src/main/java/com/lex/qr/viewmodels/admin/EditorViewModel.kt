package com.lex.qr.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.CreateGroupRequest
import com.lex.qr.utils.CreateSubjectRequest
import com.lex.qr.utils.Group
import com.lex.qr.utils.Role
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import com.lex.qr.utils.UpdateUserRequest
import com.lex.qr.utils.User
import com.lex.qr.viewmodels.CurrentAdminPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CurrentEditorPage : Page {
    Category, ObjectList, Editor
}

data class EditorState(
    val page: CurrentEditorPage = CurrentEditorPage.Category,
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,

    val selectedUser: User? = null,
    val selectedGroup: Group? = null,
    val selectedSubject: Subject? = null,

    val users: List<User> = emptyList(),
    val groups: List<Group> = emptyList(),
    val subjects: List<Subject> = emptyList(),

    var email: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var role: Role = Role.STUDENT,
    val userGroup: Group? = null,
    var name: String = "",
)

@HiltViewModel
class EditorViewModel @Inject constructor(private val api: API) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorState())
    val uiState: StateFlow<EditorState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    //===очистка===
    private fun cleanModel(){
        _uiState.value = _uiState.value.copy(
            users = emptyList(),
            groups = emptyList(),
            subjects = emptyList(),
            selectedUser = null,
            selectedSubject = null,
            selectedGroup = null,
            email = "",
            firstName = "",
            lastName = "",
            role = Role.STUDENT,
            userGroup = null,
            name = "",
            page = CurrentEditorPage.Category
        )
    }

    //===навигация===

    fun onBackPressed(){
        viewModelScope.launch {
            when(_uiState.value.page){
                CurrentEditorPage.Category -> {
                    _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                    _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
                }
                CurrentEditorPage.ObjectList -> {
                    if (_uiState.value.users.isNotEmpty())
                        _uiState.value = _uiState.value.copy(users = emptyList())
                    if (_uiState.value.groups.isNotEmpty())
                        _uiState.value = _uiState.value.copy(groups = emptyList())
                    if (_uiState.value.subjects.isNotEmpty())
                        _uiState.value = _uiState.value.copy(subjects = emptyList())

                    _uiState.value = _uiState.value.copy(page = CurrentEditorPage.Category)
                    _uiEvent.send(UiEvent.ChangeTitle("Выберите категорию"))
                }
                CurrentEditorPage.Editor -> {
                    _uiState.value.selectedUser?.let {
                        _uiState.value = _uiState.value.copy(
                            selectedUser = null,
                            groups = emptyList(),
                            email = "",
                            firstName = "",
                            lastName = "",
                            role = Role.STUDENT,
                            userGroup = null,
                        )
                        _uiState.value = _uiState.value.copy(page = CurrentEditorPage.ObjectList)
                        _uiEvent.send(UiEvent.ChangeTitle("Пользователи"))
                    }
                    _uiState.value.selectedGroup?.let {
                        _uiState.value = _uiState.value.copy(
                            selectedGroup = null,
                            name = ""
                        )
                        _uiState.value = _uiState.value.copy(page = CurrentEditorPage.ObjectList)
                        _uiEvent.send(UiEvent.ChangeTitle("Группы"))
                    }
                    _uiState.value.selectedSubject?.let {
                        _uiState.value = _uiState.value.copy(
                            selectedSubject = null,
                            name = ""
                        )
                        _uiState.value = _uiState.value.copy(page = CurrentEditorPage.ObjectList)
                        _uiEvent.send(UiEvent.ChangeTitle("Предметы"))
                    }
                }
            }
        }
    }

    //===изменение значений===

    fun changeEmail(text: String?){
        _uiState.value = _uiState.value.copy(email = text ?: "")
    }

    fun changeFirstName(text: String?){
        _uiState.value = _uiState.value.copy(firstName = text ?: "")
    }

    fun changeLastName(text: String?){
        _uiState.value = _uiState.value.copy(lastName = text ?: "")
    }

    fun changeRole(role: Role){
        _uiState.value = _uiState.value.copy(role = role)
    }

    fun changeName(text: String?){
        _uiState.value = _uiState.value.copy(name = text ?: "")
    }

    //===выбор категории===

    fun selectUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentEditorPage.ObjectList, isLoading = true)
            _uiEvent.send(UiEvent.ChangeTitle("Пользователи"))
            val response = api.getUsers()
            response.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(users = it) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiState.value = _uiState.value.copy(page = CurrentEditorPage.Category)
                        _uiEvent.send(UiEvent.ChangeTitle("Выберите категорию"))
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentEditorPage.ObjectList, isLoading = true)
            _uiEvent.send(UiEvent.ChangeTitle("Группы"))
            val response = api.getGroups()
            response.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(groups = it) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiState.value = _uiState.value.copy(page = CurrentEditorPage.Category)
                        _uiEvent.send(UiEvent.ChangeTitle("Выберите категорию"))
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectSubjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentEditorPage.ObjectList, isLoading = true)
            _uiEvent.send(UiEvent.ChangeTitle("Предметы"))
            val response = api.getSubjects()
            response.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(subjects = it) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiState.value = _uiState.value.copy(page = CurrentEditorPage.Category)
                        _uiEvent.send(UiEvent.ChangeTitle("Выберите категорию"))
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    //===выбор объекта===

    fun selectUser(user: User){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(
                selectedUser = user,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role,
                userGroup = null
            )
            val response = user.groupId?.let { api.getGroup(it) }
            response?.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(userGroup = it) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(page = CurrentEditorPage.Editor, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Редактор пользователя"))
        }
    }

    fun selectGroup(group: Group){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(selectedGroup = group, name = group.name)
            _uiState.value = _uiState.value.copy(page = CurrentEditorPage.Editor, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Редактор группы"))
        }
    }

    fun selectSubject(subject: Subject){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(selectedSubject = subject, name = subject.name)
            _uiState.value = _uiState.value.copy(page = CurrentEditorPage.Editor, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Редактор предмета"))
        }
    }

    //===обновление объектов===

    fun updateUser(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.updateUser(
                _uiState.value.selectedUser!!.id,
                UpdateUserRequest(
                    _uiState.value.email,
                    " ",
                    _uiState.value.firstName,
                    _uiState.value.lastName,
                    _uiState.value.role.name,
                    _uiState.value.userGroup?.id
                )
            )
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
            cleanModel()
        }
    }

    fun updateGroup(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.updateGroup(
                _uiState.value.selectedGroup!!.id,
                CreateGroupRequest(_uiState.value.name)
            )
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
            cleanModel()
        }
    }

    fun updateSubject(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.updateSubject(
                _uiState.value.selectedSubject!!.id,
                CreateSubjectRequest(_uiState.value.name)
            )
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
            cleanModel()
        }
    }

    //===удаление объектов===

    fun deleteUser(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.deleteUser(_uiState.value.selectedUser!!.id)
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
            cleanModel()
        }
    }

    fun deleteGroup(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.deleteGroup(_uiState.value.selectedGroup!!.id)
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
            cleanModel()
        }
    }

    fun deleteSubject(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.deleteSubject(_uiState.value.selectedSubject!!.id)
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
            cleanModel()
        }
    }

    //===работа с Dialog списка групп===

    fun getGroupList(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showDialog = true, isLoading = true)
            val response = api.getGroups()
            response.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(groups = it) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiState.value = _uiState.value.copy(showDialog = false)
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun setSelectedGroup(group: Group?){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(userGroup = group, showDialog = false)
        }
    }
}