package com.lex.qr.pages.admin

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lex.qr.components.LoadingColumn
import com.lex.qr.pages.getPageTransitionSpec
import com.lex.qr.ui.theme.Blue
import com.lex.qr.viewmodels.admin.ArchiveViewModel
import com.lex.qr.viewmodels.admin.CurrentArchivePage

@Composable
fun Archive(){
    val viewModel: ArchiveViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.page,
        transitionSpec = {
            getPageTransitionSpec(initialState, targetState)
        },
        modifier = Modifier.fillMaxSize()
    ){ currentPage ->
        Box(modifier = Modifier.fillMaxSize()){
            when(currentPage){
                CurrentArchivePage.TermList -> {
                    if (uiState.isLoading) {
                        LoadingColumn(
                            Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.75f),
                            contentPadding = PaddingValues(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.75f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(
                                            width = 4.dp,
                                            color = Blue,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {  },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                ) {
                                    Text(
                                        textAlign = TextAlign.Center,
                                        color = Blue,
                                        text = "Семестр 1",
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 12.dp)
                                    )
                                }
                            }
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(
                                            width = 4.dp,
                                            color = Blue,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {  },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                ) {
                                    Text(
                                        textAlign = TextAlign.Center,
                                        color = Blue,
                                        text = "Семестр 2",
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.7f).padding(bottom = 80.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Blue
                        )
                    ) {
                        Text("Архивировать активный семестр", textAlign = TextAlign.Center, fontSize = 18.sp)
                    }
                }
                CurrentArchivePage.Term -> {

                }
            }
        }
    }
}