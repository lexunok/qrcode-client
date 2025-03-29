package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.utils.API
import com.lex.qr.utils.Claims
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.LoginRequest
import com.lex.qr.utils.UiEvent
import com.lex.qr.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val api: API,
    private val userPrefs: UserPreferences
) : ViewModel() {

    private var _title = MutableStateFlow("Главная")
    val title: StateFlow<String> = _title.asStateFlow()

    private var _showMenu = MutableStateFlow(false)
    val showMenu: StateFlow<Boolean> = _showMenu.asStateFlow()

    private var _user = MutableStateFlow<Claims?>(null)
    val user: StateFlow<Claims?> = _user.asStateFlow()

    private var _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _isLoggedIn = MutableStateFlow(userPrefs.isLoggedIn())

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun showToast(message: String) {
        _toastMessage.value = message
    }
    fun clearToast() {
        _toastMessage.value = null
    }

    fun updateTitle(title: String) {
        viewModelScope.launch {
            _title.value = title
        }
    }
    fun updateUser(user: Claims?) {
        viewModelScope.launch {
            _user.value = user
        }
    }
    fun updateMenu(showMenu: Boolean) {
        viewModelScope.launch {
            _showMenu.value = showMenu
        }
    }

    fun login() {
        viewModelScope.launch {
            if (_isLoggedIn.value) {
                val (savedEmail, savedPassword) = userPrefs.getUser()

                val loginRequest = LoginRequest(
                    email = savedEmail ?: "",
                    password = savedPassword ?: ""
                )

                val response = api.login(loginRequest)

                response.fold(
                    onSuccess = {
                        _user.value = it
                        api.jwtToken = it.token
                        _isLoggedIn.value = true
                    },
                    onFailure = {
                        _isLoggedIn.value = false
                        _toastMessage.value = it.message
                    }
                )
            }
            _isLoading.value = false
        }
    }
}