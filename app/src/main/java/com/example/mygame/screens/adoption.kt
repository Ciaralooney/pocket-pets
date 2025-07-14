package com.example.mygame.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mygame.R
import com.example.mygame.data.Pet
import com.example.mygame.data.PetEvent
import com.example.mygame.data.PetViewModel


@Composable
fun AdoptionScreen(
    viewModel: PetViewModel,
    onAddToFavorites: (Pet) -> Unit,
    onPetClick: (Pet) -> Unit,
    onAdoptPet: (Pet) -> Unit
) {
    val availablePets = viewModel.getPetsByAdoptionType(false).collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFADD8E6))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availablePets.forEach { pet ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onPetClick(pet) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val petImageId = when (pet.image) {
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
                Image(
                    painter = painterResource(id = petImageId),
                    contentDescription = "image of $pet",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = "Name: ${pet.name}")
                    Text(text = "Age: ${pet.age}")
                    FloatingActionButton(
                        onClick = {
                            viewModel.onEvent(PetEvent.AdoptPet(pet))
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth(0.4f),
                        containerColor = Color(0xFFFFC107),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "Adopt",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
