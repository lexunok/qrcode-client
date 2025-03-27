package com.lex.qr.components.admin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue

@Composable
fun CreatePageText(text: String) {
    Text(
        text = text,
        color = Blue,
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    )
}