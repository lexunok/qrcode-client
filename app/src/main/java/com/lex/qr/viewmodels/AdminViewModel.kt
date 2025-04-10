package com.lex.qr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lex.qr.pages.Page
import com.lex.qr.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CurrentAdminPage : Page {
    Editor, Main, Create, Archive
}

data class AdminState(
    val page: CurrentAdminPage = CurrentAdminPage.Main
)

//решить, после обновления объектов получать новый список или изменять текущий список
//тоесть нужно определиться, возвращаться после EDITOR к LIST или CATEGORY,
//щас к MAIN так как удобно очищать данные

@HiltViewModel
class AdminViewModel @Inject constructor() : ViewModel(){
    private val _uiState = MutableStateFlow(AdminState())
    val uiState: StateFlow<AdminState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    //===навигация===

    fun onBackPressed(){
        viewModelScope.launch {
            when(_uiState.value.page) {
                CurrentAdminPage.Main -> {}
                CurrentAdminPage.Editor, CurrentAdminPage.Create, CurrentAdminPage.Archive -> {
                    _uiState.value = _uiState.value.copy(page = CurrentAdminPage.Main)
                    _uiEvent.send(UiEvent.ChangeTitle("Главная"))
                }
            }
        }
    }

    fun updatePage(newPage: Page) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = newPage as CurrentAdminPage)
        }
    }

    fun toCategory(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.Editor)
            _uiEvent.send(UiEvent.ChangeTitle("Выберите категорию"))
        }
    }

    fun toCreate(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.Create)
            _uiEvent.send(UiEvent.ChangeTitle("Окно создания"))
        }
    }

    fun toArchive(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(page = CurrentAdminPage.Archive)
            _uiEvent.send(UiEvent.ChangeTitle("Архив"))
        }
    }
}