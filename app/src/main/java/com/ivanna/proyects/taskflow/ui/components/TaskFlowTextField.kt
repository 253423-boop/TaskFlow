package com.ivanna.proyects.taskflow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivanna.proyects.taskflow.ui.theme.LocalTaskFlowColors
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles

@Composable
fun TaskFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    errorTextStyle: TextStyle = TaskFlowTextStyles.fieldError,
    helperEndText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 5,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onFieldClick: (() -> Unit)? = null
) {
    val hasError = !errorMessage.isNullOrBlank()
    val shape = RoundedCornerShape(9.2.dp)
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (hasError) {
        LocalTaskFlowColors.current.errorInputBackground
    } else {
        colorScheme.surface
    }
    val borderColor = if (hasError) colorScheme.error else colorScheme.outline

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TaskFlowTextStyles.fieldLabel,
                color = if (hasError) colorScheme.error else colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (!helperEndText.isNullOrBlank()) {
                Text(
                    text = helperEndText,
                    style = TaskFlowTextStyles.taskFieldError,
                    color = if (hasError) colorScheme.error else colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (onFieldClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.3.dp)
                    .clip(shape)
                    .background(color = backgroundColor, shape = shape)
                    .border(BorderStroke(1.15.dp, borderColor), shape)
                    .clickable(enabled = enabled, onClick = onFieldClick)
                    .padding(horizontal = 13.8.dp, vertical = 12.65.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = value.ifEmpty { placeholder },
                    style = TaskFlowTextStyles.fieldValue,
                    color = if (value.isEmpty()) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                textStyle = TaskFlowTextStyles.fieldValue.copy(color = colorScheme.onSurface),
                cursorBrush = SolidColor(colorScheme.primary),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.3.dp)
                            .clip(shape)
                            .background(color = backgroundColor, shape = shape)
                            .border(BorderStroke(1.15.dp, borderColor), shape)
                            .padding(horizontal = 13.8.dp, vertical = 12.65.dp),
                        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TaskFlowTextStyles.fieldValue,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = if (singleLine) 1 else maxLines,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = errorTextStyle,
                color = colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.3.dp)
            )
        }
    }
}
