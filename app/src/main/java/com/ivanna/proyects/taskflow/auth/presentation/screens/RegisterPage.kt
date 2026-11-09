package com.ivanna.proyects.taskflow.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivanna.proyects.taskflow.TaskFlowApplication
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthFooterLink
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthServerErrorText
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthTextField
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.RegisterViewModel
import com.ivanna.proyects.taskflow.ui.components.TaskFlowHeader
import com.ivanna.proyects.taskflow.ui.components.TaskFlowPrimaryButton
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTextStyles
import com.ivanna.proyects.taskflow.ui.theme.TaskFlowTheme
import com.upchiapas.taskflow.R

@Composable
fun RegisterPage(
    viewModel: RegisterViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as TaskFlowApplication)
            .authModule
            .factoryRegister
    ),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) {
            onRegisterSuccess()
        }
    }

    RegisterContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::onRegisterSubmit,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    showLoginLink: Boolean = true
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
            title = stringResource(id = R.string.register_title),
            showBack = true,
            backEnabled = !uiState.isLoading,
            onBack = onNavigateToLogin
        )

        Spacer(modifier = Modifier.height(26.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 27.6.dp)
        ) {
            AuthTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = stringResource(id = R.string.label_name),
                placeholder = stringResource(id = R.string.placeholder_name),
                errorMessage = uiState.nameErrorRes?.let { stringResource(id = it) },
                enabled = !uiState.isLoading,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(19.dp))

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

            Spacer(modifier = Modifier.height(19.dp))

            AuthTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = stringResource(id = R.string.label_password),
                placeholder = stringResource(id = R.string.placeholder_password),
                errorMessage = uiState.passwordErrorRes?.let { stringResource(id = it) },
                enabled = !uiState.isLoading,
                isPassword = true,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(19.dp))

            AuthTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(id = R.string.label_confirm_password),
                placeholder = stringResource(id = R.string.placeholder_password),
                errorMessage = uiState.confirmPasswordErrorRes?.let { stringResource(id = it) },
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

            Spacer(modifier = Modifier.height(42.dp))

            TaskFlowPrimaryButton(
                text = stringResource(id = R.string.action_register),
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                loadingText = stringResource(id = R.string.action_registering)
            )

            if (showLoginLink) {
                Spacer(modifier = Modifier.height(6.dp))
                AuthFooterLink(
                    text = stringResource(id = R.string.action_go_to_login),
                    textStyle = TaskFlowTextStyles.authLink,
                    onClick = onNavigateToLogin,
                    enabled = !uiState.isLoading,
                    contentAlignment = Alignment.CenterStart,
                    contentPadding = PaddingValues(start = 20.7.dp, end = 8.dp)
                )
            }

            if (!resolvedServerError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                AuthServerErrorText(message = resolvedServerError)
            }
        }
    }
}

@Preview(name = "01 · Registro", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview01Register() {
    TaskFlowTheme {
        RegisterContent(
            uiState = RegisterUiState(),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "02 · Validaciones", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview02RegisterValidations() {
    TaskFlowTheme {
        RegisterContent(
            uiState = RegisterUiState(
                name = stringResource(id = R.string.preview_user_name),
                email = stringResource(id = R.string.preview_bad_email),
                password = stringResource(id = R.string.preview_valid_password_fixture),
                confirmPassword = stringResource(id = R.string.preview_mismatch_password_fixture),
                emailErrorRes = R.string.error_invalid_email,
                confirmPasswordErrorRes = R.string.error_passwords_mismatch
            ),
            showLoginLink = false,
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "19 · Contraseña corta", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview19ShortPassword() {
    TaskFlowTheme {
        RegisterContent(
            uiState = RegisterUiState(
                password = stringResource(id = R.string.preview_short_password_fixture),
                confirmPassword = stringResource(id = R.string.preview_valid_password_fixture),
                serverErrorRes = R.string.error_short_password
            ),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "20 · Correo duplicado", showBackground = true, widthDp = 414, heightDp = 771)
@Composable
private fun Preview20DuplicateEmail() {
    TaskFlowTheme {
        RegisterContent(
            uiState = RegisterUiState(
                email = stringResource(id = R.string.preview_registered_email),
                serverErrorRes = R.string.error_email_already_registered
            ),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToLogin = {}
        )
    }
}
