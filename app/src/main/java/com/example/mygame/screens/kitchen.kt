package com.example.mygame.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter.State.Empty.painter
import com.example.mygame.R
import com.example.mygame.data.Pet
import com.example.sportsteam.visuals.LeapingAnimation
import kotlinx.coroutines.delay

@Composable
fun KitchenScreen(
    navController: NavController,
    selectedPet: Pet?) {

    var eatingAnimation by remember { mutableStateOf(true) }
    var showParticleEffect by remember { mutableStateOf(true) }

    var foodSize by remember { mutableStateOf(200.dp) }
    LaunchedEffect(Unit) {
        for (i in 1..4) {
            delay(1000)
            foodSize -= 20.dp
        }
        eatingAnimation = false
        showParticleEffect = false
        delay(200)
        navController.popBackStack()
    }

    val petImageId = when (selectedPet?.image) {
        "lovelitchi" -> R.drawable.lovelitchi
        "mimitchi" -> R.drawable.mimitchi
        "mametchi" -> R.drawable.mametchi
        "pianitchi" -> R.drawable.pianitchi
        "hapihapitchi" -> R.drawable.hapihapitchi
        "chamametchi" -> R.drawable.chamametchi
        "kikitchi" -> R.drawable.kikitchi
        "melodytchi" -> R.drawable.melodytchi
        "hoops" -> R.drawable.whoops
        "piddles" -> R.drawable.piddles
        "yoyo" -> R.drawable.yoyo
        else -> R.drawable.hoops
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.kitchen),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LeapingAnimation(petImageId)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(370.dp))
            Image(
                painter = painterResource(id = R.drawable.cake),
                contentDescription = "Cake Slice",
                modifier = Modifier
                    .size(foodSize)
                    .size(200.dp)
                    .padding(top = 130.dp)
                    .offset(x = 60.dp)
            )
        }
    }
}