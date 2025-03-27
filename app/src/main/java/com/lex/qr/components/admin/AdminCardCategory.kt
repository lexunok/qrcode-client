package com.lex.qr.components.admin

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue

@Composable
fun AdminCardCategory(
    text: String,
    onItemClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(width = 4.dp, color = Blue, shape = RoundedCornerShape(8.dp))
            .clickable{ onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Text(
            color = Blue,
            text = text,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}