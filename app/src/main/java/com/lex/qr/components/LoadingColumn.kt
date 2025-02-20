package com.lex.qr.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingColumn(
    modifier: Modifier,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(5) { // 5 заглушек
            val transition = rememberInfiniteTransition()
            val colorAnimation by transition.animateColor(
                initialValue = Color.White,
                targetValue = Color.LightGray,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = colorAnimation),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Text(
                    color = Color.Transparent,
                    text = "",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}