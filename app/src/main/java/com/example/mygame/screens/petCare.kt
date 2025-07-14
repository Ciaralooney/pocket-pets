package com.example.mygame.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.example.mygame.services.proximitySensorHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mygame.R
import com.example.mygame.data.Pet
import com.example.mygame.data.PetEvent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.mygame.data.PetViewModel
import com.example.sportsteam.visuals.LeapingAnimation
import com.example.sportsteam.visuals.WalkingAnimation

@Composable
fun PetCareScreen(
    viewModel: PetViewModel,
    navController: NavController,
    selectedPet: Pet,
    onEvent: (PetEvent) -> Unit
) {


    val context = LocalContext.current
    val proximityState = remember { mutableStateOf(false) }

    val hungerMeter by viewModel.fetchHungerMeter(selectedPet.id).collectAsState()
    val happyMeter by viewModel.fetchHappyMeter(selectedPet.id).collectAsState(initial = selectedPet.happyMeter)

    proximitySensorHandler(
        context = context,
        onNear = {
            proximityState.value = true

        },
        onFar = {
            proximityState.value = false
        }
    )

    val petImageId = when (selectedPet.image) {
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

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(

            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

                Text(
                    text = "Happiness: ${happyMeter}/10",
                    style = MaterialTheme.typography.bodyLarge
                )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                    Text(text = "Hunger: $hungerMeter/10", style = MaterialTheme.typography.bodyLarge)

            }
            Spacer(modifier = Modifier.height(90.dp))

            WalkingAnimation(petImageId = petImageId)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
            Image(
                painter = painterResource(id = R.drawable.sponge),
                contentDescription = "sponge",
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        selectedPet.let { viewModel.updateHappyMeter(it, increment = true) }
                        navController.navigate("Pool")
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.cake),
                contentDescription = "cake",
                modifier = Modifier
                    .size(85.dp)
                    .clickable {
                        selectedPet.let { viewModel.updateHungerMeter(it, increment = true) }
                        navController.navigate("Kitchen")
                    }
            )
            Image(
                painter = painterResource(id = R.drawable.love),
                contentDescription = "love",
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        if (proximityState.value) {
                            selectedPet.let { viewModel.updateHappyMeter(it, increment = true)
                                navController.navigate("Love")
                            }
                        }
                    }
            )
    }
}
