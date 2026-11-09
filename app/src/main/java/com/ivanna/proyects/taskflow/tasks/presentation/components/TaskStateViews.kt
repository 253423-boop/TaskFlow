package com.ivanna.proyects.taskflow.tasks.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivanna.proyects.taskflow.ui.components.TaskFlowPrimaryButton
import com.ivanna.proyects.taskflow.ui.theme.LocalTaskFlowColors
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.upchiapas.taskflow.R

@Composable
fun LoadingContent(
    message: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val spinnerTrack = LocalTaskFlowColors.current.spinnerTrack

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 27.6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = colorScheme.primary,
            trackColor = spinnerTrack,
            strokeWidth = 5.dp,
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = TaskFlowTextStyles.loadingStateText,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyContent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 27.6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_empty_tasks),
            contentDescription = stringResource(id = R.string.cd_empty_tasks),
            modifier = Modifier.size(70.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = title,
            style = TaskFlowTextStyles.emptyStateTitle,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = subtitle,
            style = TaskFlowTextStyles.stateSubtitle,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(42.dp))
            TaskFlowPrimaryButton(
                text = actionLabel,
                onClick = onActionClick
            )
        }
    }
}

@Composable
fun ErrorContent(
    title: String,
    subtitle: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 27.6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_error_circle),
            contentDescription = stringResource(id = R.string.cd_error_icon),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = title,
            style = TaskFlowTextStyles.errorStateTitle,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = subtitle,
            style = TaskFlowTextStyles.stateSubtitle,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        TaskFlowPrimaryButton(
            text = retryLabel,
            onClick = onRetry
        )
    }
}
