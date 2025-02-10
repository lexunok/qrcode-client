package com.lex.qr

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue

@Composable
fun Title(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Blue,
        textAlign = TextAlign.Center,
        modifier = modifier,
        fontSize = 32.sp
    )
}