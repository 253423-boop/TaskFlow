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
import com.ivanna.proyects.taskflow.tasks.presentation.screens.TaskFilter
import com.ivanna.proyects.taskflow.ui.theme.LocalTaskFlowColors
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles

@Composable
fun TaskFilterRow(
    selectedFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(20.7.dp)
    val colorScheme = MaterialTheme.colorScheme
    val filterInactiveText = LocalTaskFlowColors.current.filterInactiveText

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            val interactionSource = remember(filter) { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics { selected = isSelected }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Tab,
                        onClick = { onFilterSelected(filter) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.25.dp)
                        .clip(pillShape)
                        .background(
                            color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant,
                            shape = pillShape
                        )
                        .padding(start = 10.35.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(id = filter.labelRes),
                        style = TaskFlowTextStyles.filterOption,
                        color = if (isSelected) colorScheme.onPrimary else filterInactiveText,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
