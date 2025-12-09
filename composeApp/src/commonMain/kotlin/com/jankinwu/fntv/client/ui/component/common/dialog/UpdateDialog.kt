package com.jankinwu.fntv.client.ui.component.common.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jankinwu.fntv.client.manager.UpdateInfo
import com.jankinwu.fntv.client.manager.UpdateStatus
import com.jankinwu.fntv.client.ui.customAccentButtonColors
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.AccentButton
import io.github.composefluent.component.Button
import io.github.composefluent.component.DialogSize
import io.github.composefluent.component.FluentDialog
import io.github.composefluent.component.Text

@Composable
fun UpdateDialog(
    status: UpdateStatus,
    onDownload: (UpdateInfo) -> Unit,
    onInstall: (UpdateInfo) -> Unit,
    onDismiss: () -> Unit
) {
    if (status !is UpdateStatus.Idle) {
        FluentDialog(
            visible = true,
            size = DialogSize.Standard
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                when (status) {
                    is UpdateStatus.Checking -> {
                        Text("Checking for updates...", style = FluentTheme.typography.subtitle)
                    }

                    is UpdateStatus.Available -> {
                        Text("Update Available", style = FluentTheme.typography.subtitle)
                        Spacer(Modifier.height(12.dp))
                        Text("Version: ${status.info.version}")
                        Spacer(Modifier.height(8.dp))
                        Text(status.info.releaseNotes)
                        Spacer(Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.width(8.dp))
                            AccentButton(
                                buttonColors = customAccentButtonColors(),
                                onClick = { onDownload(status.info) }) {
                                Text("Download")
                            }
                        }
                    }

                    is UpdateStatus.Downloading -> {
                        Text("Downloading...", style = FluentTheme.typography.subtitle)
                        Spacer(Modifier.height(12.dp))
                        Text("Progress: ${(status.progress * 100).toInt()}%")
                        Spacer(Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = onDismiss) {
                                Text("Cancel")
                            }
                        }
                    }

                    is UpdateStatus.Downloaded -> {
                        Text("Update Downloaded", style = FluentTheme.typography.subtitle)
                        Spacer(Modifier.height(12.dp))
                        Text("File saved to: ${status.filePath}")
                        Spacer(Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.width(8.dp))
                            AccentButton(
                                buttonColors = customAccentButtonColors(),
                                onClick = { onInstall(status.info) }
                            ) {
                                Text("Install")
                            }
                        }
                    }

                    is UpdateStatus.Error -> {
                        Text("Error", style = FluentTheme.typography.subtitle)
                        Spacer(Modifier.height(12.dp))
                        Text(status.message)
                        Spacer(Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AccentButton(
                                buttonColors = customAccentButtonColors(),
                                onClick = onDismiss
                            ) {
                                Text("OK")
                            }
                        }
                    }

                    is UpdateStatus.UpToDate -> {
                        Text("Up to Date", style = FluentTheme.typography.subtitle)
                        Spacer(Modifier.height(12.dp))
                        Text("You are using the latest version.")
                        Spacer(Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AccentButton(
                                buttonColors = customAccentButtonColors(),
                                onClick = onDismiss
                            ) {
                                Text("OK")
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
