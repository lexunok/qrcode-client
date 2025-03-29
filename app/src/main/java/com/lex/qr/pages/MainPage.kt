package com.lex.qr.pages

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.components.MenuProfile
import com.lex.qr.components.Title
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.Claims
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.Role
import com.lex.qr.viewmodels.MainViewModel

interface Page

@Composable
fun MainPage(
    geolocationClient: GeolocationClient
    ) {
    val viewModel: MainViewModel = viewModel()

    val changeTitle = { value: String -> viewModel.updateTitle(value) }
    val changeUser = { value: Claims? -> viewModel.updateUser(value) }
    val changeMenu = { value: Boolean -> viewModel.updateMenu(value) }
    val onToast = {value: String -> viewModel.showToast(value)}

    val title by viewModel.title.collectAsState()
    val user by viewModel.user.collectAsState()
    val showMenu by viewModel.showMenu.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
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
                geolocationClient.startLocationUpdates(context)
            } else {
                onToast("Ошибка в старте поиска геолокации")
            }
        }

    LaunchedEffect(Unit) {
        requestPermissionsLauncher.launch(
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        )
        geolocationClient.checkGps(context)
        viewModel.login()
    }

    user?.let {
        Box (modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 56.dp)
            .shadow(12.dp)
            .background(color = Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues = PaddingValues(end = 56.dp))
                .align(Alignment.TopCenter)
            ) {
                Title(title, Modifier.fillMaxWidth().align(Alignment.Center).padding(vertical = 12.dp))
            }

            when (it.role) {
                Role.ADMIN -> {
                    AdminPage(onToast, changeTitle)
                }
                Role.STAFF -> {
                    StaffPage(geolocationClient, onToast, changeTitle)
                }
                Role.STUDENT -> {
                    StudentPage(geolocationClient, it, onToast, changeTitle)
                }
            }
            MenuProfile(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp),
                user = it,
                showMenu = showMenu,
                changeMenu = changeMenu,
                onToast = onToast,
                changeUser = changeUser,
            )
        }
    }
    if (!isLoading && user == null) {
        LoginPage(changeUser, onToast)
    }
}