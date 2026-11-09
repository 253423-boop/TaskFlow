package com.ivanna.proyects.taskflow.tasks.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.upchiapas.taskflow.R

@Composable
fun TaskStatusSelector(
    selectedStatus: TaskStatus,
    onStatusSelected: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val pillShape = RoundedCornerShape(7.dp)
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(id = R.string.field_status_required),
            style = TaskFlowTextStyles.statusLabel,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(0.8f)
        )

        StatusPillOption(
            label = stringResource(id = R.string.status_pending),
            isSelected = selectedStatus == TaskStatus.PENDIENTE,
            enabled = enabled,
            shape = pillShape,
            onClick = { onStatusSelected(TaskStatus.PENDIENTE) },
            modifier = Modifier.weight(1.12f)
        )

        StatusPillOption(
            label = stringResource(id = R.string.status_completed),
            isSelected = selectedStatus == TaskStatus.COMPLETADA,
            enabled = enabled,
            shape = pillShape,
            onClick = { onStatusSelected(TaskStatus.COMPLETADA) },
            modifier = Modifier.weight(1.55f)
        )
    }
}

@Composable
private fun StatusPillOption(
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember(label) { MutableInteractionSource() }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics { selected = isSelected }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 30.dp)
                .clip(shape)
                .background(
                    color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant,
                    shape = shape
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                style = if (isSelected) {
                    TaskFlowTextStyles.statusOptionSelected
                } else {
                    TaskFlowTextStyles.statusOptionUnselected
                },
                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
