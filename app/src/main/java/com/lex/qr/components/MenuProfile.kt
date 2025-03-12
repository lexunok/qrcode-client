package com.lex.qr.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.R
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Red
import com.lex.qr.utils.User
import com.lex.qr.utils.UserPreferences

@Composable
fun MenuProfile(modifier: Modifier,
                 user: User,
                 showMenu:Boolean,
                 userPrefs: UserPreferences,
                 changeMenu: (Boolean) -> Unit,
                 onLogout: (Boolean) -> Unit
) {
    Box(modifier) {
        IconButton(
            modifier = Modifier.size(56.dp),
            onClick = { changeMenu(!showMenu) }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_account_circle_24),
                contentDescription = "Кнопка профиля",
                modifier = Modifier.fillMaxSize(),
                tint = Blue
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { changeMenu(false) },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "${user.firstName} ${user.lastName}",
                    fontSize = 16.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                    color = Blue
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TODO()
                        },
                    fontWeight = FontWeight.SemiBold,
                    text = "Загрузить аватарку",
                    fontSize = 16.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                    color = Blue
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            userPrefs.clearUser()
                            onLogout(false)
                        },
                    fontWeight = FontWeight.SemiBold,
                    text = "Выход",
                    fontSize = 16.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                    color = Red
                )
            }
        }
    }
}

