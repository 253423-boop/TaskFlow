package com.ivanna.proyects.taskflow.tasks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.ivanna.proyects.taskflow.tasks.presentation.util.TaskDateFormatter
import com.ivanna.proyects.taskflow.ui.theme.LocalTaskFlowColors
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.upchiapas.taskflow.R

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val overdueColor = LocalTaskFlowColors.current.overdueCrimson
    val isCompleted = task.estado == TaskStatus.COMPLETADA
    val isOverdueAndPending = task.vencida && !isCompleted
    val shortDate = TaskDateFormatter.formatShortForCard(task.fechaEntrega)
    val statusText = stringResource(
        id = if (isCompleted) R.string.status_completed else R.string.status_pending
    )
    val priorityLabel = stringResource(
        id = when (task.prioridad) {
            TaskPriority.BAJA -> R.string.badge_priority_low
            TaskPriority.MEDIA -> R.string.badge_priority_medium
            TaskPriority.ALTA -> R.string.badge_priority_high
        }
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.8.dp),
        color = colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.95.dp, vertical = 14.95.dp)
        ) {
            Text(
                text = task.titulo,
                style = if (isCompleted) {
                    TaskFlowTextStyles.cardCompletedTitle
                } else {
                    TaskFlowTextStyles.cardTitle
                },
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = stringResource(id = R.string.home_card_meta_format, task.materia, statusText),
                style = TaskFlowTextStyles.cardMeta,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isCompleted) {
                Text(
                    text = stringResource(
                        id = R.string.home_card_completed_footer_format,
                        shortDate,
                        task.prioridad.level
                    ),
                    style = TaskFlowTextStyles.cardCompletedFooter,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOverdueAndPending) {
                            stringResource(id = R.string.home_card_overdue_format, shortDate)
                        } else {
                            stringResource(id = R.string.home_card_due_format, shortDate)
                        },
                        style = TaskFlowTextStyles.cardMeta,
                        color = if (isOverdueAndPending) overdueColor else colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    )
                    Text(
                        text = priorityLabel,
                        style = TaskFlowTextStyles.cardPriority,
                        color = colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
    }
}
