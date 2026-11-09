package com.ivanna.proyects.taskflow

import com.ivanna.proyects.taskflow.auth.data.datasource.AuthApiService
import com.ivanna.proyects.taskflow.auth.data.dto.AuthResponseDto
import com.ivanna.proyects.taskflow.auth.data.dto.LoginRequestDto
import com.ivanna.proyects.taskflow.auth.data.dto.RegisterRequestDto
import com.ivanna.proyects.taskflow.auth.data.dto.UserDto
import com.ivanna.proyects.taskflow.auth.data.repository.AuthRepositoryImpl
import com.ivanna.proyects.taskflow.auth.domain.model.User
import com.ivanna.proyects.taskflow.auth.domain.repository.AuthRepository
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.AuthValidation
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.LoginViewModel
import com.ivanna.proyects.taskflow.auth.presentation.viewmodels.RegisterViewModel
import com.ivanna.proyects.taskflow.core.network.ApiException
import com.ivanna.proyects.taskflow.core.network.AuthInterceptor
import com.ivanna.proyects.taskflow.core.network.isUnauthorizedError
import com.ivanna.proyects.taskflow.core.session.SessionManager
import com.ivanna.proyects.taskflow.tasks.data.datasource.TasksApiService
import com.ivanna.proyects.taskflow.tasks.data.dto.CreateTaskDto
import com.ivanna.proyects.taskflow.tasks.data.dto.TaskDto
import com.ivanna.proyects.taskflow.tasks.data.dto.UpdateTaskDto
import com.ivanna.proyects.taskflow.tasks.data.repository.TasksRepositoryImpl
import com.ivanna.proyects.taskflow.tasks.domain.model.Task
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import com.ivanna.proyects.taskflow.tasks.domain.repository.TasksRepository
import com.ivanna.proyects.taskflow.tasks.presentation.viewmodels.TaskFormViewModel
import com.upchiapas.taskflow.R
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response as OkHttpResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AuthValidationAndViewModelTest {

    @Test
    fun emailValidatorRejectsMissingDomainTldAndAcceptsValidEmails() {
        assertFalse(AuthValidation.isValidEmail("ivanna@upchiapas"))
        assertFalse(AuthValidation.isValidEmail(""))
        assertTrue(AuthValidation.isValidEmail("correo@ejemplo.com"))
        assertTrue(AuthValidation.isValidEmail("ivanna.munoz@upchiapas.edu.mx"))
    }

    @Test
    fun authInterceptorAddsBearerHeaderOnlyWhenTokenPresent() {
        val fakeSession = FakeSessionManager()
        val interceptor = AuthInterceptor(fakeSession)

        val unauthenticatedChain = RecordingInterceptorChain(
            Request.Builder().url("https://taskflow.34-204-242-28.sslip.io/api/v1/tasks/").build()
        )
        interceptor.intercept(unauthenticatedChain)
        assertNull(unauthenticatedChain.proceededRequest?.header("Authorization"))

        fakeSession.saveToken("secret-jwt")
        val authenticatedChain = RecordingInterceptorChain(
            Request.Builder().url("https://taskflow.34-204-242-28.sslip.io/api/v1/tasks/").build()
        )
        interceptor.intercept(authenticatedChain)
        assertEquals(
            "Bearer secret-jwt",
            authenticatedChain.proceededRequest?.header("Authorization")
        )
    }

    @Test
    fun authRepositoryMapsTypedExceptionsAndPersistsSessionFields() = runBlocking {
        val fakeSession = FakeSessionManager()
        val fakeApi = FakeAuthApiService()
        val repository = AuthRepositoryImpl(apiService = fakeApi, sessionManager = fakeSession)

        val success = repository.login("correo@ejemplo.com", "12345678")
        assertTrue(success.isSuccess)
        assertEquals("jwt-token", fakeSession.getToken())
        assertEquals("Ivanna Muñoz", fakeSession.getUserName())
        assertEquals(3, fakeSession.getUserId())
        assertTrue(repository.isUserLoggedIn())

        repository.logout()
        assertFalse(repository.isUserLoggedIn())
        assertNull(fakeSession.getToken())

        val registerSuccess = repository.register("Ivanna Muñoz", "correo@ejemplo.com", "12345678", "12345678")
        assertTrue(registerSuccess.isSuccess)
        assertEquals("jwt-token", fakeSession.getToken())

        fakeApi.loginAction = {
            throw HttpException(
                Response.error<AuthResponseDto>(
                    401,
                    """{"detail":"Credenciales inválidas"}""".toResponseBody("application/json".toMediaType())
                )
            )
        }
        val invalidCredentials = repository.login("correo@ejemplo.com", "wrong")
        assertTrue(invalidCredentials.exceptionOrNull() is ApiException.InvalidCredentials)

        fakeApi.loginAction = { throw IOException("timeout") }
        val noConnection = repository.login("correo@ejemplo.com", "12345678")
        assertTrue(noConnection.exceptionOrNull() is ApiException.NoConnection)

        fakeApi.registerAction = {
            throw HttpException(
                Response.error<AuthResponseDto>(
                    409,
                    """{"detail":"Correo ya registrado"}""".toResponseBody("application/json".toMediaType())
                )
            )
        }
        val duplicateEmail = repository.register("Ivanna", "correo@registrado.com", "12345678", "12345678")
        assertTrue(duplicateEmail.exceptionOrNull() is ApiException.DuplicateEmail)

        fakeApi.registerAction = {
            throw HttpException(
                Response.error<AuthResponseDto>(
                    422,
                    """{"detail":[{"msg":"La contraseña debe tener al menos 8 caracteres"}]}""".toResponseBody("application/json".toMediaType())
                )
            )
        }
        val shortPassword = repository.register("Ivanna", "correo@ejemplo.com", "1234", "1234")
        assertTrue(shortPassword.exceptionOrNull() is ApiException.ShortPassword)

        fakeApi.getMeAction = {
            throw HttpException(
                Response.error<UserDto>(
                    401,
                    """{"detail":"Token expirado"}""".toResponseBody("application/json".toMediaType())
                )
            )
        }
        val expiredSession = repository.getCurrentUser()
        assertTrue(expiredSession.exceptionOrNull() is ApiException.Unauthorized)
        assertTrue(expiredSession.exceptionOrNull()?.isUnauthorizedError() == true)
    }

    @Test
    fun tasksRepositorySupportsCrudAndMapsUnauthorizedAndNetworkErrors() = runBlocking {
        val fakeTasksApi = FakeTasksApiService()
        val repository = TasksRepositoryImpl(apiService = fakeTasksApi)

        val created = repository.createTask(
            titulo = "  Nueva tarea  ",
            materia = "  Redes  ",
            descripcion = "   ",
            fechaEntrega = "2026-11-20T23:59:00",
            prioridad = TaskPriority.ALTA
        )
        assertTrue(created.isSuccess)
        assertEquals("Nueva tarea", created.getOrThrow().titulo)
        assertNull(created.getOrThrow().descripcion)

        val updated = repository.updateTask(
            id = 1,
            titulo = "Actualizada",
            materia = "Redes",
            descripcion = "Detalle",
            fechaEntrega = "2026-11-21T23:59:00",
            prioridad = TaskPriority.BAJA,
            estado = TaskStatus.PENDIENTE
        )
        assertTrue(updated.isSuccess)
        assertEquals(TaskPriority.BAJA, updated.getOrThrow().prioridad)

        val completed = repository.completeTask(1)
        assertTrue(completed.isSuccess)
        assertEquals(TaskStatus.COMPLETADA, completed.getOrThrow().estado)

        val nextDelivery = repository.getNextDelivery()
        assertTrue(nextDelivery.isSuccess)
        assertNull(nextDelivery.getOrThrow())

        val deleted = repository.deleteTask(1)
        assertTrue(deleted.isSuccess)

        fakeTasksApi.getTasksAction = {
            throw HttpException(
                Response.error<List<TaskDto>>(
                    401,
                    """{"detail":"No autorizado"}""".toResponseBody("application/json".toMediaType())
                )
            )
        }
        val unauthorizedResult = repository.getTasks(null)
        assertTrue(unauthorizedResult.exceptionOrNull() is ApiException.Unauthorized)

        fakeTasksApi.getTasksAction = { throw IOException("offline") }
        val offlineResult = repository.getTasks(null)
        assertTrue(offlineResult.exceptionOrNull() is ApiException.NoConnection)
    }

    @Test
    fun loginViewModelValidatesEmailAndRequiredPassword() {
        val viewModel = LoginViewModel(
            authRepository = StubAuthRepository(),
            initialSessionExpired = true
        )
        assertTrue(viewModel.uiState.value.isSessionExpiredNotice)

        viewModel.onEmailChange("ivanna@upchiapas")
        assertFalse(viewModel.uiState.value.isSessionExpiredNotice)

        viewModel.onLoginSubmit()
        assertEquals(R.string.error_invalid_email, viewModel.uiState.value.emailErrorRes)
        assertEquals(R.string.error_required_field, viewModel.uiState.value.passwordErrorRes)
    }

    @Test
    fun registerViewModelValidatesNameEmailShortPasswordAndMismatch() {
        val viewModel = RegisterViewModel(authRepository = StubAuthRepository())
        viewModel.onNameChange("Ivanna Muñoz")
        viewModel.onEmailChange("ivanna@upchiapas")
        viewModel.onPasswordChange("123456")
        viewModel.onConfirmPasswordChange("different")
        viewModel.onRegisterSubmit()

        assertNull(viewModel.uiState.value.nameErrorRes)
        assertEquals(R.string.error_invalid_email, viewModel.uiState.value.emailErrorRes)
        assertEquals(R.string.error_short_password, viewModel.uiState.value.passwordErrorRes)
        assertEquals(R.string.error_passwords_mismatch, viewModel.uiState.value.confirmPasswordErrorRes)
    }

    @Test
    fun taskFormViewModelValidatesRequiredFieldsMaxLengthAndAcceptsPastDates() {
        val viewModel = TaskFormViewModel(
            taskId = null,
            tasksRepository = StubTasksRepository(),
            authRepository = StubAuthRepository()
        )

        viewModel.onSubmit()
        assertEquals(R.string.error_title_required, viewModel.uiState.value.tituloErrorRes)
        assertEquals(R.string.error_subject_required, viewModel.uiState.value.materiaErrorRes)
        assertEquals(R.string.error_due_date_required, viewModel.uiState.value.fechaErrorRes)

        viewModel.onTituloChange("a".repeat(101))
        assertEquals(R.string.error_title_max_length, viewModel.uiState.value.tituloErrorRes)

        viewModel.onTituloChange("Tarea válida")
        viewModel.onMateriaChange("Redes")
        viewModel.onDateTimeSelected(LocalDate.of(2020, 1, 1), LocalTime.of(12, 0))
        assertNull(viewModel.uiState.value.tituloErrorRes)
        assertNull(viewModel.uiState.value.fechaErrorRes)
        assertEquals(LocalDate.of(2020, 1, 1), viewModel.uiState.value.selectedDate)
    }

    private class RecordingInterceptorChain(
        private val initialRequest: Request
    ) : Interceptor.Chain {
        var proceededRequest: Request? = null

        override fun request(): Request = initialRequest

        override fun proceed(request: Request): OkHttpResponse {
            proceededRequest = request
            return OkHttpResponse.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }

        override fun connection(): Connection? = null
        override fun call(): Call {
            throw UnsupportedOperationException()
        }

        override fun connectTimeoutMillis(): Int = 30_000
        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        override fun readTimeoutMillis(): Int = 30_000
        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        override fun writeTimeoutMillis(): Int = 30_000
        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    }

    private class FakeSessionManager : SessionManager {
        private var token: String? = null
        private var userName: String? = null
        private var userId: Int? = null

        override fun saveToken(token: String) {
            this.token = token
        }

        override fun getToken(): String? = token

        override fun saveUserName(name: String) {
            this.userName = name
        }

        override fun getUserName(): String? = userName

        override fun saveUserId(userId: Int) {
            this.userId = userId
        }

        override fun getUserId(): Int? = userId

        override fun clearSession() {
            token = null
            userName = null
            userId = null
        }

        override fun hasActiveSession(): Boolean = !token.isNullOrBlank()
    }

    private class FakeAuthApiService : AuthApiService {
        var loginAction: suspend (LoginRequestDto) -> AuthResponseDto = {
            AuthResponseDto(
                accessToken = "jwt-token",
                tokenType = "bearer",
                user = UserDto(id = 3, nombre = "Ivanna Muñoz", correo = it.correo)
            )
        }
        var registerAction: suspend (RegisterRequestDto) -> AuthResponseDto = {
            AuthResponseDto(
                accessToken = "jwt-token",
                tokenType = "bearer",
                user = UserDto(id = 3, nombre = it.nombre, correo = it.correo)
            )
        }
        var getMeAction: suspend () -> UserDto = {
            UserDto(id = 3, nombre = "Ivanna Muñoz", correo = "correo@ejemplo.com")
        }

        override suspend fun register(request: RegisterRequestDto): AuthResponseDto = registerAction(request)
        override suspend fun login(request: LoginRequestDto): AuthResponseDto = loginAction(request)
        override suspend fun getMe(): UserDto = getMeAction()
    }

    private class FakeTasksApiService : TasksApiService {
        var getTasksAction: suspend (String?) -> List<TaskDto> = { emptyList() }

        override suspend fun getTasks(estado: String?): List<TaskDto> = getTasksAction(estado)
        override suspend fun getNextDelivery(): Response<JsonElement> = Response.success(JsonNull)
        override suspend fun getTaskById(id: Int): TaskDto {
            return TaskDto(
                id = id,
                userId = 3,
                titulo = "Tarea",
                materia = "Redes",
                descripcion = null,
                fechaEntrega = "2026-11-20T23:59:00",
                prioridad = 2,
                estado = "pendiente",
                vencida = false,
                createdAt = "2026-11-05T10:24:00"
            )
        }

        override suspend fun createTask(body: CreateTaskDto): TaskDto {
            return TaskDto(
                id = 1,
                userId = 3,
                titulo = body.titulo,
                materia = body.materia,
                descripcion = body.descripcion,
                fechaEntrega = body.fechaEntrega,
                prioridad = body.prioridad,
                estado = body.estado,
                vencida = false,
                createdAt = "2026-11-05T10:24:00"
            )
        }

        override suspend fun updateTask(id: Int, body: UpdateTaskDto): TaskDto {
            return TaskDto(
                id = id,
                userId = 3,
                titulo = body.titulo ?: "Tarea",
                materia = body.materia ?: "Redes",
                descripcion = body.descripcion,
                fechaEntrega = body.fechaEntrega ?: "2026-11-20T23:59:00",
                prioridad = body.prioridad ?: 2,
                estado = body.estado ?: "pendiente",
                vencida = false,
                createdAt = "2026-11-05T10:24:00"
            )
        }

        override suspend fun completeTask(id: Int): TaskDto {
            return TaskDto(
                id = id,
                userId = 3,
                titulo = "Tarea",
                materia = "Redes",
                descripcion = null,
                fechaEntrega = "2026-11-20T23:59:00",
                prioridad = 2,
                estado = "completada",
                vencida = false,
                createdAt = "2026-11-05T10:24:00"
            )
        }

        override suspend fun deleteTask(id: Int): Response<Unit> = Response.success(Unit)
    }

    private class StubAuthRepository : AuthRepository {
        override suspend fun login(correo: String, password: String): Result<User> =
            Result.success(User(3, "Ivanna Muñoz", correo))

        override suspend fun register(
            nombre: String,
            correo: String,
            password: String,
            passwordConfirm: String
        ): Result<User> = Result.success(User(3, nombre, correo))

        override suspend fun getCurrentUser(): Result<User> =
            Result.success(User(3, "Ivanna Muñoz", "correo@ejemplo.com"))

        override fun logout() {}
        override fun isUserLoggedIn(): Boolean = false
    }

    private class StubTasksRepository : TasksRepository {
        private val sample = Task(
            id = 1,
            userId = 3,
            titulo = "Tarea",
            materia = "Redes",
            descripcion = null,
            fechaEntrega = "2026-11-20T23:59:00",
            prioridad = TaskPriority.MEDIA,
            estado = TaskStatus.PENDIENTE,
            vencida = false,
            createdAt = "2026-11-05T10:24:00"
        )

        override suspend fun getTasks(estado: TaskStatus?): Result<List<Task>> = Result.success(listOf(sample))
        override suspend fun getNextDelivery(): Result<Task?> = Result.success(sample)
        override suspend fun getTaskById(id: Int): Result<Task> = Result.success(sample)
        override suspend fun createTask(
            titulo: String,
            materia: String,
            descripcion: String?,
            fechaEntrega: String,
            prioridad: TaskPriority,
            estado: TaskStatus
        ): Result<Task> = Result.success(sample)

        override suspend fun updateTask(
            id: Int,
            titulo: String,
            materia: String,
            descripcion: String?,
            fechaEntrega: String,
            prioridad: TaskPriority,
            estado: TaskStatus
        ): Result<Task> = Result.success(sample)

        override suspend fun completeTask(id: Int): Result<Task> = Result.success(sample)
        override suspend fun deleteTask(id: Int): Result<Unit> = Result.success(Unit)
    }
}
