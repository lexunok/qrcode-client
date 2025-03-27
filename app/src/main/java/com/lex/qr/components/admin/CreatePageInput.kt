package com.lex.qr.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.LightGray

@Composable
fun CreatePageInput(
    value: String,
    onValueChange: (String) -> Unit
){
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = LightGray,
            focusedLabelColor = Blue,
            unfocusedLabelColor = Color.Transparent,
            cursorColor = Blue,
            focusedTextColor = Blue,
            unfocusedTextColor = Blue,
        ),
        singleLine = true
    )
}