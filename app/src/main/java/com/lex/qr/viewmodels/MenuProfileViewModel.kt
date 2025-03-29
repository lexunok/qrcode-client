package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.utils.API
import com.lex.qr.utils.Claims
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
class MenuProfileViewModel @Inject constructor(private val api: API, private val userPrefs: UserPreferences) : ViewModel() {

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun logout() {
        viewModelScope.launch {
            api.jwtToken = null
            userPrefs.clearUser()
            _uiEvent.send(UiEvent.ChangeUser(null))
        }
    }

    fun uploadAvatar(user: Claims, bytes: ByteArray) {
        viewModelScope.launch {
            val response = api.uploadAvatar(bytes)
            response.fold(
                onSuccess = {
                    _uiEvent.send(UiEvent.ShowToast("Успешно"))
                    _uiEvent.send(UiEvent.ChangeUser(user.copy(avatarUrl = it)))
                },
                onFailure = {
                    it.message?.let { msg ->
                        _uiEvent.send(UiEvent.ShowToast(msg))
                    }
                }
            )
        }
    }
}