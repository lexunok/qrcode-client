package com.lex.qr.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import com.lex.qr.ui.theme.LightGray
import com.lex.qr.viewmodels.SubjectHist

@Composable
fun SubjectHistChart(subjectsHist: List<SubjectHist>) {
    Text(
        text = "По предметам:",
        textAlign = TextAlign.Start,
        fontSize = 20.sp,
        color = Blue,
        modifier = Modifier.fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 4.dp),
    )

    subjectsHist.forEach { item ->
        val value = item.count.toFloat() / item.total.toFloat()
        Text(
            text = "${item.name}: ${item.count}/${item.total}",
            textAlign = TextAlign.Center,
            color = Blue,
            modifier = Modifier.fillMaxWidth().padding(4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f).height(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightGray.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = value.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Blue)
                )
            }

            Text(
                text = "${(value * 100).toInt()}%",
                color = Blue,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(40.dp),
            )
        }
    }
}