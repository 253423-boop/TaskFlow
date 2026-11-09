package com.ivanna.proyects.taskflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.upchiapas.taskflow.R

@Composable
fun TaskFlowHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    isHomeTitle: Boolean = false,
    showBack: Boolean = false,
    backEnabled: Boolean = true,
    onBack: () -> Unit = {},
    showLogout: Boolean = false,
    onLogout: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
        ) {
            if (showBack) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 13.3.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = backEnabled,
                            role = Role.Button,
                            onClick = onBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_chevron),
                        contentDescription = stringResource(id = R.string.cd_back),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = if (showBack) 54.dp else 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_brand_logo),
                    contentDescription = stringResource(id = R.string.cd_logo),
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(if (showBack) 6.dp else 8.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = TaskFlowTextStyles.brandWordmark,
                    color = colorScheme.primary
                )
            }

            if (showLogout) {
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 19.6.dp)
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = onLogout
                        )
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.action_logout),
                        style = TaskFlowTextStyles.headerAction,
                        color = colorScheme.primary
                    )
                }
            }
        }

        if (!title.isNullOrEmpty()) {
            Text(
                text = title,
                style = if (isHomeTitle) {
                    TaskFlowTextStyles.homeTitle
                } else {
                    TaskFlowTextStyles.screenTitle
                },
                color = colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 27.6.dp,
                        end = 27.6.dp,
                        top = if (isHomeTitle) 21.dp else 19.dp
                    )
            )
        }
    }
}
