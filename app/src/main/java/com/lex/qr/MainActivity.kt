package com.lex.qr

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.QRTheme


class MainActivity : ComponentActivity() {

    private lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        api = API()

        val lightTransparentStyle = SystemBarStyle.light(
            scrim = TRANSPARENT,
            darkScrim = TRANSPARENT
        )
        enableEdgeToEdge(
            statusBarStyle = lightTransparentStyle,
            navigationBarStyle = lightTransparentStyle
        )

        setContent {
            QRTheme {
                Box (modifier = Modifier
                    .fillMaxSize()
                    .background(color = Blue)) {
                    var key by remember { mutableStateOf<String?>(null) }
                    var user by remember { mutableStateOf<User?>(null) }
                    var isLoading by remember { mutableStateOf(false) }

                    val changeUser = { value: User? -> user = value }
                    val changeKey = { value: String? -> key = value }
                    val onLoading = {value: Boolean -> isLoading = value}
                    if (user != null) {
                        MainPage(api, user!!, key, isLoading, changeUser, onLoading, changeKey)
                    }
                    else {
                        LoginPage (api, isLoading, onLoading, changeUser)
                    }
                }
            }
        }
    }
}

//ГЕОЛОКАЦИЮ ОТПРАВЛЯТЬ СО СТУДЕНТСКОГО АККА
//СДЕЛАТЬ АКТИВНОСТЬ QR CODE НА 20 МИНУТ
//ЧЕЛОВЕК МОЖЕТ ТОЛЬКО ОДИН РАЗ ОТСКАНИРОВАТЬ КОД СО СВОЕГО АККА И УСТРОЙСТВА (НУЖНО ОТПРАВЛЯТЬ УСТРОЙСТВО НА СЕРВЕР)

