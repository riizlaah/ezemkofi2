package nr.dev.ezemkofi2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nr.dev.ezemkofi2.ui.theme.Ezemkofi2Theme
import nr.dev.ezemkofi2.ui.theme.poppins
import kotlin.math.max

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ezemkofi2Theme {
                val scope = rememberCoroutineScope()
                val ctx = LocalContext.current
                val sizes = mapOf(
                    "S" to 0.85,
                    "M" to 1.0,
                    "L" to 1.15
                )

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(innerPadding)
                            .background(Color.White)
                            .padding(16.dp),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(R.drawable.chevron_left_regular_24),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable(onClick = {
                                        val intent = Intent(ctx, HomeActivity::class.java)
                                        ctx.startActivity(intent)
                                        finish()
                                    })
                            )
                            Text(
                                "Your Cart",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        }
                        LazyColumn(
                            Modifier
                                .weight(1f)
                                .padding(bottom = 12.dp)
                        ) {
                            items(HttpClient.carts) { cart ->
                                val item = cart.coffee
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
                                    NetworkImage(
                                        "images/${item.imagePath}",
                                        modifier = Modifier
                                            .padding(end = 16.dp)
                                            .weight(1f)
                                            .height(118.dp)
                                    )
                                    Column(Modifier.weight(2.5f)) {
                                        Row(Modifier.fillMaxWidth()) {
                                            Column(Modifier.weight(1f)) {
                                                Text(item.name, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    item.category,
                                                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            Icon(
                                                painterResource(R.drawable.delete_outline),
                                                contentDescription = "Delete",
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clickable(onClick = {
                                                        HttpClient.removeFromCart(item.id, cart.size)
                                                    }),
                                                tint = MaterialTheme.colorScheme.tertiary
                                            )
                                        }

                                        Spacer(Modifier.height(12.dp))
                                        Text("Size: ${cart.size}")
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                Modifier
                                                    .padding(8.dp)
                                                    .shadow(2.dp, CircleShape)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    painterResource(R.drawable.minus_regular_24),
                                                    contentDescription = "Decrease",
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clickable(onClick = {
                                                            HttpClient.updateCartQty(item.id, cart.size, max(1, cart.qty - 1))
                                                        }),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(cart.qty.toString(), fontWeight = FontWeight.Medium)
                                                Icon(
                                                    painterResource(R.drawable.plus_regular_24),
                                                    contentDescription = "Increase",
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clickable(onClick = {HttpClient.updateCartQty(item.id, cart.size, cart.qty + 1)}),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Text(
                                                "$%.2f".format(item.price * sizes[cart.size]!!),
                                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (HttpClient.carts.isEmpty()) return@Column
                        Column(Modifier.fillMaxWidth()) {
                            val total = HttpClient.carts.sumOf { it.coffee.price * it.qty * sizes[it.size]!! }
                            Text("Total : $%.2f".format(total), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                            Button({
                                scope.launch {
                                    HttpClient.checkout()
                                }
                            }, contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Text("CHECKOUT", fontFamily = poppins)
                            }
                        }
                    }
                }
            }
        }
    }
}