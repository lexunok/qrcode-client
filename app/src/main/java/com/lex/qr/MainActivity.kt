package com.lex.qr

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lex.qr.pages.LoginPage
import com.lex.qr.pages.MainPage
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.QRTheme
import com.lex.qr.utils.API
import com.lex.qr.utils.Claims
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.LoginRequest
import com.lex.qr.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var api: API
    private lateinit var geolocationClient: GeolocationClient
    @Inject lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                    var isLoading by remember { mutableStateOf(true) }
                    var isLoggedIn by remember { mutableStateOf(userPrefs.isLoggedIn()) }
                    var lastLocation by remember { mutableStateOf("") }
                    var user by remember { mutableStateOf<Claims?>(null) }

                    val context = LocalContext.current
                    var toastMessage by remember { mutableStateOf<String?>(null) }

                    val onToast = {value: String? -> toastMessage = value}
                    val onUserAcc = {value: Claims? -> user = value}

                    LaunchedEffect(toastMessage) {
                        toastMessage?.let {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            toastMessage = null
                        }
                    }

                    if (isLoading) {
                        Box (modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp, vertical = 64.dp)
                            .shadow(12.dp)
                            .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                            .padding(20.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Blue,
                                modifier = Modifier.size(100.dp).align(Alignment.Center),
                                strokeWidth = 12.dp
                            )
                        }
                    }

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

                        if (isLoggedIn) {
                            val (savedEmail, savedPassword) = userPrefs.getUser()

                            val loginRequest = LoginRequest(
                                email = savedEmail ?: "",
                                password = savedPassword ?: ""
                            )

                            val response = api.login(loginRequest)

                            response.fold(
                                onSuccess = {
                                    user = it
                                    api.updateToken(it.token)
                                    isLoggedIn = true
                                },
                                onFailure = {
                                    isLoggedIn = false
                                    toastMessage = it.message
                                }
                            )
                        }
                        isLoading = false
                    }

                    if (user!=null) {
                        MainPage(api, geolocationClient, userPrefs, user!!, lastLocation, onUserAcc, onToast)
                    }
                    else if (!isLoading) {
                        LoginPage(onToast, onUserAcc)
                    }
                }
            }
        }
    }
}

