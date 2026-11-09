package com.ivanna.proyects.taskflow.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthTextField
import com.ivanna.proyects.taskflow.auth.presentation.components.TaskFlowHeader
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.LoginViewModel
import com.upchiapas.taskflow.R

@Composable
fun LoginPage(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
        }
    }

    val goToRegisterText = stringResource(id = R.string.action_go_to_register)
    val registerHighlight = stringResource(id = R.string.register_title)
    val registerPromptAnnotated = buildAnnotatedString {
        val highlightIndex = goToRegisterText.indexOf(registerHighlight)
        if (highlightIndex >= 0) {
            append(goToRegisterText.substring(0, highlightIndex))
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(goToRegisterText.substring(highlightIndex, highlightIndex + registerHighlight.length))
            }
            append(goToRegisterText.substring(highlightIndex + registerHighlight.length))
        } else {
            append(goToRegisterText)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        val viewportMinHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportMinHeight)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TaskFlowHeader(
                title = stringResource(id = R.string.login_title)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(id = R.string.label_email),
                placeholder = stringResource(id = R.string.placeholder_email),
                errorMessageRes = uiState.emailErrorRes,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = stringResource(id = R.string.label_password),
                placeholder = stringResource(id = R.string.placeholder_password),
                isPassword = true,
                errorMessageRes = uiState.passwordErrorRes,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onLoginSubmit()
                    }
                ),
                enabled = !uiState.isLoading
            )

            if (uiState.serverError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.serverError.orEmpty().ifBlank {
                        stringResource(id = R.string.error_network_generic)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onLoginSubmit()
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.action_login),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToRegister,
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = registerPromptAnnotated,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
