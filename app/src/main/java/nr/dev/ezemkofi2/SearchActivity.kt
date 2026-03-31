package nr.dev.ezemkofi2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nr.dev.ezemkofi2.ui.theme.Ezemkofi2Theme

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ezemkofi2Theme {
                var search by remember { mutableStateOf("") }
                val coffees = remember { mutableStateListOf<Coffee>() }

                LaunchedEffect(Unit) {
                    coffees.addAll(HttpClient.getCoffees(search))
                }

                LaunchedEffect(search) {
                    delay(500)
                    coffees.clear()
                    coffees.addAll(HttpClient.getCoffees(search))
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(innerPadding)
                            .background(Color.White)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.chevron_left_regular_24),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .shadow(3.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable(onClick = {
                                        finish()
                                    })
                            )
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(
                                search,
                                { search = it },
                                modifier = Modifier.weight(1f),
                                decorationBox = { tf ->
                                    Row(
                                        Modifier
                                            .clip(CircleShape)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.secondary,
                                                CircleShape
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.weight(3f), contentAlignment = Alignment.CenterStart) {
                                            Text(
                                                "Find your perfect coffee",
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            tf()
                                        }
                                        Icon(painterResource(R.drawable.search_alt_2_regular_24), contentDescription = "Search", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                                    }
                                }
                            )
                        }
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Text("Search Result", fontWeight = FontWeight.SemiBold)
                            }
                            items(coffees) {item ->
                                Row(
                                    Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .padding(end = 16.dp)
                                            .weight(1f)
                                            .height(118.dp)
                                    ) {
                                        NetworkImage("images/${item.imagePath}")
                                        Row(
                                            Modifier
                                                .align(Alignment.BottomCenter)
                                                .clip(RoundedCornerShape(45))
                                                .background(MaterialTheme.colorScheme.primary)
                                                .padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painterResource(R.drawable.star_solid_24),
                                                contentDescription = "Star",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("%.1f".format(item.rating), color = Color.White)
                                        }
                                    }
                                    Column(Modifier.weight(2.5f)) {
                                        Text(item.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            item.category,
                                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(Modifier.height(24.dp))
                                        Text(
                                            "$%.2f".format(item.price),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}