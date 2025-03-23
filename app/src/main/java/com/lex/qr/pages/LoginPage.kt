package com.lex.qr.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.R
import com.lex.qr.components.Title
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.LightGray
import com.lex.qr.utils.Claims
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.CurrentLoginPage
import com.lex.qr.viewmodels.LoginViewModel

@Composable
fun LoginPage(
    onToast: (String?) -> Unit,
    onUserAcc: (Claims) -> Unit
) {
    val viewModel: LoginViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.Login -> onUserAcc(event.claims)
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
    ) {
        Title(uiState.title, Modifier.fillMaxWidth())
        Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp))
        {
            when(uiState.page) {
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
                    LoginButton("Войти", Modifier.align(Alignment.BottomCenter)){ viewModel.login() }
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
                    LoginButton("Далее", Modifier.align(Alignment.BottomCenter)){ viewModel.toNewPassword() }
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
                    LoginButton("Сохранить", Modifier.align(Alignment.BottomCenter)){ viewModel.updatePassword() }
                }
            }
        }
    }
}

@Composable
private fun LoginText(text: String){
    Text(
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        text = text,
        color = Blue,
        textAlign = TextAlign.Center,
        fontSize = 20.sp
    )
}

@Composable
private fun LoginTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
){
    OutlinedTextField (
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = LightGray,
            focusedLabelColor = Blue,
            unfocusedLabelColor = Color.Transparent,
            cursorColor = Blue,
            focusedTextColor = Blue,
            unfocusedTextColor = Blue,
        ),
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation
    )
}

@Composable
private fun LoginClickableText(
    text: String,
    modifier: Modifier
){
    Text(
        modifier = modifier
            .padding(end = 4.dp, bottom = 8.dp, top = 16.dp)
            .fillMaxWidth(),
        text = text,
        color = Blue,
        textAlign = TextAlign.End,
        fontSize = 16.sp,
        textDecoration = TextDecoration.Underline
    )
}

@Composable
private fun LoginButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
){
    Button(
        onClick = { onClick() },
        modifier = modifier.padding(bottom = 80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Blue
        ),
        contentPadding = PaddingValues(horizontal = 80.dp, vertical = 20.dp)
    ) {
        Text(text, fontSize = 20.sp)
    }
}
