package com.lex.qr.viewmodels.admin

import androidx.lifecycle.ViewModel
import com.lex.qr.pages.Page
import com.lex.qr.utils.API
import com.lex.qr.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

enum class CurrentArchivePage : Page {
    TermList, Term
}

data class ArchiveState(
    val page: CurrentArchivePage = CurrentArchivePage.TermList,
    val isLoading: Boolean = false,
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(private val api: API) : ViewModel(){
    private val _uiState = MutableStateFlow(ArchiveState())
    val uiState: StateFlow<ArchiveState> = _uiState

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()
}