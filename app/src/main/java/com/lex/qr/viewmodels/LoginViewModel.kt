package com.lex.qr.viewmodels

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.ViewModel
import com.lex.qr.utils.API
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.LoginRequest
import com.lex.qr.utils.NewPasswordRequest
import com.lex.qr.utils.RecoveryPassword
import com.lex.qr.utils.UiEvent
import com.lex.qr.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CurrentLoginPage: Page {
    LOGIN, PASSWORD_RECOVERY, PASSWORD_NEW
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val passwordVisual: VisualTransformation = PasswordVisualTransformation(),
    val isPasswordVisible: Boolean = false,
    val page: CurrentLoginPage = CurrentLoginPage.LOGIN,
    val recoveryEmail: String = "",
    val recoveryCode: String = "",
    val recoveryPassword: String = "",
    val recoveryPasswordResponse: RecoveryPassword? = null,
    val title: String = "Вход",
    val buttonInteraction: MutableInteractionSource = MutableInteractionSource(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val api: API, private val userPrefs: UserPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun changePassword(text: String) {
        _uiState.value = _uiState.value.copy(password = text)
    }
    fun changeEmail(text: String) {
        _uiState.value = _uiState.value.copy(email = text)
    }
    fun changeRecoveryEmail(text: String) {
        _uiState.value = _uiState.value.copy(recoveryEmail = text)
    }
    fun changeRecoveryCode(text: String) {
        _uiState.value = _uiState.value.copy(recoveryCode = text)
    }
    fun changeRecoveryPassword(text: String) {
        _uiState.value = _uiState.value.copy(recoveryPassword = text)
    }
    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            if (_uiState.value.email.isNotEmpty() && _uiState.value.password.isNotEmpty()) {

                val response = api.login(LoginRequest(_uiState.value.email, _uiState.value.password))

                response.fold(
                    onSuccess = {
                        userPrefs.saveUser(_uiState.value.email, _uiState.value.password)
                        api.updateToken(it.token)
                        _uiEvent.send(UiEvent.Login(it))
                    },
                    onFailure = {
                        api.updateToken(null)
                        it.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun changeVisibility() {
        if (_uiState.value.isPasswordVisible) {
            _uiState.value = _uiState.value.copy(passwordVisual = PasswordVisualTransformation())
        }
        else {
            _uiState.value = _uiState.value.copy(passwordVisual = VisualTransformation.None)
        }
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }
    fun toLogin() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                page = CurrentLoginPage.LOGIN,
                title = "Вход"
            )
        }
    }
    fun toRecoveryPassword() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                page = CurrentLoginPage.PASSWORD_RECOVERY,
                title = "Восстановление пароля"
            )
        }
    }
    fun toNewPassword() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            if (_uiState.value.recoveryEmail.isNotEmpty()) {

                val response = api.sendCode(_uiState.value.recoveryEmail)

                response.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            page = CurrentLoginPage.PASSWORD_NEW,
                            recoveryPasswordResponse = it
                        )
                        _uiEvent.send(UiEvent.ShowToast("На указанную почту был отправлен код"))
                    },
                    onFailure = {
                        api.updateToken(null)
                        it.message?.let { msg ->
                            _uiEvent.send(UiEvent.ShowToast(msg))
                        }
                    }
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    fun updatePassword() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value.recoveryPasswordResponse?.let {
                if (_uiState.value.recoveryPassword.isNotEmpty() && _uiState.value.recoveryCode.isNotEmpty()) {

                    val response = api.updatePassword(
                        NewPasswordRequest(
                            id = it.id,
                            code = _uiState.value.recoveryCode,
                            password = _uiState.value.recoveryPassword
                        )
                    )

                    response.fold(
                        onSuccess = { res ->
                            if (_uiState.value.isPasswordVisible) {
                                _uiState.value = _uiState.value.copy(
                                    isPasswordVisible = false,
                                    passwordVisual = PasswordVisualTransformation()
                                )
                            }
                            _uiState.value = _uiState.value.copy(
                                page = CurrentLoginPage.LOGIN,
                                title = "Вход"
                            )
                            _uiEvent.send(UiEvent.ShowToast(res))
                        },
                        onFailure = { res ->
                            res.message?.let { msg ->
                                _uiEvent.send(UiEvent.ShowToast(msg))
                            }
                        }
                    )
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}