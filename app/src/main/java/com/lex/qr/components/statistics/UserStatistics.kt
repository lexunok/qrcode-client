package com.lex.qr.components.statistics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.lex.qr.R
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.LightGray
import com.lex.qr.utils.Attendance
import com.lex.qr.utils.StudentStats

@Composable
fun UserStatistics(student: StudentStats, attendance: Attendance) {

    val commonValue = if (attendance.totalClasses == 0) 0f else attendance.activeClasses.toFloat() / attendance.totalClasses.toFloat()

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(student.avatarUrl)
            .error(R.drawable.baseline_account_circle_24)
            .placeholder(R.drawable.baseline_account_circle_24)
            .build(),
        contentScale = ContentScale.Crop
    )

    Image(
        painter = painter,
        contentDescription = "Аватарка",
        modifier = Modifier
            .padding(top = 12.dp)
            .size(56.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = Blue,
                shape = CircleShape
            ),
        contentScale = ContentScale.Crop
    )

    Text(
        text = "${student.firstName} ${student.lastName}",
        textAlign = TextAlign.Start,
        fontSize = 20.sp,
        color = Blue,
        modifier = Modifier.fillMaxWidth().padding(4.dp),
    )

    Text(
        text = "Посещаемость: ${attendance.activeClasses}/${attendance.totalClasses}",
        textAlign = TextAlign.Start,
        fontSize = 20.sp,
        color = Blue,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 4.dp, end = 4.dp, bottom = 4.dp),
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
                    .fillMaxWidth(fraction = commonValue.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Blue)
            )
        }

        Text(
            text = "${(commonValue * 100).toInt()}%",
            color = Blue,
            modifier = Modifier
                .padding(start = 8.dp)
                .width(40.dp),
        )
    }
}