package com.lex.qr.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.lex.qr.R
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Red
import com.lex.qr.utils.API
import com.lex.qr.utils.User
import com.lex.qr.utils.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun MenuProfile(modifier: Modifier,
                api: API,
                user: User,
                showMenu:Boolean,
                userPrefs: UserPreferences,
                changeMenu: (Boolean) -> Unit,
                onLogout: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val makeRequest = rememberCoroutineScope()
    var avatarUrl by remember { mutableStateOf("https://qrcode-wva2.shuttle.app/api/profile/avatar/${user.id}") }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(avatarUrl)
            .error(R.drawable.baseline_account_circle_24)
            .placeholder(R.drawable.baseline_account_circle_24)
            .build(),
        contentScale = ContentScale.Crop
    )

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val imageBytes = inputStream?.readBytes()
            inputStream?.close()
            imageBytes?.let { bytes ->
                makeRequest.launch {
                    if (api.uploadAvatar(user.id, bytes)) {
                        //Убрать костыль, нужно с сервера url присылать при upload
                        avatarUrl += "?t=${System.currentTimeMillis()}"
                    }
                }
            }
        }
    }

    Box(modifier) {
        IconButton(
            modifier = Modifier.size(56.dp),
            onClick = { changeMenu(!showMenu) }
        ) {
            Image(
                painter = painter,
                contentDescription = "Аватарка",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(width = 2.dp, color = Blue, shape = CircleShape),
                contentScale = ContentScale.Crop
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
                           launcher.launch("image/*")
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

