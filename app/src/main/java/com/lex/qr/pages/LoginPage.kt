package com.lex.qr.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.R
import com.lex.qr.components.Title
import com.lex.qr.components.login.LoginButton
import com.lex.qr.components.login.LoginClickableText
import com.lex.qr.components.login.LoginText
import com.lex.qr.components.login.LoginTextInput
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.Claims
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.CurrentLoginPage
import com.lex.qr.viewmodels.LoginViewModel

@Composable
fun LoginPage(
    changeUser: (Claims?) -> Unit,
    onToast: (String) -> Unit,
) {
    val viewModel: LoginViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.ChangeUser -> changeUser(event.user)
                else -> {}
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 64.dp)
        .shadow(12.dp)
        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
        .padding(20.dp)
    ){
        Title(uiState.title, Modifier.fillMaxWidth())
        AnimatedContent(
            targetState = uiState.page,
            transitionSpec = {
                getPageTransitionSpec(initialState, targetState)
            },
            modifier = Modifier.fillMaxSize().padding(top = 80.dp)
        ) { currentPage ->
            Box(modifier = Modifier.fillMaxSize()) {
                when(currentPage) {
                    CurrentLoginPage.LOGIN -> {
                        Column {
                            LoginText("Email")
                            LoginTextInput(
                                uiState.email,
                                onValueChange = { text -> viewModel.changeEmail(text) }
                            )
                            LoginText("Пароль")
                            LoginTextInput(
                                uiState.password,
                                onValueChange = { text -> viewModel.changePassword(text) },
                                trailingIcon = {
                                    val id = if (uiState.isPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id),
                                        contentDescription = "Глазик",
                                        tint = Blue,
                                        modifier = Modifier.clickable {viewModel.changeVisibility()}
                                    )
                                },
                                visualTransformation = uiState.passwordVisual
                            )
                            LoginClickableText(
                                "Забыли пароль?",
                                Modifier.clickable(
                                    interactionSource = uiState.buttonInteraction,
                                    indication = null,
                                    onClick = { viewModel.toRecoveryPassword() }
                                )
                            )
                        }
                        LoginButton(
                            "Войти",
                            Modifier.align(Alignment.BottomCenter),
                            uiState.isLoading
                        ){ viewModel.login() }
                    }
                    CurrentLoginPage.PASSWORD_RECOVERY -> {
                        Column {
                            LoginText("Введите почту")
                            LoginTextInput(
                                uiState.recoveryEmail,
                                onValueChange = { text -> viewModel.changeRecoveryEmail(text) }
                            )
                            LoginClickableText(
                                "Вернуться на страницу входа",
                                Modifier.clickable(
                                    interactionSource = uiState.buttonInteraction,
                                    indication = null,
                                    onClick = { viewModel.toLogin() }
                                )
                            )
                        }
                        LoginButton(
                            "Далее",
                            Modifier.align(Alignment.BottomCenter),
                            uiState.isLoading
                        ){ viewModel.toNewPassword() }
                    }
                    CurrentLoginPage.PASSWORD_NEW -> {
                        Column {
                            LoginText("Введите код")
                            LoginTextInput(
                                uiState.recoveryCode,
                                onValueChange = { text -> viewModel.changeRecoveryCode(text) }
                            )
                            LoginText("Введите новый пароль")
                            LoginTextInput(
                                uiState.recoveryPassword,
                                onValueChange = { text -> viewModel.changeRecoveryPassword(text) },
                                trailingIcon = {
                                    val id = if (uiState.isPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id),
                                        contentDescription = "Глазик",
                                        tint = Blue,
                                        modifier = Modifier.clickable {viewModel.changeVisibility()}
                                    )
                                },
                                visualTransformation = uiState.passwordVisual
                            )
                            LoginClickableText(
                                "Вернуться на страницу входа",
                                Modifier.clickable(
                                    interactionSource = uiState.buttonInteraction,
                                    indication = null,
                                    onClick = { viewModel.toLogin() }
                                )
                            )
                        }
                        LoginButton(
                            "Сохранить",
                            Modifier.align(Alignment.BottomCenter),
                            uiState.isLoading
                        ){ viewModel.updatePassword() }
                    }
                }
            }
        }
    }
}
