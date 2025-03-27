package com.lex.qr.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.CreateGroupRequest
import com.lex.qr.utils.CreateSubjectRequest
import com.lex.qr.utils.CreateUserRequest
import com.lex.qr.utils.Group
import com.lex.qr.utils.Role
import com.lex.qr.utils.Subject
import com.lex.qr.utils.UiEvent
import com.lex.qr.utils.UpdateUserRequest
import com.lex.qr.utils.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CurrentAdminPage : Page {
    EDITOR, LIST, CATEGORY, MAIN, CREATE
}

enum class ObjectType{
    SUBJECT, GROUP, USER, USERS, NULL
}

data class AdminState(
    val page: CurrentAdminPage = CurrentAdminPage.MAIN,
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val groups: List<Group> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val selectedUser: User? = null,
    val selectedGroup: Group? = null,
    val selectedSubject: Subject? = null,
    var selectedOption: ObjectType = ObjectType.NULL,
    var email: String = "",
    var password: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var role: Role = Role.STUDENT,
    val userGroup: Group? = null,
    var name: String = "",
    val fileContent: String? = null,
    val errorMessage: String = "",
    val showDialog: Boolean = false
)

//решить, после обновления объектов получать новый список или изменять текущий список
//тоесть нужно определиться, возвращаться после EDITOR к LIST или CATEGORY,
//щас к MAIN так как удобно очищать данные

@HiltViewModel
class AdminViewModel @Inject constructor(private val api: API) : ViewModel(){
    private val _uiState = MutableStateFlow(AdminState())
    val uiState: StateFlow<AdminState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    //===изменение значений===

    fun changeSelectedOption(objectType: ObjectType){
        _uiState.value = _uiState.value.copy(selectedOption = objectType)
    }

    fun changeEmail(text: String?){
        _uiState.value = _uiState.value.copy(email = text ?: "")
    }

    fun changePassword(text: String?){
        _uiState.value = _uiState.value.copy(password = text ?: "")
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

    //===переход к основным категориям===

    fun toCategory(){
        viewModelScope.launch {
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
                page = CurrentAdminPage.CATEGORY
            )
            _uiEvent.send(UiEvent.ChangeTitle("Выберите категорию"))
        }
    }

    fun toCreate(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                groups = emptyList(),
                email = "",
                password = "",
                firstName = "",
                lastName = "",
                role = Role.STUDENT,
                userGroup = null,
                name = "",
                page = CurrentAdminPage.CREATE
            )
            _uiEvent.send(UiEvent.ChangeTitle("Окно создания"))
        }
    }

    fun onBackPressed(){
        viewModelScope.launch {
            when(_uiState.value.page) {
                CurrentAdminPage.MAIN -> {}
                CurrentAdminPage.LIST -> {
                    when {
                        _uiState.value.users.isNotEmpty() ->
                            _uiState.value = _uiState.value.copy(users = emptyList())
                        _uiState.value.groups.isNotEmpty() ->
                            _uiState.value = _uiState.value.copy(groups = emptyList())
                        _uiState.value.subjects.isNotEmpty() ->
                            _uiState.value = _uiState.value.copy(subjects = emptyList())
                    }
                    _uiState.value = _uiState.value.copy(page = CurrentAdminPage.CATEGORY)
                    _uiEvent.send(UiEvent.ChangeTitle("Выберите категорию"))
                }
                CurrentAdminPage.EDITOR -> {
                    _uiState.value.selectedUser.let {
                        _uiState.value = _uiState.value.copy(page = CurrentAdminPage.LIST)
                        _uiEvent.send(UiEvent.ChangeTitle("Пользователи"))
                    }
                    _uiState.value.selectedGroup.let {
                        _uiState.value = _uiState.value.copy(page = CurrentAdminPage.LIST)
                        _uiEvent.send(UiEvent.ChangeTitle("Группы"))
                    }
                    _uiState.value.selectedSubject.let {
                        _uiState.value = _uiState.value.copy(page = CurrentAdminPage.LIST)
                        _uiEvent.send(UiEvent.ChangeTitle("Предметы"))
                    }
                }
                CurrentAdminPage.CATEGORY, CurrentAdminPage.CREATE -> {
                    _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN)
                    _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                }
            }
        }
    }

    //===выбор категории===

    fun selectUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.LIST, isLoading = true)
            _uiEvent.send(UiEvent.ChangeTitle("Пользователи"))
            val response = api.getUsers()
            response.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(users = it) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiState.value = _uiState.value.copy(page = CurrentAdminPage.CATEGORY)
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.LIST, isLoading = true)
            _uiEvent.send(UiEvent.ChangeTitle("Группы"))
            val response = api.getGroups()
            response.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(groups = it) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiState.value = _uiState.value.copy(page = CurrentAdminPage.CATEGORY)
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.LIST, isLoading = true)
            _uiEvent.send(UiEvent.ChangeTitle("Предметы"))
            val response = api.getSubjects()
            response.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(subjects = it) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiState.value = _uiState.value.copy(page = CurrentAdminPage.CATEGORY)
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.EDITOR, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Редактор пользователя"))
        }
    }

    fun selectGroup(group: Group){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(selectedGroup = group, name = group.name)
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.EDITOR, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Редактор группы"))
        }
    }

    fun selectSubject(subject: Subject){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(selectedSubject = subject, name = subject.name)
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.EDITOR, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Редактор предмета"))
        }
    }

    //===создание объектов===

    fun createUser(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.createUser(
                CreateUserRequest(
                    _uiState.value.email,
                    _uiState.value.password,
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
        }
    }

    //проверить!!!
    fun createUsers(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.createUsers(_uiState.value.fileContent!!)
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
        }
    }

    fun createGroup(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.createGroup(CreateGroupRequest(_uiState.value.name))
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
        }
    }

    fun createSubject(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = api.createSubject(CreateSubjectRequest(_uiState.value.name))
            response.fold(
                onSuccess = { _uiEvent.send(UiEvent.ShowToast("Успешно")) },
                onFailure = { error ->
                    error.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
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
                    _uiState.value.password,
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
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
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.MAIN, isLoading = false)
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
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

    //===работа с файлами===

    //проверить!!!
    fun selectFile(context: Context, uri: Uri){
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    fileContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Ошибка чтения файла",
                    fileContent = null
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}