package com.lex.qr.components.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.LightGray

@Composable
fun LoginTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
){
    OutlinedTextField (
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
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
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation
    )
}