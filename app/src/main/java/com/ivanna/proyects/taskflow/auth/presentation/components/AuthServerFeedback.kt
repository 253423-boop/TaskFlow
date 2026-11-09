package com.ivanna.proyects.taskflow.auth.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.ivanna.proyects.taskflow.ui.components.MessageType
import com.ivanna.proyects.taskflow.ui.components.OperationMessage
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.upchiapas.taskflow.R

@Composable
fun AuthSessionExpiredNotice(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    OperationMessage(
        type = MessageType.ERROR,
        text = stringResource(id = R.string.error_session_expired),
        compact = true,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@Composable
fun AuthServerErrorText(
    message: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TaskFlowTextStyles.authErrorCompact
) {
    Text(
        text = message,
        style = style,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.fillMaxWidth()
    )
}
