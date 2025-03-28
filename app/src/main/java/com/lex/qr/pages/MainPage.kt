package com.lex.qr.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import com.lex.qr.utils.API
import com.lex.qr.utils.Claims
import com.lex.qr.utils.GeolocationClient
import com.lex.qr.utils.Role
import com.lex.qr.utils.UserPreferences

interface Page

@Composable
fun MainPage(
    api: API,
    userPrefs: UserPreferences,
    user: Claims,
    lastLocation: String,
    onUserAcc: (Claims?) -> Unit,
    onToast: (String?) -> Unit,
    ) {

    var showMenu by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf(user.role) }
    var title by remember { mutableStateOf("Главная") }
    val changeTitle = { value: String -> title = value }

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

        when (role) {
            Role.ADMIN -> {
                AdminPage(onToast, changeTitle)
            }
            Role.STAFF -> {
                StaffPage(lastLocation, onToast, changeTitle)
            }
            Role.STUDENT -> {
                StudentPage(user, lastLocation, onToast, changeTitle)
            }
        }
        MenuProfile(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp),
            user = user,
            role = role,
            api = api,
            showMenu = showMenu,
            userPrefs = userPrefs,
            changeMenu = {value: Boolean -> showMenu = value},
            onUserAcc = onUserAcc,
            onToast = onToast,
            changeRole = {value: Role -> role = value},
            )
    }
}