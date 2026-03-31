package nr.dev.ezemkofi2

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class HttpReq(
    val url: String,
    val body: String = "",
    val method: String = "GET",
    val headers: Map<String, String> = emptyMap(),
    val timeout: Int = 10000
)

data class HttpRes(
    val code: Int,
    val body: String? = null,
    val bytes: ByteArray? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val errors: String? = null
)

data class User(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String
)

object HttpClient {
    val addr = "http://10.0.2.2:5000/"

    var token: String = ""

    var user by mutableStateOf<User?>(null)

    fun send(req: HttpReq, getByte: Boolean = false): HttpRes {
        val conn = URL(req.url).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = req.method
            conn.readTimeout = req.timeout
            conn.connectTimeout = req.timeout
            req.headers.forEach { (t, u) -> conn.setRequestProperty(t, u) }
            if(req.body.isNotEmpty() && req.method in listOf("POST", "PATCH", "PUT")) {
                conn.getOutputStream().buffered().use { it.write(req.body.toByteArray()) }
            }

            conn.connect()
            val code = conn.responseCode
            val body = if(getByte) {
                null
            } else {
                if(code in 200..299) {
                    conn.getInputStream().bufferedReader().use { it.readText() }
                } else {
                    conn.errorStream?.bufferedReader()?.use { it.readText() }
                }
            }
            val bytes = if(!getByte) {
                null
            } else {
                if(code in 200..299) {
                    conn.getInputStream().buffered().use { it.readBytes() }
                } else {
                    conn.errorStream?.buffered()?.use { it.readBytes() }
                }
            }

            HttpRes(
                code,
                body,
                bytes,
                conn.headerFields
            )
        } catch (e: Exception) {
            HttpRes(
                -1,
                errors = e.message ?: "Network error"
            )
        } finally {
            conn.disconnect()
        }
    }

    suspend fun fetchImg(route: String): ImageBitmap? {
        val res = withContext(Dispatchers.IO) {
            send(HttpReq(addr + route), true)
        }
        if(res.bytes == null || res.code != 200) return null
        return try {
            BitmapFactory.decodeByteArray(res.bytes, 0, res.bytes.size).asImageBitmap()
        } catch(e: Exception) {
            Log.i("imageFetcher", e.message ?: "Unknown error")
            null
        }
    }

    suspend fun jsonReq(route: String, body: String = "", method: String = "GET"): HttpRes {
        return withContext(Dispatchers.IO) {
            val headers = if(token.isEmpty()) mapOf("content-type" to "application/json") else mapOf("content-type" to "application/json", "authorization" to "Bearer $token")
            println(headers)
            send(HttpReq(addr + route, body, method, headers))
        }
    }

    suspend fun login(username: String, password: String): String {
        val res = jsonReq("api/auth", """{"username": "$username", "password": "$password"}""", "POST")
        if(res.code != 200 || res.body.isNullOrEmpty()) return res.body ?: "Login Failed"
        token = res.body
        println(token)
        return "ok"
    }

    suspend fun register(username: String, fullName: String, email: String, password: String): String {
        val res = jsonReq("api/register", """{"username": "$username", "fullname": "$fullName", "email": "$email", "password": "$password"}""", "POST")
        if(res.code != 200 || res.body.isNullOrEmpty()) return "Registration Failed"
        if(res.code == 200) return "ok"
        return res.body
    }

    suspend fun me(): User? {
        val res = jsonReq("api/me")
        if(res.body.isNullOrEmpty() || res.code != 200) return null
        val obj = JSONObject(res.body)
        return User(
            obj.getInt("id"),
            obj.getString("username"),
            obj.getString("fullName"),
            obj.getString("email"),
        )

    }
}