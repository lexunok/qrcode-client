package com.lex.qr.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun SelectDates(
    value1: String,
    value2: String,
    onValueChange1: (String) -> Unit,
    onValueChange2: (String) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth(0.5f)) {
            Text(
                text = "Дата от",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Blue,
                modifier = Modifier.fillMaxWidth().padding(4.dp),
            )
            DateInput(value1) { newValue -> onValueChange1(newValue) }
        }
        Column(modifier = Modifier.fillMaxWidth(1f)) {
            Text(
                text = "Дата до",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Blue,
                modifier = Modifier.fillMaxWidth().padding(4.dp),
            )
            DateInput(value2) { newValue -> onValueChange2(newValue) }
        }
    }
}