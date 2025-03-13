package com.lex.qr.pages

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lex.qr.components.MenuProfile
import com.lex.qr.components.Title
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.API
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.Role
import com.lex.qr.utils.User
import com.lex.qr.utils.UserPreferences

interface Page

enum class PageTransitionDirection {
    LEFT, RIGHT, UP, DOWN
}
@Composable
fun MainPage(
    api: API,
    geolocationClient: GeolocationClient,
    userPrefs: UserPreferences,
    user: User,
    lastLocation: String,
    onLogout: (Boolean) -> Unit,
) {

    LaunchedEffect(Unit) {
        geolocationClient.checkGps()
    }

    var isLoading by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("Главная") }

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

        if (isLoading) {
            CircularProgressIndicator(color = Blue, modifier = Modifier.size(100.dp).align(Alignment.Center), strokeWidth = 12.dp)
        }

        when (user.role) {
            Role.ADMIN -> {
                AdminPage(api) { value: String -> title = value }
            }
            Role.STAFF -> {
                StaffPage(api, user, geolocationClient, lastLocation) { value: String -> title = value }
            }
            Role.STUDENT -> {
                StudentPage(api, user, geolocationClient, lastLocation) { value: String -> title = value }
            }
        }
        MenuProfile(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp),
            user = user,
            api = api,
            showMenu = showMenu,
            userPrefs = userPrefs,
            changeMenu = {value: Boolean -> showMenu = value},
            onLogout = onLogout
        )
    }
}