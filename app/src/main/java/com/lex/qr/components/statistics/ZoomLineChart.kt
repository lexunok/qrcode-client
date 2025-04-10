package com.lex.qr.components.statistics

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.lex.qr.utils.LineChart
import com.lex.qr.viewmodels.GraphZoomLevel

@Composable
fun ZoomLineChart(
    zoomLevel: GraphZoomLevel,
    list: List<LineChart>,
    changeZoom: (GraphZoomLevel) -> Unit,
){
    var currentScale by remember { mutableFloatStateOf(1f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(zoomLevel) {
                detectTransformGestures { _, _, zoom, _ ->
                    currentScale *= zoom

                    if (currentScale > 1.1f) {
                        when (zoomLevel) {
                            GraphZoomLevel.MONTHS -> changeZoom(GraphZoomLevel.WEEKS)
                            GraphZoomLevel.WEEKS -> changeZoom(GraphZoomLevel.DAYS)
                            GraphZoomLevel.DAYS -> {}
                        }
                        currentScale = 1f
                    } else if (currentScale < 0.9f) {
                        when (zoomLevel) {
                            GraphZoomLevel.DAYS -> changeZoom(GraphZoomLevel.WEEKS)
                            GraphZoomLevel.WEEKS -> changeZoom(GraphZoomLevel.MONTHS)
                            GraphZoomLevel.MONTHS -> {}
                        }
                        currentScale = 1f
                    }
                }
            }
    ){
        LineChart(list, zoomLevel)
    }
}