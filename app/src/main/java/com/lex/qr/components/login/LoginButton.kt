package com.lex.qr.components.login

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue

@Composable
fun LoginButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
){
    Button(
        onClick = { onClick() },
        modifier = modifier.padding(bottom = 80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Blue
        ),
        contentPadding = PaddingValues(horizontal = 80.dp, vertical = 20.dp)
    ) {
        Text(text, fontSize = 20.sp)
    }
}