package nr.dev.ezemkofi2

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nr.dev.ezemkofi2.ui.theme.Ezemkofi2Theme
import nr.dev.ezemkofi2.ui.theme.poppins

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ezemkofi2Theme {
                var user by remember { mutableStateOf<User?>(null) }
                val categories = remember { mutableStateListOf<Category>() }
                val coffees = remember { mutableStateListOf<Coffee>() }
                val topPickCoffees = remember { mutableStateListOf<Coffee>() }
                var currentCategory by remember { mutableIntStateOf(0) }
                val ctx = LocalContext.current

                LaunchedEffect(Unit) {
                    user = HttpClient.me()
                    if (categories.isEmpty()) {
                        HttpClient.loadCart()
                        categories.addAll(HttpClient.getCategories())
                        currentCategory = categories[0].id
                        topPickCoffees.addAll(HttpClient.getTopCoffees())
                    }
                }

                LaunchedEffect(currentCategory) {
                    coffees.clear()
                    coffees.addAll(HttpClient.getCoffees("", currentCategory))
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
                        if (user == null) return@Column
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton({
                                HttpClient.token = ""
                                HttpClient.saveToken()
                                val intent = Intent(ctx, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                ctx.startActivity(intent)
                            }) {
                                Icon(
                                    painterResource(R.drawable.logout),
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(24.dp).rotate(180f)
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Good Morning")
                                Text(
                                    user!!.fullName,
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton({
                                val intent = Intent(ctx, CartActivity::class.java)
                                ctx.startActivity(intent)
                            }) {
                                Icon(
                                    painterResource(R.drawable.shopping_bag_regular_24),
                                    contentDescription = "Cart",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        LazyColumn(
                            Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp, horizontal = 24.dp)
                        ) {
                            item {
                                Box(
                                    Modifier
                                        .padding(vertical = 20.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(45))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.secondary,
                                            RoundedCornerShape(45)
                                        )
                                        .padding(12.dp)
                                        .clickable(onClick = {
                                            val intent = Intent(ctx, SearchActivity::class.java)
                                            startActivity(intent)
                                        })
                                ) {
                                    Text(
                                        "Find your perfect coffee",
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Icon(
                                        painterResource(R.drawable.search_alt_2_regular_24),
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(24.dp)
                                    )
                                }
                            }
                            item {
                                Text("Categories", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
                                LazyRow(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(categories) { item ->
                                        Button(
                                            {
                                                currentCategory = item.id
                                            }, colors = ButtonDefaults.buttonColors(
                                                if (item.id == currentCategory) MaterialTheme.colorScheme.primary else Color(0xffc8c8c8),
                                                if (item.id == currentCategory) Color.White else Color.Black,
                                            )
                                        ) {
                                            Text(item.name, fontFamily = poppins)
                                        }
                                    }
                                }
                            }
                            item {
                                LazyRow(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(coffees) { item ->
                                        Column(
                                            Modifier
                                                .width(212.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                                .drawBehind {
                                                    val offset = Offset(0f, size.height * 0.35f)
                                                    drawRoundRect(
                                                        color = Color(0xff156545), offset,
                                                        cornerRadius = CornerRadius(24f, 24f)
                                                    )
                                                }
                                                .padding(16.dp, 24.dp)
                                                .clickable(onClick = {
                                                    val intent = Intent(ctx, DetailActivity::class.java)
                                                    intent.putExtra("id", item.id)
                                                    ctx.startActivity(intent)
                                                })
                                        ) {
                                            Box(Modifier.width(192.dp)) {
                                                NetworkImage(
                                                    "images/${item.imagePath}",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            Text(
                                                item.name,
                                                modifier = Modifier.fillMaxWidth(),
                                                softWrap = false,
                                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                                fontWeight = FontWeight.Bold,
                                                overflow = TextOverflow.Ellipsis,
                                                color = Color.White
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Row(
                                                Modifier
                                                    .clip(RoundedCornerShape(45))
                                                    .background(Color(0xff37765D))
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
                                                Text(
                                                    "%.1f".format(item.rating),
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                "$%.2f".format(item.price),
                                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                Text("Top Picks", fontWeight = FontWeight.SemiBold)
                            }
                            items(topPickCoffees) { item ->
                                Row(
                                    Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth()
                                        .clickable(onClick = {
                                            val intent = Intent(ctx, DetailActivity::class.java)
                                            intent.putExtra("id", item.id)
                                            ctx.startActivity(intent)
                                        }),
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
                                    Column(Modifier.weight(2.35f)) {
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