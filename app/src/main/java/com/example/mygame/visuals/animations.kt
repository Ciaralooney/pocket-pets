package com.example.sportsteam.visuals

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mygame.R


@Composable
fun WalkingAnimation(petImageId: Int) {
    val xPosition = remember { Animatable(0f) }
    val yPosition = remember { Animatable(0f) }

    val jumpHeight = 10f
    val moveSpeed = 1000

    LaunchedEffect(Unit) {
        while (true) {
            xPosition.animateTo(
                targetValue = (-150..150).random().toFloat(),
                animationSpec = tween(durationMillis = moveSpeed)
            )

            // jumping
            yPosition.animateTo(
                targetValue = -jumpHeight,
                animationSpec = tween(durationMillis = 300, delayMillis = 300)
            )
            yPosition.animateTo(
                targetValue = 4f,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }
    Box(
        modifier = Modifier
            .offset(x = xPosition.value.dp, y = yPosition.value.dp)
            .size(400.dp)
    ) {
        Image(
            painter = painterResource(id = petImageId),
            contentDescription = "Animated Pet",
            modifier = Modifier.fillMaxSize()
                .padding(top = 230.dp)
                .size(200.dp)
        )
    }
}

@Composable
fun LeapingAnimation(petImageId: Int) {
    val xPosition = remember { Animatable(0f) }
    val yPosition = remember { Animatable(0f) }

    val jumpHeight = 60f

    LaunchedEffect(Unit) {
        while (true) {
            yPosition.animateTo(
                targetValue = -jumpHeight,
                animationSpec = tween(durationMillis = 300, delayMillis = 300)
            )
            yPosition.animateTo(
                targetValue = 4f,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }
    Box(
        modifier = Modifier
            .offset(x = xPosition.value.dp, y = yPosition.value.dp)
            .size(800.dp)
            .padding(bottom = 50.dp)
    ) {
        Image(
            painter = painterResource(id = petImageId),
            contentDescription = "Jumping Pet",
            modifier = Modifier
                .size(175.dp)
                .align(Alignment.BottomCenter)

        )
    }

}

