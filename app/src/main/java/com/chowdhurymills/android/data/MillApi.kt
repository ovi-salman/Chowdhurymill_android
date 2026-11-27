package com.chowdhurymills.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.net.URI
import java.util.concurrent.TimeUnit

class MillApi(private val store: SessionStore) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private suspend fun originFor(base: String): String {
        val uri = URI(base)
        val port = when {
            uri.port == -1 && uri.scheme == "https" -> 443
            uri.port == -1 && uri.scheme == "http" -> 80
            else -> uri.port
        }
        val authority = if ((uri.scheme == "http" && port == 80) || (uri.scheme == "https" && port == 443)) {
            uri.host
        } else {
            "${uri.host}:$port"
        }
        return "${uri.scheme}://$authority"
    }

    private suspend fun requestBuilder(
        path: String,
        mutating: Boolean = false,
    ): Request.Builder {
        val base = store.baseUrl()
        val builder = Request.Builder().url("$base$path")
        val cookie = store.sessionCookie()
        if (cookie.isNotBlank()) {
            builder.header("Cookie", cookie)
        }
        if (mutating) {
            val origin = originFor(base)
            builder.header("Origin", origin)
            builder.header("Sec-Fetch-Site", "same-origin")
            builder.header("Sec-Fetch-Mode", "cors")
            builder.header("Sec-Fetch-Dest", "empty")
        }
        return builder
    }

    private fun captureCookie(response: okhttp3.Response) {
        val setCookie = response.headers("Set-Cookie")
        if (setCookie.isEmpty()) return
        // Persist only the mill_session cookie value/attributes we need for Cookie header.
        val session = setCookie.firstOrNull { it.startsWith("mill_session=") }
            ?: return
        val pair = session.substringBefore(';')
        // Persist asynchronously via blocking call site; callers use suspend capture.
        cookiePending = pair
    }

    @Volatile
    private var cookiePending: String? = null

    private suspend fun flushCookie() {
        cookiePending?.let {
            if (it.endsWith("=") || it == "mill_session=") {
                store.clearSession()
            } else {
                store.setSessionCookie(it)
            }
            cookiePending = null
        }
    }

    private suspend inline fun <reified T> execute(
        request: Request,
        expectAuthFailure: Boolean = false,
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                captureCookie(response)
                flushCookie()
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val err = runCatching { json.decodeFromString<ErrorResponse>(body) }.getOrNull()
                    val message = err?.error
                        ?: when (response.code) {
                            401 -> if (expectAuthFailure) "Sign in required." else "Staff ID and password do not match."
                            403 -> "This role cannot open that page."
                            429 -> "Too many sign-in attempts. Wait a few minutes."
                            else -> "Request failed (${response.code})."
                        }
                    return@withContext ApiResult.Err(message, response.code)
                }
                if (body.isBlank()) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Ok(Unit as T)
                }
                ApiResult.Ok(json.decodeFromString(body))
            }
        } catch (ex: Exception) {
            ApiResult.Err(ex.message ?: "Could not reach the mill server.")
        }
    }

    suspend fun me(): ApiResult<MeResponse> {
        val request = requestBuilder("/api/me").get().build()
        return execute(request, expectAuthFailure = true)
    }

    suspend fun login(staffId: String, password: String, staffType: String): ApiResult<LoginResponse> {
        val payload = json.encodeToString(LoginRequest(staffId, password, staffType))
        val request = requestBuilder("/api/login", mutating = true)
            .post(payload.toRequestBody(mediaType))
            .build()
        return execute(request)
    }

    suspend fun logout(): ApiResult<Unit> {
        val request = requestBuilder("/api/logout", mutating = true)
            .post("{}".toRequestBody(mediaType))
            .build()
        execute<OkResponse>(request)
        store.clearSession()
        return ApiResult.Ok(Unit)
    }

    suspend fun directory(): ApiResult<DirectoryResponse> {
        val request = requestBuilder("/api/directory").get().build()
        return execute(request)
    }

    suspend fun searchAccounts(term: String): ApiResult<NamesResponse> {
        val request = requestBuilder("/api/accounts?term=${term.encodeUrl()}").get().build()
        return execute(request)
    }

    suspend fun ledger(name: String? = null, from: String? = null, to: String? = null): ApiResult<LedgerResponse> {
        val params = buildList {
            if (!name.isNullOrBlank()) add("name=${name.encodeUrl()}")
            if (!from.isNullOrBlank()) add("from=${from.encodeUrl()}")
            if (!to.isNullOrBlank()) add("to=${to.encodeUrl()}")
        }.joinToString("&")
        val path = if (params.isBlank()) "/api/ledger" else "/api/ledger?$params"
        val request = requestBuilder(path).get().build()
        return execute(request)
    }

    suspend fun day(date: String): ApiResult<DayResponse> {
        val request = requestBuilder("/api/transactions?date=${date.encodeUrl()}").get().build()
        return execute(request)
    }

    suspend fun updateTransaction(id: Long, quantity: String, amount: Double): ApiResult<OkResponse> {
        val payload = json.encodeToString(UpdateTransactionRequest(id, quantity, amount))
        val request = requestBuilder("/api/transactions", mutating = true)
            .patch(payload.toRequestBody(mediaType))
            .build()
        return execute(request)
    }

    suspend fun deleteTransaction(id: Long): ApiResult<OkResponse> {
        val payload = json.encodeToString(DeleteTransactionRequest(id))
        val request = requestBuilder("/api/transactions", mutating = true)
            .delete(payload.toRequestBody(mediaType))
            .build()
        return execute(request)
    }

    suspend fun outstanding(date: String, credit: Boolean): ApiResult<OutstandingResponse> {
        val creditFlag = if (credit) "1" else "0"
        val request = requestBuilder("/api/outstanding?date=${date.encodeUrl()}&credit=$creditFlag").get().build()
        return execute(request)
    }

    suspend fun postDay(date: String, lines: List<PostingLine>): ApiResult<PostingResponse> {
        val payload = json.encodeToString(PostingRequest(date, lines))
        val request = requestBuilder("/api/posting", mutating = true)
            .post(payload.toRequestBody(mediaType))
            .build()
        return execute(request)
    }

    suspend fun createAccount(name: String, phone: String, details: String): ApiResult<CreateAccountResponse> {
        val payload = json.encodeToString(CreateAccountRequest(name, phone, details))
        val request = requestBuilder("/api/accounts", mutating = true)
            .post(payload.toRequestBody(mediaType))
            .build()
        return execute(request)
    }
}

sealed class ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>()
    data class Err(val message: String, val code: Int = 0) : ApiResult<Nothing>()
}

private fun String.encodeUrl(): String = java.net.URLEncoder.encode(this, Charsets.UTF_8.name())
