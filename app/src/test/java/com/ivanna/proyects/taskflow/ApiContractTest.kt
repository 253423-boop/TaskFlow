package com.ivanna.proyects.taskflow

import com.ivanna.proyects.taskflow.auth.data.datasource.AuthApiService
import com.ivanna.proyects.taskflow.auth.data.repository.AuthRepositoryImpl
import com.ivanna.proyects.taskflow.core.network.ApiException
import com.ivanna.proyects.taskflow.core.network.AuthInterceptor
import com.ivanna.proyects.taskflow.core.session.SessionManager
import com.ivanna.proyects.taskflow.tasks.data.datasource.TasksApiService
import com.ivanna.proyects.taskflow.tasks.data.repository.TasksRepositoryImpl
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskPriority
import com.ivanna.proyects.taskflow.tasks.domain.model.TaskStatus
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ApiContractTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun authEndpointsMatchFastApiPathsPayloadsAndErrors() = runBlocking {
        val recorded = mutableListOf<RecordedCall>()
        var nextCode = 201
        var nextBody =
            """{"access_token":"jwt.123","token_type":"bearer","user":{"id":1,"nombre":"Ivanna","correo":"ivanna@example.com"}}"""

        val sessionManager = InMemorySessionManager()
        val retrofit = createTestRetrofit(sessionManager) { request, bodyText ->
            recorded += RecordedCall(
                method = request.method,
                pathAndQuery = request.url.encodedPath + (request.url.encodedQuery?.let { "?$it" } ?: ""),
                authHeader = request.header("Authorization"),
                body = bodyText
            )
            buildJsonResponse(request, nextCode, nextBody)
        }
        val authRepository = AuthRepositoryImpl(
            apiService = retrofit.create(AuthApiService::class.java),
            sessionManager = sessionManager
        )

        val regResult = authRepository.register("Ivanna", "ivanna@example.com", "password123", "password123")
        assertTrue(regResult.isSuccess)
        assertEquals("POST", recorded.last().method)
        assertEquals("/api/v1/auth/register", recorded.last().pathAndQuery)
        val regJson = Json.parseToJsonElement(recorded.last().body).jsonObject
        assertEquals("password123", regJson["password_confirm"]?.jsonPrimitive?.content)

        nextCode = 400
        nextBody = """{"detail":"El correo ya está registrado."}"""
        val dupResult = authRepository.register("Ivanna", "ivanna@example.com", "password123", "password123")
        assertTrue(dupResult.exceptionOrNull() is ApiException.DuplicateEmail)

        nextCode = 422
        nextBody = """{"detail":[{"type":"string_too_short","loc":["body","password"],"msg":"String should have at least 8 characters","input":"123"}]}"""
        val shortPwResult = authRepository.register("Ivanna", "ivanna@example.com", "123", "123")
        assertTrue(shortPwResult.exceptionOrNull() is ApiException.ShortPassword)

        nextCode = 200
        nextBody =
            """{"access_token":"jwt.login","token_type":"bearer","user":{"id":2,"nombre":"Test User","correo":"test@example.com"}}"""
        val loginResult = authRepository.login("test@example.com", "password123")
        assertTrue(loginResult.isSuccess)
        assertEquals("/api/v1/auth/login", recorded.last().pathAndQuery)

        nextCode = 401
        nextBody = """{"detail":"Credenciales incorrectas."}"""
        val badLogin = authRepository.login("test@example.com", "wrong")
        assertTrue(badLogin.exceptionOrNull() is ApiException.InvalidCredentials)

        nextCode = 200
        nextBody = """{"id":2,"nombre":"Test User","correo":"test@example.com"}"""
        val meResult = authRepository.getCurrentUser()
        assertTrue(meResult.isSuccess)
        assertEquals("/api/v1/auth/me", recorded.last().pathAndQuery)
        assertEquals("Bearer jwt.login", recorded.last().authHeader)

        nextCode = 401
        nextBody = """{"detail":"No se pudieron validar las credenciales. Token inválido o expirado."}"""
        val expiredMe = authRepository.getCurrentUser()
        assertTrue(expiredMe.exceptionOrNull() is ApiException.Unauthorized)
    }

    @Test
    fun tasksEndpointsMatchFastApiRoutesNullProximaEntregaAndClearDescription() = runBlocking {
        val recorded = mutableListOf<RecordedCall>()
        var nextCode = 200
        var nextBody = "[]"

        val sessionManager = InMemorySessionManager().apply { saveToken("jwt.tasks") }
        val retrofit = createTestRetrofit(sessionManager) { request, bodyText ->
            recorded += RecordedCall(
                method = request.method,
                pathAndQuery = request.url.encodedPath + (request.url.encodedQuery?.let { "?$it" } ?: ""),
                authHeader = request.header("Authorization"),
                body = bodyText
            )
            buildJsonResponse(request, nextCode, nextBody)
        }
        val tasksRepository = TasksRepositoryImpl(
            apiService = retrofit.create(TasksApiService::class.java),
            json = json
        )

        nextCode = 200
        nextBody =
            """[{"id":1,"user_id":1,"titulo":"Reporte de Redes","materia":"Redes","descripcion":"Doc","fecha_entrega":"2026-11-10T14:00:00","estado":"pendiente","prioridad":3,"created_at":"2026-11-09T06:47:18.049212","vencida":false}]"""
        val filtered = tasksRepository.getTasks(TaskStatus.PENDIENTE)
        assertTrue(filtered.isSuccess)
        assertEquals("GET", recorded.last().method)
        assertEquals("/api/v1/tasks/?estado=pendiente", recorded.last().pathAndQuery)
        assertEquals("Bearer jwt.tasks", recorded.last().authHeader)

        nextCode = 200
        nextBody = "null"
        val emptyNext = tasksRepository.getNextDelivery()
        assertTrue(emptyNext.isSuccess)
        assertNull(emptyNext.getOrThrow())
        assertEquals("/api/v1/tasks/proxima-entrega", recorded.last().pathAndQuery)

        nextCode = 200
        nextBody =
            """{"id":5,"user_id":1,"titulo":"Examen","materia":"Redes","descripcion":null,"fecha_entrega":"2026-11-11T10:00:00","estado":"pendiente","prioridad":2,"created_at":"2026-11-09T06:47:18.049212","vencida":false}"""
        val singleNext = tasksRepository.getNextDelivery()
        assertTrue(singleNext.isSuccess)
        assertEquals(5, singleNext.getOrThrow()?.id)

        nextCode = 200
        nextBody =
            """{"id":5,"user_id":1,"titulo":"Examen edit","materia":"Redes","descripcion":null,"fecha_entrega":"2026-11-12T10:00:00","estado":"pendiente","prioridad":1,"created_at":"2026-11-09T06:47:18.049212","vencida":false}"""
        val updated = tasksRepository.updateTask(
            id = 5,
            titulo = "Examen edit",
            materia = "Redes",
            descripcion = null,
            fechaEntrega = "2026-11-12T10:00:00",
            prioridad = TaskPriority.BAJA,
            estado = TaskStatus.PENDIENTE
        )
        assertTrue(updated.isSuccess)
        assertEquals("PUT", recorded.last().method)
        assertEquals("/api/v1/tasks/5", recorded.last().pathAndQuery)
        val updateJson = Json.parseToJsonElement(recorded.last().body).jsonObject
        assertTrue(updateJson.containsKey("descripcion"))
        assertEquals(JsonNull, updateJson["descripcion"])

        nextCode = 200
        nextBody =
            """{"id":5,"user_id":1,"titulo":"Examen edit","materia":"Redes","descripcion":null,"fecha_entrega":"2026-11-12T10:00:00","estado":"completada","prioridad":1,"created_at":"2026-11-09T06:47:18.049212","vencida":false}"""
        val completed = tasksRepository.completeTask(5)
        assertTrue(completed.isSuccess)
        assertEquals("PUT", recorded.last().method)
        assertEquals("/api/v1/tasks/5/completar", recorded.last().pathAndQuery)

        nextCode = 204
        nextBody = ""
        val deleted = tasksRepository.deleteTask(5)
        assertTrue(deleted.isSuccess)
        assertEquals("DELETE", recorded.last().method)
        assertEquals("/api/v1/tasks/5", recorded.last().pathAndQuery)
    }

    private fun createTestRetrofit(
        sessionManager: SessionManager,
        handler: (Request, String) -> Response
    ): Retrofit {
        val mockInterceptor = Interceptor { chain ->
            val request = chain.request()
            val bodyBuffer = Buffer()
            request.body?.writeTo(bodyBuffer)
            handler(request, bodyBuffer.readUtf8())
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(mockInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://taskflow.34-204-242-28.sslip.io/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private fun buildJsonResponse(request: Request, code: Int, body: String): Response {
        val responseBody = if (code == 204) {
            ByteArray(0).toResponseBody(null)
        } else {
            body.toResponseBody("application/json".toMediaType())
        }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("HTTP $code")
            .body(responseBody)
            .build()
    }

    private data class RecordedCall(
        val method: String,
        val pathAndQuery: String,
        val authHeader: String?,
        val body: String
    )

    private class InMemorySessionManager : SessionManager {
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
}
