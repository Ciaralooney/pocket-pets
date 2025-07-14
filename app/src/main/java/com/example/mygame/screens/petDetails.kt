package com.example.mygame.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.mygame.R
import com.example.mygame.data.Pet

@Composable
fun PetDetailScreen(
    selectedPet: Pet?,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFADD8E6))
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            if (selectedPet != null) {
                Text(
                    text = selectedPet.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(4.dp, Color(0xFFF4A6C2), RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = petImageId),
                        contentDescription = "image of ${selectedPet.name}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }


                Spacer(modifier = Modifier.height(32.dp))

                PetDetailCard(label = "Age", value = selectedPet.age.toString())
                PetDetailCard(label = "Current Happiness", value = "${selectedPet.happyMeter}/10")
                PetDetailCard(label = "Current Hunger", value = "${selectedPet.hungerMeter}/10")

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(Color(0xFFF4A6C2))
                ) {
                    Text(text = "Back", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                Text(
                    text = "Database Error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PetDetailCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
