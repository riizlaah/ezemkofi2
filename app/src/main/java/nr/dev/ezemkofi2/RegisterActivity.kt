package nr.dev.ezemkofi2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nr.dev.ezemkofi2.ui.theme.Ezemkofi2Theme
import nr.dev.ezemkofi2.ui.theme.poppins

class RegisterActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ezemkofi2Theme {
                var username by remember { mutableStateOf("") }
                var fullName by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                val pass = remember { TextFieldState() }
                val pass2 = remember { TextFieldState() }
                var errMsg by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()
                var loading by remember { mutableStateOf(false) }
                val ctx = LocalContext.current


                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(innerPadding)
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painterResource(R.drawable.logo_green_with_icon),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Create Account",
                                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Register yourself to become our member and enjoy all the benefits")
                            Spacer(Modifier.height(32.dp))
                            SimpleInput(username, {username = it}, Modifier.fillMaxWidth(), "Username")
                            SimpleInput(fullName, {fullName = it}, Modifier.fillMaxWidth(), "Full Name")
                            SimpleInput(email, {email = it}, Modifier.fillMaxWidth(), "Email")
                            PasswordInput(pass, Modifier.fillMaxWidth())
                            PasswordInput(pass2, Modifier.fillMaxWidth(), "Confirm Password")
                            if(errMsg.isNotEmpty()) {
                                Text(errMsg, color = Color.Red, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp))
                            }
                            Spacer(Modifier.height(24.dp))
                            Button({
                                if(username.isEmpty()) {
                                    errMsg = "Username is required"
                                    return@Button
                                }
                                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    errMsg = "Email not valid"
                                    return@Button
                                }
                                if(fullName.isEmpty()) {
                                    errMsg = "Full Name is required"
                                    return@Button
                                }
                                if(pass.text.length < 4) {
                                    errMsg = "Password length must be 4 characters or more"
                                    return@Button
                                }
                                if(pass.text != pass2.text) {
                                    errMsg = "Password confirmation must be same"
                                    return@Button
                                }
                                errMsg = ""
                                scope.launch {
                                    when(val msg = HttpClient.register(username, fullName, email, pass.text.toString())) {
                                        "ok" -> {
                                            loading = false
                                            val intent = Intent(ctx, MainActivity::class.java)
                                            ctx.startActivity(intent)
                                        }
                                        else -> {
                                            errMsg = msg
                                            loading = false
                                        }
                                    }
                                }
                            }, modifier = Modifier.fillMaxWidth()) {
                                if(loading) {
                                    LoadingCircle(loading)
                                    return@Button
                                }
                                Text("SIGN UP", fontFamily = poppins)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Already have an account?", color = MaterialTheme.colorScheme.secondary)
                                TextButton({
                                    val intent = Intent(ctx, MainActivity::class.java)
                                    ctx.startActivity(intent)
                                }) {
                                    Text("Login here", fontWeight = FontWeight.SemiBold, fontFamily = poppins)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}