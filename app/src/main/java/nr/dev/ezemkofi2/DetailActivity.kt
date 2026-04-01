package nr.dev.ezemkofi2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nr.dev.ezemkofi2.ui.theme.Ezemkofi2Theme
import nr.dev.ezemkofi2.ui.theme.poppins
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra("id", 0)
        enableEdgeToEdge()
        setContent {
            Ezemkofi2Theme {
                var coffee by remember { mutableStateOf<Coffee?>(null) }
                var qty by remember { mutableIntStateOf(1) }
                val options =
                    listOf(CoffeeSize("S", 0.85), CoffeeSize("M", 1.0), CoffeeSize("L", 1.15))
                var selectedOpt by remember { mutableIntStateOf(1) }
                val radius = 300.dp
                val imgSize by animateDpAsState(
                    radius * options[selectedOpt].scale.toFloat(),
                    tween(500)
                )
                var targetRot by remember { mutableFloatStateOf(0f) }
                val imgRot by animateFloatAsState(targetRot, tween(500))
                val ctx = LocalContext.current

                LaunchedEffect(Unit) {
                    coffee = HttpClient.getCoffeeById(id)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(innerPadding)
                            .background(Color.White),
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight(0.4f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(Modifier
                                .fillMaxWidth()
                                .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painterResource(R.drawable.chevron_left_regular_24),
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xff37765D))
                                        .clickable(onClick = {
                                            val intent = Intent(ctx, HomeActivity::class.java)
                                            ctx.startActivity(intent)
                                            finish()
                                        })
                                )
                                Text(
                                    "Details",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            }
                            if (coffee == null) return@Box
                            LazyColumn(Modifier.weight(1f)) {
                                item {

                                    Box(Modifier
                                        .fillMaxWidth()
                                        .height(radius * 1.55f)) {
                                        NetworkImage(
                                            "images/${coffee!!.imagePath}",
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .rotate(imgRot)
                                                .size(imgSize)
                                        )
                                        Box(
                                            Modifier
                                                .align(Alignment.CenterEnd)
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xfff9853a)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(coffee!!.rating.toString(), color = Color.White)
                                        }
                                        options.forEachIndexed { i, size ->
                                            val angle =
                                                (230f + 80f * (i.toFloat() / (options.size - 1))).toDouble()

                                            val x = (radius * 0.65f) * cos(Math.toRadians(angle)).toFloat()
                                            val y = -(radius * 0.65f) * sin(Math.toRadians(angle)).toFloat()
                                            val rot = 45f + (i * -45f)

                                            Button(
                                                {
                                                    if(selectedOpt != i) targetRot += 360f
                                                    selectedOpt = i
                                                },
                                                modifier = Modifier
                                                    .offset(x, y)
                                                    .align(Alignment.Center)
                                                    .size(40.dp)
                                                    .rotate(rot),
                                                shape = CircleShape,
                                                contentPadding = PaddingValues(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    if (selectedOpt == i) MaterialTheme.colorScheme.primary else Color.White,
                                                    if (selectedOpt == i) Color.White else MaterialTheme.colorScheme.primary
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text(size.name)
                                            }
                                        }
                                    }
                                }
                                item {
                                    Text(
                                        coffee!!.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                                    )
                                    Text(coffee!!.description, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(Modifier.height(24.dp))
                                }
                                item {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "$%.2f".format(coffee!!.price * options[selectedOpt].scale * qty),
                                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
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
                                                    .clickable(onClick = { qty = max(1, qty - 1) }),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(qty.toString(), fontWeight = FontWeight.Medium)
                                            Icon(
                                                painterResource(R.drawable.plus_regular_24),
                                                contentDescription = "Increase",
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clickable(onClick = { qty += 1 }),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                                item {
                                    Button(
                                        {
                                            HttpClient.addToCart(coffee!!, options[selectedOpt].name, qty)
                                            val intent = Intent(ctx, CartActivity::class.java)
                                            ctx.startActivity(intent)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(16.dp)
                                    ) {
                                        Text("ADD TO CART", fontFamily = poppins)
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