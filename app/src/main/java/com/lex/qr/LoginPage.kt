package com.lex.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.lex.qr.components.Title
import com.lex.qr.ui.theme.Blue

@Composable
fun LoginPage(api: API, isLoading: Boolean, onLoading: (Boolean) -> (Unit), onLogin: (User) -> Unit) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        onLoading(true)
        val response = api.getUsers()
        response?.let {
            users = response
        }
        onLoading(false)
    }

    Box (modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 64.dp)
        .shadow(12.dp)
        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
        .padding(20.dp)
    ) {
        Title("Вход", Modifier.fillMaxWidth())

        if (isLoading) {
            CircularProgressIndicator(color = Blue, modifier = Modifier.size(100.dp).align(Alignment.Center), strokeWidth = 12.dp)
        }

        if (users.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(users) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.clickable { onLogin(item) }
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(width = 4.dp, color = Blue, shape = RoundedCornerShape(8.dp)
                            ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        ),
                    ) {
                        Text(
                            color = Blue,
                            text = "${item.firstName} ${item.lastName}",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}