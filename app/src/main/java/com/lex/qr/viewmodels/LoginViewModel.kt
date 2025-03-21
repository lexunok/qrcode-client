package com.lex.qr.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.ViewModel
import com.lex.qr.utils.API
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.ClassResponse
import com.lex.qr.utils.JoinClassRequest
import com.lex.qr.utils.LoginRequest
import com.lex.qr.utils.Rating
import com.lex.qr.utils.UiEvent
import com.lex.qr.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class LoginState(
    val email: String = "chernenkoag@std.tyuiu.ru",
    val password: String = "lexunok2505",
    val passwordVisual: VisualTransformation = PasswordVisualTransformation(),
    val isPasswordVisible: Boolean = false
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
    fun login() {
        viewModelScope.launch {
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
}