package com.lex.qr.components.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue

@Composable
fun LoginClickableText(
    text: String,
    modifier: Modifier
){
    Text(
        modifier = modifier
            .padding(end = 4.dp, bottom = 8.dp, top = 16.dp)
            .fillMaxWidth(),
        text = text,
        color = Blue,
        textAlign = TextAlign.End,
        fontSize = 16.sp,
        textDecoration = TextDecoration.Underline
    )
}