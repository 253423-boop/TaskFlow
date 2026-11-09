package com.ivanna.proyects.taskflow.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ivanna.proyects.taskflow.auth.presentation.components.AuthTextField
import com.ivanna.proyects.taskflow.auth.presentation.components.TaskFlowHeader
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.RegisterViewModel
import com.upchiapas.taskflow.R

@Composable
fun RegisterPage(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) {
            onRegisterSuccess()
        }
    }

    val goToLoginText = stringResource(id = R.string.action_go_to_login)
    val loginHighlight = stringResource(id = R.string.login_title)
    val loginPromptAnnotated = buildAnnotatedString {
        val highlightIndex = goToLoginText.indexOf(loginHighlight)
        if (highlightIndex >= 0) {
            append(goToLoginText.substring(0, highlightIndex))
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(goToLoginText.substring(highlightIndex, highlightIndex + loginHighlight.length))
            }
            append(goToLoginText.substring(highlightIndex + loginHighlight.length))
        } else {
            append(goToLoginText)
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateToLogin,
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = stringResource(id = R.string.action_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TaskFlowHeader(
                title = stringResource(id = R.string.register_title)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = stringResource(id = R.string.label_name),
                placeholder = stringResource(id = R.string.placeholder_name),
                errorMessageRes = uiState.nameErrorRes,
                keyboardCapitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                imeAction = ImeAction.Next,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = stringResource(id = R.string.label_confirm_password),
                placeholder = stringResource(id = R.string.placeholder_password),
                isPassword = true,
                errorMessageRes = uiState.confirmPasswordErrorRes,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onRegisterSubmit()
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
                    viewModel.onRegisterSubmit()
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
                        text = stringResource(id = R.string.action_register),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = loginPromptAnnotated,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
