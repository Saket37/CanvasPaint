package dev.saketanand.canvaspaint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.saketanand.canvaspaint.component.CanvasControls
import dev.saketanand.canvaspaint.component.DrawingCanvas
import dev.saketanand.canvaspaint.ui.theme.CanvasPaintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasPaintTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = viewModel<CanvasViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DrawingCanvas(
                            paths = state.paths,
                            currentPath = state.currentPath,
                            onAction = viewModel::onAction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        CanvasControls(
                            selectedColor = state.selectedColor,
                            colors = allColors,
                            onClearCanvasClick = {
                                viewModel.onAction(DrawingActions.OnClearCanvasClick)
                            },
                            onColorClick = {
                                viewModel.onAction(DrawingActions.OnSelectNewColor(it))
                            }
                        )


                    }
                }
            }
        }
    }
}
