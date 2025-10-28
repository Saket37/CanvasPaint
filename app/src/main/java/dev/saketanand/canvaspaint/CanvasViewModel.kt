package dev.saketanand.canvaspaint

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DrawingState(
    val selectedColor: Color = Color.Black,
    val currentPath: PathData? = null,
    val paths: List<PathData> = emptyList()
)

data class PathData(
    val id: String, val color: Color, val path: List<Offset>
)

val allColors = listOf(
    Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan
)

sealed interface DrawingActions {
    data object OnNewPathStart : DrawingActions
    data class OnDraw(val offset: Offset) : DrawingActions
    data object OnNewPathEnd : DrawingActions
    data class OnSelectNewColor(val color: Color) : DrawingActions
    data object OnClearCanvasClick : DrawingActions
}

class CanvasViewModel : ViewModel() {

    private val _state = MutableStateFlow(DrawingState())
    val state = _state.asStateFlow()

    fun onAction(action: DrawingActions) {
        when (action) {
            DrawingActions.OnClearCanvasClick -> onClearCanvasClick()
            is DrawingActions.OnDraw -> onDraw(action.offset)
            DrawingActions.OnNewPathEnd -> onNewPathStart()
            DrawingActions.OnNewPathStart -> onPathEnd()
            is DrawingActions.OnSelectNewColor -> onSelectColor(action.color)
        }
    }

    private fun onSelectColor(color: Color) {
        _state.update {
            it.copy(
                selectedColor = color
            )
        }
    }

    private fun onPathEnd() {
        val currentPathData = state.value.currentPath ?: return
        _state.update {
            it.copy(
                currentPath = null, paths = it.paths + currentPathData
            )
        }
    }

    private fun onNewPathStart() {
        _state.update {
            it.copy(
                currentPath = PathData(
                    id = System.currentTimeMillis().toString(),
                    color = it.selectedColor,
                    path = emptyList()
                )
            )
        }
    }

    private fun onDraw(offset: Offset) {
        val currentPathData = state.value.currentPath ?: return
        _state.update {
            it.copy(
                currentPath = currentPathData.copy(
                    path = currentPathData.path + offset
                )
            )
        }
    }

    private fun onClearCanvasClick() {
        _state.update {
            it.copy(
                currentPath = null, paths = emptyList()
            )
        }
    }
}