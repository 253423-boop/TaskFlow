package com.ivanna.proyects.taskflow.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivanna.proyects.taskflow.TaskFlowApplication
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthFooterLink
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthServerErrorText
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthSessionExpiredNotice
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthTextField
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.LoginViewModel
import com.ivanna.proyects.taskflow.ui.components.TaskFlowHeader
import com.ivanna.proyects.taskflow.ui.components.TaskFlowPrimaryButton
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTheme
import com.upchiapas.taskflow.R

@Composable
fun LoginPage(
    sessionExpired: Boolean = false,
    viewModel: LoginViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as TaskFlowApplication)
            .authModule
            .createLoginFactory(sessionExpired)
    ),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onLoginSubmit,
        onNavigateToRegister = onNavigateToRegister,
        onDismissNotice = viewModel::clearServerError
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    onDismissNotice: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val resolvedServerError = uiState.serverErrorMessage
        ?: uiState.serverErrorRes?.let { stringResource(id = it) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = 28.dp)
    ) {
        TaskFlowHeader(
            title = stringResource(id = R.string.login_title),
            showBack = false
        )

        Spacer(modifier = Modifier.height(58.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 27.6.dp)
        ) {
            AuthTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = stringResource(id = R.string.label_email),
                placeholder = stringResource(id = R.string.placeholder_email),
                errorMessage = uiState.emailErrorRes?.let { stringResource(id = it) },
                enabled = !uiState.isLoading,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(23.dp))

            AuthTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = stringResource(id = R.string.label_password),
                placeholder = stringResource(id = R.string.placeholder_password),
                errorMessage = uiState.passwordErrorRes?.let { stringResource(id = it) },
                enabled = !uiState.isLoading,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSubmit()
                    }
                )
            )

            when {
                uiState.isSessionExpiredNotice -> {
                    Spacer(modifier = Modifier.height(14.dp))
                    AuthSessionExpiredNotice(onDismiss = onDismissNotice)
                    Spacer(modifier = Modifier.height(18.dp))
                }
                !resolvedServerError.isNullOrBlank() -> {
                    Spacer(modifier = Modifier.height(20.dp))
                    AuthServerErrorText(
                        message = resolvedServerError,
                        style = when (uiState.serverErrorKind) {
                            LoginErrorKind.INVALID_CREDENTIALS -> TaskFlowTextStyles.loginErrorBold
                            else -> TaskFlowTextStyles.authErrorCompact
                        }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                }
                else -> {
                    Spacer(modifier = Modifier.height(68.dp))
                }
            }

            TaskFlowPrimaryButton(
                text = stringResource(id = R.string.action_login),
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                loadingText = stringResource(id = R.string.action_signing_in)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthFooterLink(
                text = stringResource(id = R.string.action_go_to_register),
                textStyle = TaskFlowTextStyles.authLinkLarge,
                onClick = onNavigateToRegister,
                enabled = !uiState.isLoading
            )
        }
    }
}

@Preview(name = "03 · Inicio de sesión", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview03Login() {
    TaskFlowTheme {
        LoginContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(name = "14 · Credenciales inválidas", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview14LoginInvalidCredentials() {
    TaskFlowTheme {
        LoginContent(
            uiState = LoginUiState(
                serverErrorRes = R.string.error_invalid_credentials,
                serverErrorKind = LoginErrorKind.INVALID_CREDENTIALS
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(name = "15 · Sesión caducada", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview15LoginSessionExpired() {
    TaskFlowTheme {
        LoginContent(
            uiState = LoginUiState(isSessionExpiredNotice = true),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(name = "21 · Error de conexión al entrar", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview21LoginConnectionError() {
    TaskFlowTheme {
        LoginContent(
            uiState = LoginUiState(
                serverErrorRes = R.string.error_no_connection,
                serverErrorKind = LoginErrorKind.CONNECTION
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onNavigateToRegister = {}
        )
    }
}
