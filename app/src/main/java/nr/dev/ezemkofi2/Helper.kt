package nr.dev.ezemkofi2

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

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

data class Category(
    val id: Int,
    val name: String
)

data class Coffee(
    val id: Int,
    val name: String,
    val category: String,
    val rating: Double,
    val price: Double,
    val imagePath: String,
    val description: String = ""
)

data class CoffeeSize(
    val name: String,
    val scale: Double
)

data class Cart(
    val coffeeId: Int,
    val coffee: Coffee,
    val size: String,
    val qty: Int
)

data class Transaction(
    val id: Int
)

object HttpClient {
    val addr = "http://10.0.2.2:5000/"

    var token: String = ""

    lateinit var sharedPrefs: SharedPreferences

    var user by mutableStateOf<User?>(null)

    val carts = mutableStateListOf<Cart>()

    fun loadToken() {
        token = sharedPrefs.getString("token", "") ?: ""
    }

    fun saveToken() {
        sharedPrefs.edit {
            putString("token", token)
        }
    }

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

    suspend fun fetchImg(path: String): ImageBitmap? {
        val res = withContext(Dispatchers.IO) {
            send(HttpReq(addr + path), true)
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
        saveToken()
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

    // coffees
    suspend fun getCoffees(search: String = "", categoryId: Int = 0): List<Coffee> {
        var url = "api/coffee"
        if(search.isNotBlank()) {
            url += "?search=" + URLEncoder.encode(search, "UTF-8")
            if(categoryId > 0) {
                url += "&coffeeCategoryID=$categoryId"
            }
        } else if(categoryId > 0) {
            url += "?coffeeCategoryID=$categoryId"
        }
        val res = jsonReq(url)
        if(res.body.isNullOrEmpty() || res.code != 200) return emptyList()
        val arr = mutableListOf<Coffee>()
        val json = JSONArray(res.body)
        for(i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            arr.add(Coffee(
                obj.getInt("id"),
                obj.getString("name"),
                obj.getString("category"),
                obj.getDouble("rating"),
                obj.getDouble("price"),
                obj.getString("imagePath"),
            ))
        }
        return arr
    }

    suspend fun getTopCoffees(): List<Coffee> {
        val res = jsonReq("api/coffee/top-picks")
        if(res.body.isNullOrEmpty() || res.code != 200) return emptyList()
        val arr = mutableListOf<Coffee>()
        val json = JSONArray(res.body)
        for(i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            arr.add(Coffee(
                obj.getInt("id"),
                obj.getString("name"),
                obj.getString("category"),
                obj.getDouble("rating"),
                obj.getDouble("price"),
                obj.getString("imagePath")
            ))
        }
        return arr
    }

    suspend fun getCategories(): List<Category> {
        val res = jsonReq("api/coffee-category")
        if(res.body.isNullOrEmpty() || res.code != 200) return emptyList()
        val arr = mutableListOf<Category>()
        val json = JSONArray(res.body)
        for(i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            arr.add(Category(
                obj.getInt("id"),
                obj.getString("name")
            ))
        }
        return arr
    }

    suspend fun getCoffeeById(id: Int): Coffee? {
        val res = jsonReq("api/coffee/$id")
        if(res.body.isNullOrEmpty() || res.code != 200) return null
        val obj = JSONObject(res.body)
        return Coffee(
            obj.getInt("id"),
            obj.getString("name"),
            obj.getString("category"),
            obj.getDouble("rating"),
            obj.getDouble("price"),
            obj.getString("imagePath"),
            obj.getString("description")
        )
    }

    fun addToCart(coffee: Coffee, size: String, qty: Int) {
        val idx = carts.indexOfFirst { it.coffeeId == coffee.id && it.size == size }
        if(idx > -1) {
            carts[idx] = carts[idx].copy(qty = carts[idx].qty + qty)
            return
        }
        carts.add(Cart(coffee.id, coffee, size, qty))
        saveCart()
    }

    fun removeFromCart(coffeeId: Int, size: String) {
        carts.removeIf { it.coffeeId == coffeeId && it.size == size }
        saveCart()
    }

    fun updateCartQty(coffeeId: Int, size: String, qty: Int) {
        val idx = carts.indexOfFirst { it.coffeeId == coffeeId && it.size == size }
        if(idx > -1) carts[idx] = carts[idx].copy(qty = qty)
        saveCart()
    }

    fun saveCart() {
        sharedPrefs.edit {
            var data = "["
            carts.forEach {
                val coff = it.coffee
                data += """{"coffeeId": ${it.coffeeId}, "coffee": {"id": ${coff.id}, "name": "${coff.name}", "category": "${coff.category}", "rating": ${coff.rating}, "price": ${coff.price}, "imagePath": "${coff.imagePath}"}, "size": "${it.size}", "qty": ${it.qty}},"""
            }
            data = data.trimEnd(',')
            data += "]"
            putString("carts", data)
        }
    }

    fun loadCart() {
        val json = sharedPrefs.getString("carts", "") ?: ""
        if(json.isBlank()) return
        val arr = JSONArray(json)
        for(i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val coffObj = obj.getJSONObject("coffee")
            val coff = Coffee(
                coffObj.getInt("id"),
                coffObj.getString("name"),
                coffObj.getString("category"),
                coffObj.getDouble("rating"),
                coffObj.getDouble("price"),
                coffObj.getString("imagePath")
            )
            carts.add(Cart(
                coff.id,
                coff,
                obj.getString("size"),
                obj.getInt("qty")
            ))
        }
    }

    suspend fun checkout() {
        var body = "["
        carts.forEach {
            body += """{"coffeeId": ${it.coffeeId}, "size": "${it.size}", "qty": ${it.qty}},"""
        }
        body = body.trimEnd(',')
        val res = jsonReq("api/checkout", body, "POST")
        carts.clear()
        saveCart()
        println(res)
    }
}