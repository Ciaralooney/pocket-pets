package com.example.mygame.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mygame.R
import com.example.mygame.data.Pet
import com.example.sportsteam.visuals.LeapingAnimation
import kotlinx.coroutines.delay

@Composable
fun PoolScreen(
    navController: NavController,
    selectedPet: Pet?
) {

    LaunchedEffect(Unit) {
        delay(5000) // 5 seconds
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
            painter = painterResource(id = R.drawable.wash),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LeapingAnimation(petImageId)

        }
    }
}
