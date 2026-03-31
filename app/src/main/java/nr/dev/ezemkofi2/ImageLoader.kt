package nr.dev.ezemkofi2

import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale

class ImageLoader {
    val caches = mutableMapOf<String, ImageBitmap>()

    suspend fun loadImg(url: String): ImageBitmap? {
        caches[url]?.let { return it }
        return try {
            val img = HttpClient.fetchImg(url)
            if (img != null) caches[url] = img
            img
        } catch (e: Exception) {
            Log.e("imgLoader", e.message ?: "Unknown error")
            null
        }
    }

    fun hasCache(url: String): Boolean {
        return caches.contains(url)
    }
}


@Composable
fun NetworkImage(url: String, modifier: Modifier = Modifier, contentDescription: String = "", contentScale: ContentScale = ContentScale.Fit) {
    val imgLoader = remember { ImageLoader() }
    var isError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var img by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        isError = false
        if (!imgLoader.hasCache(url)) loading = true
        img = imgLoader.loadImg(url)
        loading = false
        if(img == null) isError = true
    }

    when {
        loading -> {
            val transition = rememberInfiniteTransition()
            val bg by transition.animateColor(
                Color.White,
                Color.LightGray,
                infiniteRepeatable(
                    tween(500),
                    RepeatMode.Reverse)
            )

            Box(Modifier.background(bg).fillMaxSize())
        }
        isError -> {
            Box(Modifier.fillMaxSize()) {
                Text("Image Failed to load", modifier = Modifier.align(Alignment.Center))
            }
        }
        img != null -> {
            Image(bitmap = img!!, contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
        }
    }

}