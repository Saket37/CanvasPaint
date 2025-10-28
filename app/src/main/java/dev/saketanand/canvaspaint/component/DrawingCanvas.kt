package dev.saketanand.canvaspaint.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastForEach
import dev.saketanand.canvaspaint.DrawingActions
import dev.saketanand.canvaspaint.PathData
import kotlin.math.abs

@Composable
fun DrawingCanvas(
    paths: List<PathData>,
    currentPath: PathData?,
    onAction: (DrawingActions) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clipToBounds()
            .background(Color.White)
            .pointerInput(true) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Main)
                            .changes.first()
                        down.consume()
                        // A new path starts as soon as the finger is down
                        onAction(DrawingActions.OnNewPathStart)
                        onAction(DrawingActions.OnDraw(down.position))
                        // Loop to track drag events
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)

                            // Find the change associated with the original "down" event
                            val drag = event.changes.firstOrNull { it.id == down.id }
                            if (drag != null) {
                                // --- Pointer is moving ---
                                if (drag.pressed) {
                                    // This is a drag event
                                    onAction(DrawingActions.OnDraw(drag.position))
                                    drag.consume()
                                }
                                // --- Pointer went up ---
                                else {
                                    // This is the "up" event
                                    // Consume the "up" event
                                    drag.consume()
                                    break // Exit inner drag loop
                                }
                            }
                        }
                        // Finger is up, end the path
                        onAction(DrawingActions.OnNewPathEnd)
                    }
                }
//                detectDragGestures(
//                    onDragStart = {
//                        onAction(DrawingActions.OnNewPathStart)
//                    }, onDragEnd = {
//                        onAction(DrawingActions.OnNewPathEnd)
//                    }, onDragCancel = {
//                        onAction(DrawingActions.OnNewPathEnd)
//                    }, onDrag = { change, _ ->
//                        onAction(DrawingActions.OnDraw(change.position))
//                    })
//
//                detectTapGestures(
//                    onTap = {
//                        onAction(DrawingActions.OnNewPathStart)
//                        onAction(DrawingActions.OnDraw(it))
//                        onAction(DrawingActions.OnNewPathEnd)
//                    }
//                )
            }
    ) {
        paths.fastForEach { pathData ->
            drawPath(
                path = pathData.path,
                color = pathData.color,
            )
        }
        currentPath?.let {
            drawPath(
                path = it.path,
                color = it.color,
            )
        }
    }
}

private fun DrawScope.drawPath(
    path: List<Offset>, color: Color, thickness: Float = 10f
) {
    if (path.isEmpty()) return
    if (path.size == 1) {
        drawCircle(
            color = color,
            radius = thickness / 2f,
            center = path.first()
        )
        return
    }
    val smoothenedPath = Path().apply {
        if (path.isNotEmpty()) {
            moveTo(path.first().x, path.first().y)

            val smoothness = 5

            for (i in 1..path.lastIndex) {
                val from = path[i - 1]
                val to = path[i]
                val dx = abs(from.x - to.x)
                val dy = abs(from.y - to.y)
                if (dx >= smoothness || dy >= smoothness) {
                    quadraticTo(x1 = (from.x + to.x) / 2f, y1 = (from.y + to.y) / 2f, to.x, to.y)
                }
            }
        }
    }

    drawPath(
        smoothenedPath, color = color, style = Stroke(
            width = thickness, cap = StrokeCap.Round, join = StrokeJoin.Round
        )
    )
}