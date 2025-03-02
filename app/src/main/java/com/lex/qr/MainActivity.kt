package com.lex.qr

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lex.qr.pages.LoginPage
import com.lex.qr.pages.MainPage
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.QRTheme
import com.lex.qr.utils.API
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.User


class MainActivity : ComponentActivity() {
    private lateinit var api: API
    private lateinit var geolocationClient: GeolocationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        api = API()
        geolocationClient = GeolocationClient(this)

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
                    var lastLocation by remember { mutableStateOf("") }

                    val requestPermissionsLauncher =
                        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                            if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                                geolocationClient.startLocationUpdates{value: String -> lastLocation = value}
                            }
                        }

                    LaunchedEffect(Unit) {
                        requestPermissionsLauncher.launch(
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                        geolocationClient.checkGps()
                    }

                    if (user != null) {
                        MainPage(api, geolocationClient, user!!, key, lastLocation, isLoading, changeUser, onLoading, changeKey)
                    }
                    else {
                        LoginPage(api, changeUser)
                    }
                }
            }
        }
    }
}

//ЧЕЛОВЕК МОЖЕТ ТОЛЬКО ОДИН РАЗ ОТСКАНИРОВАТЬ КОД СО СВОЕГО УСТРОЙСТВА (НУЖНО ОТПРАВЛЯТЬ УСТРОЙСТВО НА СЕРВЕР)

