package com.ivanna.proyects.taskflow.core.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivanna.proyects.taskflow.TaskFlowApplication
import com.ivanna.proyects.taskflow.ui.theme.LocalTaskFlowColors
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTheme
import com.upchiapas.taskflow.R

@Composable
fun SessionCheckPage(
    viewModel: SessionViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as TaskFlowApplication)
            .authModule
            .factorySession
    ),
    onNavigateToLogin: (Boolean) -> Unit,
    onNavigateToTasksHome: () -> Unit
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        when (destination) {
            SessionDestination.LOGIN -> onNavigateToLogin(false)
            SessionDestination.LOGIN_EXPIRED -> onNavigateToLogin(true)
            SessionDestination.TASKS_HOME -> onNavigateToTasksHome()
            SessionDestination.CHECKING -> Unit
        }
    }

    SessionCheckContent()
}

@Composable
fun SessionCheckContent(
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val spinnerTrack = LocalTaskFlowColors.current.spinnerTrack

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_brand_logo),
                contentDescription = stringResource(id = R.string.cd_logo),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = TaskFlowTextStyles.screenTitle,
                color = colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(44.dp),
            color = colorScheme.primary,
            trackColor = spinnerTrack,
            strokeWidth = 4.5.dp,
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.loading_session),
            style = TaskFlowTextStyles.stateSubtitle,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun PreviewSessionCheck() {
    TaskFlowTheme {
        SessionCheckContent()
    }
}
