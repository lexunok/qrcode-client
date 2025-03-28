package com.lex.qr.components.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.qr.ui.theme.Blue
import com.lex.qr.utils.LineChart
import java.time.format.DateTimeFormatter

@Composable
fun LineChart(visits: List<LineChart>) {
    val maxVisits = visits.maxOfOrNull { it.visitCount }?.toFloat() ?: 1f // Максимальное значение по Y
    val weeksCount = visits.size // Количество точек (недель)

    Text(
        text = "График:",
        textAlign = TextAlign.Start,
        fontSize = 20.sp,
        color = Blue,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 4.dp, end = 4.dp, bottom = 4.dp),
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .width(20.dp)
                .height(320.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Показываем значения от максимума до 0
            val stepCount = 5 // Количество подписей
            for (i in stepCount downTo 0) {
                val value = (maxVisits * i / stepCount).toInt()
                Text(
                    color = Blue,
                    text = value.toString(),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            // График
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(Color.White)
                    .border(2.dp, color = Blue)
                    .padding(4.dp)
            ) {
                val width = size.width
                val height = size.height
                val stepX = width / (weeksCount - 1) // Шаг по X
                val stepY = height / maxVisits // Шаг по Y
                val gridStepY = height / 5

                val path = Path()

                // Рисуем сетку
                // Горизонтальные линии
                for (i in 0..5) {
                    val y = height - (i * gridStepY)
                    drawLine(
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        color = Blue.copy(alpha = 0.3f),
                        strokeWidth = 1f
                    )
                }

                // Вертикальные линии
                for (i in 0 until weeksCount) {
                    val x = i * stepX
                    drawLine(
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        color = Blue.copy(alpha = 0.3f),
                        strokeWidth = 1f
                    )
                }

                visits.forEachIndexed { index, (visits, _) ->
                    val x = index * stepX
                    val y = height - (visits * stepY) // Инверсия Y (0 внизу)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Точки на графике
                    drawCircle(
                        color = Blue,
                        radius = 10f,
                        center = Offset(x, y)
                    )
                }

                // Отрисовка линий
                drawPath(
                    path = path,
                    color = Blue,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                visits.forEach { visit ->
                    Text(
                        color = Blue,
                        text = visit.weekDate.format(DateTimeFormatter.ofPattern("dd.MM")),
                        fontSize = 6.sp
                    )
                }
            }
        }
    }
}