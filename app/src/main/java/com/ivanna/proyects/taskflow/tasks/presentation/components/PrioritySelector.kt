package com.ivanna.proyects.taskflow.tasks.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.upchiapas.taskflow.R

@Composable
fun PrioritySelector(
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val optionShape = RoundedCornerShape(9.2.dp)
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.field_priority_required),
            style = TaskFlowTextStyles.fieldLabel,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.75.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskPriority.entries.forEach { priority ->
                val isSelected = selectedPriority == priority
                val labelRes = when (priority) {
                    TaskPriority.BAJA -> R.string.priority_low_option
                    TaskPriority.MEDIA -> R.string.priority_medium_option
                    TaskPriority.ALTA -> R.string.priority_high_option
                }
                val interactionSource = remember(priority) { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .semantics { selected = isSelected }
                        .clickable(
                            enabled = enabled,
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.RadioButton,
                            onClick = { onPrioritySelected(priority) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 39.1.dp)
                            .clip(optionShape)
                            .background(
                                color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant,
                                shape = optionShape
                            )
                            .padding(horizontal = 11.5.dp, vertical = 9.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stringResource(id = labelRes),
                            style = TaskFlowTextStyles.priorityOption,
                            color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
