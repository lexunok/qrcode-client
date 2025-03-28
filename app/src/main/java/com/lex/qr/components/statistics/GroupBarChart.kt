package com.lex.qr.components.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.GroupBar

@Composable
fun GroupBarChart(visits: List<GroupBar>) {
    val maxHeight = visits.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    Text(
        text = "Средняя посещаемость(%):",
        textAlign = TextAlign.Start,
        fontSize = 20.sp,
        color = Blue,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    )
    Row(Modifier.fillMaxWidth().height(240.dp).padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        visits.forEach { bar ->
            val height = (bar.count.toFloat() / maxHeight) * 0.8f
            Column(Modifier.width(48.dp).fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    text = bar.count.toString(),
                    color = Blue,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = height.coerceIn(0f, 0.8f))
                        .background(Blue)
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    text = bar.title,
                    color = Blue,
                )
            }
        }
    }

}