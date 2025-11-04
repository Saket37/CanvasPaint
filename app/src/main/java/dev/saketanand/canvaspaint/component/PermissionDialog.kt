package dev.saketanand.canvaspaint.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PermissionDialog(
    //permission: String,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    // onGotoAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onOkClick) {
                Text(text = "Ok")
            }
        },
        title = {
            Text(
                text = "Storage Permission Required",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                "This app needs to access your storage to save the drawing.",
                fontWeight = FontWeight.Medium
            )
        },
        modifier = modifier.fillMaxWidth()
    )
}