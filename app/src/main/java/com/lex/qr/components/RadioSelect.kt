package com.lex.qr.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lex.qr.ui.theme.Blue

@Composable
fun RadioSelect(
    selectedBool: Boolean,
    title: String,
    onSelectClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onSelectClick() }
    ) {
        RadioButton(
            selected = selectedBool,
            onClick = { onSelectClick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = Blue, // Цвет выбранной радиокнопки
                unselectedColor = Color.Gray // Цвет невыбранной радиокнопки
            )
        )
        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp),
            color = if (selectedBool) Blue else Color.Black
        )
    }
}