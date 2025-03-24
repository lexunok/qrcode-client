package com.lex.qr.components.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue

@Composable
fun LoginText(text: String){
    Text(
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        text = text,
        color = Blue,
        textAlign = TextAlign.Center,
        fontSize = 20.sp
    )
}