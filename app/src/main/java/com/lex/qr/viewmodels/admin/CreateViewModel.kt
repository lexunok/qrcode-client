package com.lex.qr.viewmodels.admin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.utils.API
import com.lex.qr.utils.CreateGroupRequest
import com.lex.qr.utils.CreateSubjectRequest
import com.lex.qr.utils.CreateUserRequest
import com.lex.qr.utils.Group
import com.lex.qr.utils.Role
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.CurrentAdminPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ObjectType{
    SUBJECT, GROUP, USER, USERS, NULL
}

data class CreateState(
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,
    var selectedOption: ObjectType = ObjectType.NULL,
    val selectedGroup: Group? = null,

    var email: String = "",
    var password: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var role: Role = Role.STUDENT,
    val userGroup: Group? = null,
    var name: String = "",
    val errorMessage: String = "",
    val fileContent: String? = null,

    val groups: List<Group> = emptyList(),
)

@HiltViewModel
class CreateViewModel @Inject constructor(private val api: API) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateState())
    val uiState: StateFlow<CreateState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    //===навигация===

    fun onBackPressed(){
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
        }
    }

    //===изменение значений===

    fun changeSelectedOption(objectType: ObjectType){
        viewModelScope.launch {
            when(objectType){
                ObjectType.USER -> {
                    _uiState.value = _uiState.value.copy(
                        email = "",
                        password = "",
                        firstName = "",
                        lastName = "",
                        role = Role.STUDENT,
                        userGroup = null,
                        groups = emptyList()
                    )
                    _uiState.value = _uiState.value.copy(selectedOption = objectType)
                }
                ObjectType.USERS -> {
                    _uiState.value = _uiState.value.copy(
                        fileContent = null,
                        errorMessage = ""
                    )
                    _uiState.value = _uiState.value.copy(selectedOption = objectType)
                }
                ObjectType.GROUP, ObjectType.SUBJECT -> {
                    _uiState.value = _uiState.value.copy(name = "")
                    _uiState.value = _uiState.value.copy(selectedOption = objectType)
                }
                ObjectType.NULL -> {}
            }
        }
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
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiState.value = _uiState.value.copy(selectedOption = ObjectType.NULL)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
            _uiEvent.send(UiEvent.ChangeTitle("Главная"))
        }
    }

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
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiState.value = _uiState.value.copy(selectedOption = ObjectType.NULL)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
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
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiState.value = _uiState.value.copy(selectedOption = ObjectType.NULL)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
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
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiState.value = _uiState.value.copy(selectedOption = ObjectType.NULL)
            _uiEvent.send(UiEvent.ChangePage(CurrentAdminPage.Main))
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