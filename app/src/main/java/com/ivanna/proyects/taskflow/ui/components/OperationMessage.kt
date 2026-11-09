package com.ivanna.proyects.taskflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ivanna.proyects.taskflow.ui.theme.LocalTaskFlowColors
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTheme
import com.upchiapas.taskflow.R

enum class MessageType {
    SUCCESS,
    ERROR
}

@Composable
fun OperationMessage(
    type: MessageType,
    text: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onDismiss: (() -> Unit)? = null
) {
    val taskFlowColors = LocalTaskFlowColors.current
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = when (type) {
        MessageType.SUCCESS -> taskFlowColors.successSurface
        MessageType.ERROR -> colorScheme.errorContainer
    }
    val contentColor = when (type) {
        MessageType.SUCCESS -> taskFlowColors.successText
        MessageType.ERROR -> colorScheme.onErrorContainer
    }
    val shape = RoundedCornerShape(if (compact) 10.dp else 12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (compact) 50.dp else 60.dp)
            .clip(shape)
            .background(backgroundColor, shape)
            .then(
                if (onDismiss != null) {
                    Modifier.clickable(onClick = onDismiss)
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 14.dp else 18.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (type == MessageType.SUCCESS) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = stringResource(id = R.string.cd_success_icon),
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = if (compact) {
                TaskFlowTextStyles.bannerCompactMessage
            } else {
                TaskFlowTextStyles.bannerMessage
            },
            color = contentColor
        )
    }
}

@Preview(name = "13 · Mensajes de operación", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview13OperationMessages() {
    TaskFlowTheme {
        val colorScheme = MaterialTheme.colorScheme
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .systemBarsPadding()
                .padding(top = 12.dp)
        ) {
            TaskFlowHeader(
                title = stringResource(id = R.string.section_my_tasks),
                isHomeTitle = true,
                showBack = false,
                showLogout = true,
                onLogout = {}
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(id = R.string.msg_operations_title),
                    style = TaskFlowTextStyles.errorStateTitle,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(34.dp))
                Text(
                    text = stringResource(id = R.string.msg_success_label),
                    style = TaskFlowTextStyles.headerAction,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                OperationMessage(
                    type = MessageType.SUCCESS,
                    text = stringResource(id = R.string.msg_task_saved)
                )
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = stringResource(id = R.string.msg_error_label),
                    style = TaskFlowTextStyles.headerAction,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                OperationMessage(
                    type = MessageType.ERROR,
                    text = stringResource(id = R.string.error_delete_task)
                )
                Spacer(modifier = Modifier.height(38.dp))
                Text(
                    text = stringResource(id = R.string.msg_separate_note),
                    style = TaskFlowTextStyles.authErrorCompact,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
