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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.lex.qr.R
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Red
import com.lex.qr.utils.Claims
import com.lex.qr.utils.Role
import com.lex.qr.utils.UiEvent
import com.lex.qr.viewmodels.MenuProfileViewModel

@Composable
fun MenuProfile(
    modifier: Modifier,
    user: Claims,
    showMenu:Boolean,
    changeMenu: (Boolean) -> Unit,
    onToast: (String) -> Unit,
    changeUser: (Claims?) -> Unit
                ) {
    val viewModel: MenuProfileViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> onToast(event.message)
                is UiEvent.ChangeUser -> changeUser(event.user)
                else -> {}
            }
        }
    }

    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(user.avatarUrl)
            .error(R.drawable.baseline_account_circle_24)
            .placeholder(R.drawable.baseline_account_circle_24)
            .build(),
        contentScale = ContentScale.Crop
    )

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { path ->
            val inputStream = context.contentResolver.openInputStream(path)
            val imageBytes = inputStream?.readBytes()
            inputStream?.close()
            imageBytes?.let { bytes ->
                viewModel.uploadAvatar(user, bytes)
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
                if (user.role == Role.STAFF) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                changeUser(user.copy(role = Role.ADMIN))
                            },
                        fontWeight = FontWeight.SemiBold,
                        text = "В админ панель",
                        fontSize = 16.sp,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Center,
                        color = Red
                    )
                } else if (user.role == Role.ADMIN) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                changeUser(user.copy(role = Role.STAFF))
                            },
                        fontWeight = FontWeight.SemiBold,
                        text = "Закрыть админку",
                        fontSize = 16.sp,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Center,
                        color = Red
                    )
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.logout() },
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

