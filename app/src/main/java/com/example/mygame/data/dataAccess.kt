package com.example.mygame.data

import android.app.Application
import android.content.Context
import android.nfc.Tag
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.room.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import androidx.work.WorkManager
import com.example.mygame.services.ReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.internal.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import perfetto.protos.UiState

@Dao
interface PetDao {

    @Upsert
    suspend fun upsertPet(pet: Pet)

    @Delete
    suspend fun deletePet(pet: Pet)

    @Query("SELECT happyMeter FROM pet WHERE id = :petId")
    fun getHappyMeter(petId: Int): Flow<Int>

    @Query("SELECT hungerMeter FROM pet WHERE id = :petId")
    fun getHungerMeter(petId: Int): Flow<Int>

    @Query("SELECT * FROM pet ORDER BY name ASC")
    fun getPetsOrderedByName(): Flow<List<Pet>>

    @Query("SELECT * FROM pet ORDER BY age ASC")
    fun getPetsOrderedByAge(): Flow<List<Pet>>

    @Query("SELECT * FROM pet WHERE isAdopted = :isAdopted")
    fun getAdoptedPets(isAdopted: Boolean): Flow<List<Pet>>

    @Query("UPDATE pet SET happyMeter = :happyMeter WHERE id = :petId")
    suspend fun updateHappyMeter(petId: Int, happyMeter: Int)

    @Query("UPDATE pet SET hungerMeter = :hungerMeter WHERE id = :petId")
    suspend fun updateHungerMeter(petId: Int, hungerMeter: Int)

    @Query("UPDATE pet SET isAdopted = :isAdopted WHERE id = :petId")
    suspend fun updateAdoptedStatus(petId: Int, isAdopted: Boolean)
}

// step 3- create database
@Database(entities = [Pet::class], version = 1)
abstract class PetDatabase : RoomDatabase() {
    abstract val dao: PetDao

