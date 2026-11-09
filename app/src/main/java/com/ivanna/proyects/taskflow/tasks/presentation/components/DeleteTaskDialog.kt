package com.ivanna.proyects.taskflow.tasks.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.ivanna.proyects.taskflow.ui.theme.LocalTaskFlowColors
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.upchiapas.taskflow.R

@Composable
fun DeleteTaskDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    Dialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            window?.setDimAmount(0f)
        }

        DeleteTaskDialogOverlay(
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            isLoading = isLoading
        )
    }
}

@Composable
fun DeleteTaskDialogOverlay(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val scrimInteractionSource = remember { MutableInteractionSource() }
    val cardInteractionSource = remember { MutableInteractionSource() }
    val actionShape = RoundedCornerShape(9.2.dp)
    val colorScheme = MaterialTheme.colorScheme
    val dialogScrim = LocalTaskFlowColors.current.dialogScrim

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dialogScrim.copy(alpha = 0.23f))
            .clickable(
                enabled = !isLoading,
                interactionSource = scrimInteractionSource,
                indication = null,
                onClick = onDismiss
            )
            .padding(horizontal = 19.55.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = cardInteractionSource,
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(18.dp),
            color = colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.7.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.dialog_delete_title),
                    style = TaskFlowTextStyles.dialogTitle,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = stringResource(id = R.string.dialog_delete_message),
                    style = TaskFlowTextStyles.dialogBody,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(34.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.95.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(149.5f)
                            .defaultMinSize(minHeight = 48.3.dp)
                            .clip(actionShape)
                            .background(colorScheme.surface, actionShape)
                            .clickable(
                                enabled = !isLoading,
                                role = Role.Button,
                                onClick = onDismiss
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.action_cancel),
                            style = TaskFlowTextStyles.dialogAction,
                            color = colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(167.9f)
                            .defaultMinSize(minHeight = 48.3.dp)
                            .clip(actionShape)
                            .background(colorScheme.primary, actionShape)
                            .clickable(
                                enabled = !isLoading,
                                role = Role.Button,
                                onClick = onConfirm
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                id = if (isLoading) {
                                    R.string.action_deleting
                                } else {
                                    R.string.action_confirm_delete
                                }
                            ),
                            style = TaskFlowTextStyles.dialogAction,
                            color = colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
