package dev.saketanand.canvaspaint

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Picture
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.saketanand.canvaspaint.component.CanvasControls
import dev.saketanand.canvaspaint.component.DrawingCanvas
import dev.saketanand.canvaspaint.component.PermissionDialog
import dev.saketanand.canvaspaint.ui.theme.CanvasPaintTheme
import dev.saketanand.canvaspaint.utility.createBitmapFromPicture
import dev.saketanand.canvaspaint.utility.saveBitmapToStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasPaintTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = viewModel<CanvasViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    val showDialog by viewModel.showDialog.collectAsStateWithLifecycle()
                    val launchAppSettings by viewModel.launchAppSettings.collectAsStateWithLifecycle()

                    val picture = remember { Picture() }
                    val coroutineScope = rememberCoroutineScope()

                    val storagePermissionResultLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { result ->
                        permissions.forEach { permission ->
                            if (result[permission] == false) {
                                if (!shouldShowRequestPermissionRationale(permission)) {
                                    viewModel.updateLaunchAppSettings(true)
                                }
                                viewModel.updateShowDialog(true)
                            } else {
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (picture.width > 0 && picture.height > 0) {
                                        val bitmap = createBitmapFromPicture(picture)
                                        val success = saveBitmapToStorage(
                                            this@MainActivity,
                                            bitmap,
                                            "myDrawing${System.currentTimeMillis()}"
                                        )
                                        withContext(Dispatchers.Main) {
                                            if (success) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Drawing saved!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Failed to save.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Canvas is empty.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }

                            }
                        }
                    }

                    if (showDialog) {
                        PermissionDialog(onDismiss = {
                            viewModel.updateShowDialog(false)
                        }, onOkClick = {
                            viewModel.updateShowDialog(false)
                            if (launchAppSettings) {
                                viewModel.updateLaunchAppSettings(false)
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                ).also {
                                    startActivity(it)
                                }
                            } else {
                                storagePermissionResultLauncher.launch(permissions)
                            }
                        })
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray)
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DrawingCanvas(
                            paths = state.paths,
                            currentPath = state.currentPath,
                            onAction = viewModel::onAction,
                            pictureToRecord = picture,
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
                            },
                            onSavePictureClick = {
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                                    permissions.forEach { permission ->
                                        val isGranted =
                                            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                                        if (!isGranted) {
                                            if (shouldShowRequestPermissionRationale(permission)) {
                                                viewModel.updateShowDialog(true)
                                            } else {
                                                storagePermissionResultLauncher.launch(permissions)
                                            }
                                        } else {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                if (picture.width > 0 && picture.height > 0) {
                                                    val bitmap = createBitmapFromPicture(picture)
                                                    val success = saveBitmapToStorage(
                                                        this@MainActivity,
                                                        bitmap,
                                                        "myDrawing${System.currentTimeMillis()}"
                                                    )
                                                    withContext(Dispatchers.Main) {
                                                        if (success) {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                "Drawing saved!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                "Failed to save.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                } else {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "Canvas is empty.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }

                                        }
                                    }
                                } else {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        if (picture.width > 0 && picture.height > 0) {
                                            val bitmap = createBitmapFromPicture(picture)
                                            val success = saveBitmapToStorage(
                                                this@MainActivity,
                                                bitmap,
                                                "myDrawing${System.currentTimeMillis()}"
                                            )
                                            withContext(Dispatchers.Main) {
                                                if (success) {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Drawing saved!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Failed to save.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Canvas is empty.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            })


                    }
                }
            }
        }
    }
}
