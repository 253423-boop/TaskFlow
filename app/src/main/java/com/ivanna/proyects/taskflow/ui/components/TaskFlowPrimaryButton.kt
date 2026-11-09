package com.ivanna.proyects.taskflow.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles

@Composable
fun TaskFlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String = text,
    @DrawableRes leadingIconRes: Int? = null
) {
    val shape = RoundedCornerShape(if (loading) 10.dp else 11.5.dp)
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (loading) 52.dp else 48.3.dp)
            .clip(shape)
            .background(colorScheme.primary, shape)
            .then(
                if (loading) {
                    Modifier.background(colorScheme.surfaceVariant.copy(alpha = 0.65f), shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                horizontal = 20.7.dp,
                vertical = 12.dp
            ),
        contentAlignment = if (loading) Alignment.Center else Alignment.CenterStart
    ) {
        if (loading) {
            Text(
                text = loadingText,
                style = TaskFlowTextStyles.loadingButton,
                color = colorScheme.primary
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIconRes != null) {
                    Icon(
                        painter = painterResource(id = leadingIconRes),
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = TaskFlowTextStyles.primaryButton,
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun TaskFlowSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(11.5.dp)
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.3.dp)
            .clip(shape)
            .background(colorScheme.surfaceVariant, shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 20.7.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = TaskFlowTextStyles.primaryButton,
            color = colorScheme.primary
        )
    }
}

@Composable
fun TaskFlowDestructiveLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TaskFlowTextStyles.destructiveLink,
            color = MaterialTheme.colorScheme.error
        )
    }
}
