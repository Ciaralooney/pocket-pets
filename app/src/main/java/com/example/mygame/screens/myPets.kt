package com.example.mygame.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
fun MyPetsScreen(
    viewModel: PetViewModel,
    onEvent: (PetEvent) -> Unit,
    onPetClick: (Pet) -> Unit,
    onRemoveFromFavorites: (Pet) -> Unit,
) {
    val adoptedPets = viewModel.getPetsByAdoptionType(true).collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFADD8E6))
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (adoptedPets.isEmpty()) {
            Text(
                text = "No pets yet",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 10.dp)

            )
            Text(
                text = "Go adopt one!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 30.dp)

            )
            Image(
                painter = painterResource(id = R.drawable.hoops),
                contentDescription = "no pets",
                modifier = Modifier
                    .size(250.dp)
                    .padding(top = 100.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {

            }
        } else {
            LazyColumn {
                items(adoptedPets) { pet ->
                    Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPetClick(pet) }
                            .padding(8.dp),
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

                        Text(text = "${pet.name}, ", modifier = Modifier.weight(1f))

                        FloatingActionButton(
                            onClick = { onEvent(PetEvent.ReleasePet(pet)) },

                            modifier = Modifier
                                .height(50.dp)
                                .fillMaxWidth(0.4f),

                            containerColor = Color(0xFFF4A6C2),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "Release",
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
