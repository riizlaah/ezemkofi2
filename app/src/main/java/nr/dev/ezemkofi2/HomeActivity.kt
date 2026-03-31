package nr.dev.ezemkofi2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import nr.dev.ezemkofi2.ui.theme.Ezemkofi2Theme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ezemkofi2Theme {
                var user by remember { mutableStateOf<User?>(null) }

                LaunchedEffect(Unit) {
                    user = HttpClient.me()
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
                        if(user == null) return@Column
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Good Morning")
                                Text(
                                    user!!.fullName,
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                )
                            }
                            IconButton({}) {
                                Icon(
                                    painterResource(R.drawable.shopping_bag_regular_24),
                                    contentDescription = "Cart",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        LazyColumn(Modifier
                            .weight(1f)
                            .padding(24.dp)) {
                            item {
                                Box(
                                    Modifier
                                        .padding(vertical = 20.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(45))
                                        .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(45))
                                        .padding(12.dp)
                                ) {
                                    Text("Find your perfect coffee", color = MaterialTheme.colorScheme.secondary)
                                    Icon(painterResource(R.drawable.search_alt_2_regular_24), contentDescription = "Search", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.align(Alignment.CenterEnd).size(24.dp))
                                }
                            }
                            item {

                            }
                        }
                    }
                }
            }
        }
    }
}