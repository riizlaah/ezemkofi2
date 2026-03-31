package nr.dev.ezemkofi2

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nr.dev.ezemkofi2.ui.theme.Ezemkofi2Theme
import nr.dev.ezemkofi2.ui.theme.poppins

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ezemkofi2Theme {
                var username by remember { mutableStateOf("") }
                val pass = remember { TextFieldState() }
                var errMsg by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()
                var loading by remember { mutableStateOf(false) }
                val ctx = LocalContext.current
                LaunchedEffect(Unit) {
                    HttpClient.sharedPrefs = ctx.getSharedPreferences("app_prefs", MODE_PRIVATE)
                    HttpClient.loadToken()
                    if(HttpClient.me() != null) {
                        val intent = Intent(ctx, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        ctx.startActivity(intent)
                    }
                }

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
                            modifier = Modifier.fillMaxWidth().padding(32.dp)
                        )
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Login",
                                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Login with your account to continue", color = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.height(32.dp))
                            SimpleInput(username, {username = it}, Modifier.fillMaxWidth(), "Username")
                            PasswordInput(pass, Modifier.fillMaxWidth())
                            if(errMsg.isNotEmpty()) {
                                Text(errMsg, color = Color.Red, modifier = Modifier.fillMaxWidth().padding(12.dp))
                            }
                            Spacer(Modifier.height(24.dp))
                            Button({
                                if(username.isEmpty()) {
                                    errMsg = "Username is required"
                                    return@Button
                                }
                                if(pass.text.isEmpty()) {
                                    errMsg = "Password is required"
                                    return@Button
                                }
                                errMsg = ""
                                scope.launch {
                                    loading = true
                                    when(val msg = HttpClient.login(username, pass.text.toString())) {
                                        "ok" -> {
                                            val intent = Intent(ctx, HomeActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                                Text("LOGIN", fontFamily = poppins)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Don't have an account?", color = MaterialTheme.colorScheme.secondary)
                                TextButton({
                                    val intent = Intent(ctx, RegisterActivity::class.java)
                                    ctx.startActivity(intent)
                                }) {
                                    Text("Create an account!", fontWeight = FontWeight.SemiBold, fontFamily = poppins)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleInput(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, label: String = "Username") {
    Text(label, color = MaterialTheme.colorScheme.secondary)
    BasicTextField(
        value,
        onChange,
        modifier = modifier.padding(bottom = 12.dp).background(MaterialTheme.colorScheme.tertiary).padding(bottom = 1.dp).background(Color.White).padding(12.dp, 8.dp),
    )
}

@Composable
fun PasswordInput(state: TextFieldState, modifier: Modifier = Modifier, label: String = "Password") {
    Text(label, color = MaterialTheme.colorScheme.secondary)
    BasicSecureTextField(
        state,
        modifier = modifier.padding(bottom = 12.dp).background(MaterialTheme.colorScheme.tertiary).padding(bottom = 1.dp).background(Color.White).padding(12.dp, 8.dp),
    )
}

@Composable
fun LoadingCircle(isLoading: Boolean, size: Dp = 24.dp, color: Color = Color.White) {
    if(isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(size), color = color)
    }
}
