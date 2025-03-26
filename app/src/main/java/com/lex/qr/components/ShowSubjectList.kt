package com.lex.qr.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.utils.Subject

@Composable
fun ShowSubjectList(
    showDialog: Boolean,
    onLoading: Boolean,
    subjects: List<Subject>,
    selectedSubject: Subject?,
    onClick: (Subject?) -> Unit,
    closeDialog: () -> Unit,
) {
    if (showDialog) {
        Dialog(onDismissRequest = { closeDialog() }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .border(
                        width = 4.dp,
                        color = Blue,
                        shape = RoundedCornerShape(12.dp)
                    ),
                color = Color.White
            ) {
                if(onLoading){
                    LoadingColumn(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .heightIn(max = 600.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(
                                        width = 4.dp,
                                        color = Blue,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onClick(null)
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            ) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    color = Blue,
                                    text = "Отменить выбор",
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 12.dp)
                                )
                            }
                        }
                        items(subjects) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(
                                        width = 4.dp,
                                        color = if (selectedSubject?.id == item.id) Green else Blue,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onClick(item)
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            ) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    color = if (selectedSubject?.id == item.id) Green else Blue,
                                    text = item.name,
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}