package com.ivanna.proyects.taskflow.tasks.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivanna.proyects.taskflow.tasks.presentation.util.TaskDateFormatter
import com.upchiapas.taskflow.R
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePickerDialog(
    initialDate: LocalDate?,
    onDateConfirmed: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (initialDate ?: LocalDate.now()).let {
            TaskDateFormatter.toUtcMillis(it)
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val selectedMillis = datePickerState.selectedDateMillis
            TextButton(
                onClick = {
                    if (selectedMillis != null) {
                        onDateConfirmed(TaskDateFormatter.fromUtcMillis(selectedMillis))
                    }
                },
                enabled = selectedMillis != null
            ) {
                Text(text = stringResource(id = R.string.action_next))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = stringResource(id = R.string.dialog_select_date_title),
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTimePickerDialog(
    initialTime: LocalTime?,
    onTimeConfirmed: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val startTime = initialTime ?: LocalTime.of(23, 59)
    val timePickerState = rememberTimePickerState(
        initialHour = startTime.hour,
        initialMinute = startTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.dialog_select_time_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeConfirmed(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text(text = stringResource(id = R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        }
    )
}
