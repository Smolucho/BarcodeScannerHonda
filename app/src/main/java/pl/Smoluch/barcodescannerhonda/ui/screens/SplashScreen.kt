package pl.Smoluch.barcodescannerhonda.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.Smoluch.barcodescannerhonda.R

@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Honda Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Zrobione przez Smolucha",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
} 