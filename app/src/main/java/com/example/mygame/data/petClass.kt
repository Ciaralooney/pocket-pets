package com.example.mygame.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity // room annotation
data class Pet(
    @PrimaryKey(autoGenerate = true) // auto generate id
    val id: Int= 0, // unique id for each entry
    val name: String = "",
    val age: Int = 1,
    var hungerMeter: Int = 5,
    var happyMeter: Int = 5,
    val image: String = "",
    var isAdopted: Boolean = false,
    val isAddingPet: Boolean = false,
    val sortType: SortType = SortType.NAME
)