    companion object {
        @Volatile
        private var INSTANCE: PetDatabase? = null

        fun getInstance(context: Context): PetDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PetDatabase::class.java,
                    "pets.db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}

// step 4- create events
enum class SortType {
    ADOPTED,
    NAME,
    AGE
}

sealed interface PetEvent {
    object SavePet: PetEvent
    data class SetName(val name: String): PetEvent
    data class ReleasePet(val pet: Pet) : PetEvent
    data class AdoptPet(val pet: Pet) : PetEvent
    data class SetAge(val age: String): PetEvent
    object ShowDialog: PetEvent
    object HideDialog: PetEvent
    data class SortPets(val sortType: SortType): PetEvent
    data class DeletePet(val pet: Pet): PetEvent
    data class UpdateHungerMeter(val pet: Pet, val increment: Boolean) : PetEvent
    data class UpdateHappyMeter(val pet: Pet, val increment: Boolean) : PetEvent
}

data class petState(
    val pets: List<Pet> = emptyList(),
    val name: String = "",
    val age: Int = 1,
    val hungerMeter: Int = 5,
    val happyMeter: Int = 5,
    val image: String = "",
    val id: Int = 0,
    val isAdopted: Boolean = false,
    val isAddingPet: Boolean = false,
    val sortType: SortType = SortType.ADOPTED
)

// step 5- create view model
class PetViewModel(private val dao: PetDao) : ViewModel() {

    private val _happyMeter = MutableStateFlow<Int>(0)
    val happyMeter = _happyMeter.asStateFlow()

    private val _hungerMeter = MutableStateFlow<Int>(0)
    val hungerMeter = _hungerMeter.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.NAME)

fun updateHappyMeter(pet: Pet, increment: Boolean) {
    logDatabaseContent()
    viewModelScope.launch {
        try {
            // Read current happy meter from database
            val currentHappyMeter = dao.getHappyMeter(pet.id).first()

            println("currentHappyMeter: $currentHappyMeter")
            val updatedMeter = currentHappyMeter + if (increment) 1 else -1
            println("updatedMeter: $updatedMeter")

            // max value is 10
            val clampedMeter = updatedMeter.coerceIn(0, 10)
            println("clampedMeter: $clampedMeter")

            // Update the database
            dao.updateHappyMeter(pet.id, clampedMeter)

            // Update the local state
            _happyMeter.value = clampedMeter

        } catch (e: Exception) {
            println("Error updating happy meter: ${e.message}")
        }
    }
}

fun updateHungerMeter(pet: Pet, increment: Boolean) {
    logDatabaseContent()
    viewModelScope.launch {
        try {
            // Read current hunger meter from database
            val currentHungerMeter = dao.getHungerMeter(pet.id).first()

            val updatedMeter = currentHungerMeter + if (increment) 1 else -1

            // max value is 10
            val clampedMeter = updatedMeter.coerceIn(0, 10)

            // Update the database
            dao.updateHungerMeter(pet.id, clampedMeter)

            // Update the local state
            _hungerMeter.value = clampedMeter

        } catch (e: Exception) {
            println("Error updating happy meter: ${e.message}")
        }
    }
}
    fun fetchHappyMeter(petId: Int): StateFlow<Int> {
        val initialValue = runBlocking {
            dao.getPetsOrderedByName().firstOrNull()?.find { it.id == petId }?.happyMeter ?: 0
        }
        return dao.getHappyMeter(petId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)
    }

    fun logDatabaseContent() {
        viewModelScope.launch {
            val pets = dao.getPetsOrderedByName().first()
            Log.d("DatabaseContent", "All Pets: $pets")
        }
    }

    fun fetchHungerMeter(petId: Int): StateFlow<Int> {

        val initialValue = runBlocking {
            dao.getPetsOrderedByName().firstOrNull()?.find { it.id == petId }?.hungerMeter ?: 0
        }
        return dao.getHungerMeter(petId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)
    }

    fun fetchPetById(id: Int): StateFlow<Pet?> {
        return dao.getPetsOrderedByName().map { pets ->
            pets.find { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _pets: StateFlow<List<Pet>> = _sortType.flatMapLatest { sortType ->
        when(sortType) {
            SortType.ADOPTED -> dao.getAdoptedPets(true)
            SortType.NAME -> dao.getPetsOrderedByName()
            SortType.AGE -> dao.getPetsOrderedByAge()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun getPetsByAdoptionType(isAdopted: Boolean): StateFlow<List<Pet>> {
        return _pets.map { pets ->
            pets.filter { it.isAdopted == isAdopted }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }
    fun getAdoptedPets(): StateFlow<List<Pet>> {
        return dao.getAdoptedPets(true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private val _state = MutableStateFlow(petState())

    private fun depletingMeters() {
        viewModelScope.launch {
            while (true) {
                try {
                    val adoptedPets = dao.getAdoptedPets(true).first()

                    for (pet in adoptedPets) {
                        val currentHungerMeter = pet.hungerMeter - 1
                        val currentHappyMeter = pet.happyMeter - 1

                        dao.updateHungerMeter(pet.id, currentHungerMeter.coerceIn(0, 10))
                        dao.updateHappyMeter(pet.id, currentHappyMeter.coerceIn(0, 10))
                    }

                } catch (e: Exception) {
                    println("Error updating meters: ${e.message}")
                }

                kotlinx.coroutines.delay(60_000) // 1 minute timer
            }
        }
    }

    val state = combine(
        _state,
        _sortType,
        _pets
    ) { state: petState, sortType: SortType, pets: List<Pet> ->
        state.copy(
            pets = pets,
            sortType = sortType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), petState())

    fun onEvent(event: PetEvent) {
        when(event){
            is PetEvent.AdoptPet -> {
                viewModelScope.launch {
                    dao.updateAdoptedStatus(event.pet.id, true)
                }
            }
            is PetEvent.ReleasePet -> {
                viewModelScope.launch {
                    dao.updateAdoptedStatus(event.pet.id, false)
                }
            }
            is PetEvent.DeletePet -> {
                viewModelScope.launch {
                    dao.deletePet(event.pet)
                }
            }
            PetEvent.HideDialog -> {
                _state.update { it.copy(
                    isAddingPet = false
                ) }
            }

            is PetEvent.UpdateHungerMeter -> {
                val updatedMeter = event.pet.hungerMeter + if (event.increment) 1 else -1
                _hungerMeter.value = updatedMeter.coerceIn(0, 10)
            }
            is PetEvent.UpdateHappyMeter -> {
                val updatedMeter = event.pet.happyMeter + if (event.increment) 1 else -1
                _happyMeter.value = updatedMeter.coerceIn(0, 10)
            }

            PetEvent.SavePet -> {
                val name = state.value.name
                val age = state.value.age
                val hungerMeter = state.value.hungerMeter
                val happyMeter = state.value.happyMeter
                val id = state.value.id
                val image = state.value.image
                val isAdopted = state.value.isAdopted

                if(name.isBlank()) {
                    return
                }
                val pet = Pet(
                    name = name,
                    age = age,
                    id = id,
                    image = image,
                    isAdopted = isAdopted,
                    happyMeter = happyMeter,
                    hungerMeter = hungerMeter,
                )
                viewModelScope.launch {
                    dao.upsertPet(pet)
                }
                _state.update { it.copy(
                    isAddingPet = false,
                    name = "",
                    age = 0)
                }

            }
            is PetEvent.SetName -> {
                _state.update { it.copy(
                    name = event.name
                ) }
            }
            is PetEvent.SetAge -> {
                _state.update { it.copy(
                    age = event.age.toInt()
                ) }
            }
            PetEvent.ShowDialog -> {
                _state.update { it.copy(
                    isAddingPet = true
                ) }
            }
            is PetEvent.SortPets -> {
                _sortType.value = event.sortType
            }
        }
    }

    init {
        populateDatabase()
        logDatabaseContent()
        depletingMeters()
    }

    private fun populateDatabase() {
        viewModelScope.launch {
            if (dao.getPetsOrderedByName().first().isEmpty()) { // Adding pets if db is empty
                val defaultPets = listOf(
                    Pet(name = "Mametchi", age = 2, hungerMeter = 5, happyMeter = 5,
                        image = "mametchi", isAdopted = false),

                    Pet(name = "Melodytchi", age = 3, hungerMeter = 4, happyMeter = 4,
                        image = "melodytchi", isAdopted = false),

                    Pet(name = "Mimitchi", age = 1, hungerMeter = 7, happyMeter = 8,
                        image = "mimitchi", isAdopted = false),

                    Pet(name = "Lovelitchi", age = 4, hungerMeter = 6, happyMeter = 6,
                        image = "lovelitchi", isAdopted = false),

                    Pet(name = "Pianitchi", age = 6, hungerMeter = 4, happyMeter = 5,
                        image = "pianitchi", isAdopted = false),

                    Pet(name = "Kikitchi ", age = 5, hungerMeter = 6, happyMeter = 3,
                        image = "kikitchi", isAdopted = false),

                    Pet(name = "Chamametchi", age = 3, hungerMeter = 8, happyMeter = 9,
                        image = "chamametchi", isAdopted = false),

                    Pet(name = "Hapihapitchi", age = 4, hungerMeter = 2, happyMeter = 7,
                        image = "hapihapitchi", isAdopted = false),

                    Pet(name = "Hoops", age = 20, hungerMeter = 7, happyMeter = 8,
                        image = "hoops", isAdopted = false),

                    Pet(name = "Yoyo", age = 20, hungerMeter = 7, happyMeter = 8,
                        image = "yoyo", isAdopted = false),

                    Pet(name = "Piddles", age = 21, hungerMeter = 4, happyMeter = 4,
                        image = "piddles", isAdopted = false)
                )

                defaultPets.forEach { pet ->
                    dao.upsertPet(pet)
                }
            }
        }
    }

}

@Composable
fun PetScreen(
    state: petState,
    onEvent: (PetEvent) -> Unit){

    Scaffold(
        floatingActionButton={
            FloatingActionButton(onClick = {
                onEvent(PetEvent.ShowDialog)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add pet")
            }
        },
        modifier= Modifier.padding(16.dp)
    ){padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ){ item{
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically){
                SortType.entries.forEach { sortType ->
                    Row(modifier = Modifier.clickable {
                        onEvent(PetEvent.SortPets(sortType))
                    },
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = state.sortType == sortType, onClick = {
                            onEvent(PetEvent.SortPets(sortType))
                        })
                        Text(text = sortType.name)
                    }
                }
            }
        }
            val petsToShow = if (state.sortType == SortType.ADOPTED) {
                state.pets.filter { it.isAdopted }
            } else {
                state.pets
            }

            items(petsToShow) { pet ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${pet.name}, ", fontSize = 20.sp)
                        Text(text = pet.age.toString(), fontSize = 15.sp)
                    }
                    IconButton(onClick = {
                        onEvent(PetEvent.DeletePet(pet))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete pet"
                        )
                    }
                }
            }

            items(state.pets){ pet ->
                Row(modifier = Modifier.fillMaxWidth()){
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "${pet.name}, ", fontSize = 20.sp)

                        Text(text = pet.age.toString(), fontSize = 15.sp)
                    }
                    IconButton(onClick = {
                        onEvent(PetEvent.DeletePet(pet))
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete pet")
                    }
                }
            }

        }
        if (state.isAddingPet) {
            AddPetDialog(
                state = state,
                onEvent = onEvent
            )
        }
    }
}

@Composable
fun AddPetDialog(
    state: petState,
    onEvent: (PetEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onEvent(PetEvent.HideDialog)
        },
        title = { Text(text = "Add Pet") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = state.name,
                    onValueChange = {
                        onEvent(PetEvent.SetName(it))
                    },
                    placeholder = {
                        Text(text = "Name")
                    }
                )

                TextField(
                    value = state.age.toString(),
                    onValueChange = {
                        onEvent(PetEvent.SetAge(it))
                    },
                    placeholder = {
                        Text(text = "Age")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Only save the pet if all fields are filled
                    if (state.name.isNotBlank()) {
                        onEvent(PetEvent.SavePet)
                    }
                }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            Button(onClick = { onEvent(PetEvent.HideDialog) }) {
                Text(text = "Cancel")
            }
        }
    )
}

