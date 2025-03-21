package com.lex.qr.pages

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.R
import com.lex.qr.utils.API
import com.lex.qr.components.Title
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.LightGray
import com.lex.qr.utils.Claims
import com.lex.qr.utils.LoginRequest
import com.lex.qr.utils.User
import com.lex.qr.utils.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    api: API,
    userPrefs: UserPreferences,
    onLogin: (Boolean) -> Unit,
    onToast: (String?) -> Unit,
    toUserAcc: (Claims) -> Unit
) {
    var email by remember { mutableStateOf("chernenkoag@std.tyuiu.ru") }
    var password by remember { mutableStateOf("lexunok2505") }
    var passwordVisual by remember { mutableStateOf<VisualTransformation>(PasswordVisualTransformation()) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val makeRequest = rememberCoroutineScope()

    Box (modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 64.dp)
        .shadow(12.dp)
        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
        .padding(20.dp)
    ) {
        Column {
            Title("Вход", Modifier.fillMaxWidth().padding(bottom = 80.dp))
            Text(
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                text = "Email",
                color = Blue,
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
            OutlinedTextField (
                value = email,
                onValueChange = { newText -> email = newText },
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
                singleLine = true,
            )
            Text(
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 16.dp),
                text = "Пароль",
                color = Blue,
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
            OutlinedTextField (
                value = password,
                onValueChange = { newText -> password = newText },
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
                trailingIcon = {
                    if (isPasswordVisible) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_visibility_off_24),
                            contentDescription = "Глазик",
                            modifier = Modifier.clickable {
                                passwordVisual = if (isPasswordVisible) {
                                    PasswordVisualTransformation()
                                } else {
                                    VisualTransformation.None
                                }
                                isPasswordVisible = ! isPasswordVisible
                            },
                            tint = Blue
                        )
                    }
                    else {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_visibility_24),
                            contentDescription = "Глазик",
                            modifier = Modifier.clickable {
                                passwordVisual = if (isPasswordVisible) {
                                    PasswordVisualTransformation()
                                } else {
                                    VisualTransformation.None
                                }
                                isPasswordVisible = ! isPasswordVisible
                            },
                            tint = Blue
                        )
                    }
                },
                singleLine = true,
                visualTransformation = passwordVisual
            )
//            Text(
//                modifier = Modifier.padding(end = 4.dp, bottom = 8.dp, top = 16.dp).fillMaxWidth(),
//                text = "Забыли пароль?",
//                color = Blue,
//                textAlign = TextAlign.End,
//                fontSize = 16.sp,
//                textDecoration = TextDecoration.Underline
//            )
        }
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    makeRequest.launch {
                        val response = api.login(LoginRequest(email = email, password = password))

                        response.fold(
                            onSuccess = {
                                toUserAcc(it)
                                userPrefs.saveUser(email, password)
                                api.updateToken(it.token)
                                onLogin(true)
                            },
                            onFailure = {
                                api.updateToken(null)
                                onLogin(false)
                                onToast(it.message)
                            }
                        )
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Blue
            ),
            contentPadding = PaddingValues(horizontal = 80.dp, vertical = 20.dp)
        ) {
            Text("Войти", fontSize = 20.sp)
        }

    }
}
