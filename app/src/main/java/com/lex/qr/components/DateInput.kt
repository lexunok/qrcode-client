package com.lex.qr.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.LightGray
import java.time.LocalDate
import java.time.Year

@Composable
fun DateInput(
    value: String,
    onValueChange: (String) -> Unit
){
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            var input = newValue.text.filter { it.isDigit() }.take(8)
            if (input.length == 2 && input.toInt() > 31)
                input = "31"
            else if (input.length in 3..4) {
                var day = input.take(2)
                input = input.drop(2)
                if (input.length == 2){
                    if (input.toInt() > 12)
                        input = "12"
                    val date = LocalDate.of(LocalDate.now().year, input.toInt(), 1)
                    if (day.toInt() > date.lengthOfMonth())
                        day = if (input.toInt() == 2) "29" else date.lengthOfMonth().toString()
                }
                input = "$day.$input"
            } else if (input.length in 5..8){
                var day = input.take(2)
                input = input.drop(2)
                val month = input.take(2)
                input = input.drop(2)
                if (input.length == 4){
                    if (input.toInt() > LocalDate.now().year)
                        input = LocalDate.now().year.toString()
                    if (!Year.of(input.toInt()).isLeap && month.toInt() == 2 && day.toInt() > 28)
                        day = "28"
                }
                input = "$day.$month.$input"
            }

            textFieldValue = TextFieldValue(
                text = input,
                selection = TextRange(input.length)
            )

            onValueChange(input)
        },
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("00.00.0000") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = LightGray,
            focusedLabelColor = Blue,
            unfocusedLabelColor = Color.Transparent,
            cursorColor = Blue,
            focusedTextColor = Blue,
            unfocusedTextColor = Blue,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}