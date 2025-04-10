package com.lex.qr.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.R
import com.lex.qr.components.NavButton
import com.lex.qr.pages.admin.Archive
import com.lex.qr.pages.admin.Create
import com.lex.qr.pages.admin.Editor
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.AdminViewModel
import com.lex.qr.viewmodels.CurrentAdminPage


@Composable
fun AdminPage(
    onToast: (String) -> Unit,
    changeTitle: (String) -> Unit
) {
    val viewModel: AdminViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val changeAdminPage = { newPage: Page -> viewModel.updatePage(newPage) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.ChangeTitle -> changeTitle(event.title)
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        AnimatedContent(
            targetState = uiState.page,
            transitionSpec = {
                getPageTransitionSpec(initialState, targetState)
            },
            modifier = Modifier.fillMaxSize()
        ) { currentPage ->
            Box(modifier = Modifier.fillMaxSize()){
                when(currentPage) {
                    CurrentAdminPage.Editor -> {
                        Editor(onToast, changeTitle, changeAdminPage)
                    }
                    CurrentAdminPage.Main -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                        }
                    }
                    CurrentAdminPage.Create -> {
                        Create(onToast, changeTitle, changeAdminPage)
                    }
                    CurrentAdminPage.Archive -> {
                        Archive()
                    }
                }
            }
        }
        NavButton(
            Modifier.align(Alignment.BottomStart),
            R.drawable.baseline_format_list_bulleted_24,
            "List of objects"
        ) { viewModel.toCategory() }

        NavButton(
            Modifier.align(Alignment.BottomCenter),
            R.drawable.baseline_add_24,
            "Add new object"
        ) { viewModel.toCreate() }

        NavButton(
            Modifier.align(Alignment.BottomEnd),
            R.drawable.baseline_archive_24,
            "Archive"
        ) { viewModel.toArchive() }
    }
}