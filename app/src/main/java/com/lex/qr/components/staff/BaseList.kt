package com.lex.qr.components.staff

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.BaseItem

@Composable
fun BaseList(items: List<BaseItem>, onClick: (BaseItem) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .padding(top = 64.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.9f),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(items) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .clickable {onClick(item)}
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(
                        width = 4.dp,
                        color = Blue,
                        shape = RoundedCornerShape(8.dp)
                    ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    color = Blue,
                    text = item.name,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp)
                )
            }
        }
    }
}