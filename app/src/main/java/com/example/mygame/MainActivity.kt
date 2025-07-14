package com.example.mygame
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.work.*
import com.example.mygame.data.Pet
import com.example.mygame.data.PetDao
import com.example.mygame.data.PetDatabase
import com.example.mygame.data.PetEvent
import com.example.mygame.data.PetScreen
import com.example.mygame.data.PetViewModel
import com.example.mygame.data.petState
import com.example.mygame.screens.AdoptionScreen
import com.example.mygame.screens.KitchenScreen
import com.example.mygame.screens.LoveScreen
import com.example.mygame.screens.MyPetsScreen
import com.example.mygame.screens.PetCareScreen
import com.example.mygame.screens.PetDetailScreen
import com.example.mygame.screens.PoolScreen
import com.example.mygame.services.NotificationWorker
import com.example.mygame.services.ReminderViewModel
import com.example.mygame.ui.theme.MyGameTheme
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val workManager: WorkManager by lazy { WorkManager.getInstance(this) }
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            PetDatabase::class.java,
            "pets.db"
        ).build()
    }

    private val viewModel by viewModels<PetViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PetViewModel(db.dao) as T
                }
            }
        }
    )
    

    private val reminderViewModel by viewModels<ReminderViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReminderViewModel(workManager, db.dao) as T
                }
            }
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the permission launcher for notification permission.
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {

                reminderViewModel.sendFirstNotification(applicationContext)
            } else {
                Log.d("MainActivity", "Notification permission denied")
            }
        }

        // Request notification permission if needed (Android 13 and above).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent {
            MyGameTheme {
                val petDao: PetDao = db.dao
                val state by viewModel.state.collectAsState()
                val onEvent = viewModel::onEvent
                PetScreen(state = state, onEvent = onEvent)
                MainFunction(viewModel, state = state, petDao, onEvent)
            }
        }
        enqueueNotificationWork()
    }
    private fun enqueueNotificationWork() {
        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "reminder_channel",
            ExistingWorkPolicy.REPLACE,
            notificationWorkRequest
        )
    }
}

// step 1 create route classes
sealed class Screen(val route: String) {
    object Home : Screen("Home")
    object Pool : Screen("Pool")
    object Love : Screen("Love")
    object Kitchen : Screen("Kitchen")
    object PetCare : Screen("PetCare/{petName}") {
        fun createCareRoute(petName: String) = "PetCare/$petName"
    }
    object Adoption : Screen("Adoption")
    object PetDetail : Screen("PetDetail/{petName}") {
        fun createRoute(petName: String) = "PetDetail/$petName"
    }

// if you need another screen you could do a pet stats screen with the pet with highest stats

}

// step 2 create navigation items here
data class NavItem(
    var label: String,
    var icon: ImageVector,
    var screen: Screen,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFunction(viewModel: PetViewModel,
                 state: petState,
                 petDao: PetDao,
                 onEvent: (event: PetEvent) -> Unit) {
    val petsList = remember { mutableStateOf<List<Pet>>(emptyList()) }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        petsList.value = petDao.getPetsOrderedByName().first()
        loading.value = false
    }

    var selectedPet by remember { mutableStateOf<Pet?>(null) }

    var mediaPlayer: MediaPlayer? = null
    // step 4 initialise the nav controller here
    val navController = rememberNavController()


    val context = LocalContext.current
    // step 5 create list of navigation items
    val navItemList = listOf(
        NavItem(label = "My Pets", icon = Icons.Default.Home, screen = Screen.Home),
        NavItem(label = "Adoption", icon = Icons.Default.Star, screen = Screen.Adoption)
    )

    // define a variable to store the value of selected navigation item
    var selectedIndex by remember { mutableStateOf(0) }
    var isMusicPlaying by remember { mutableStateOf(false) }
    var adoptedPets by remember { mutableStateOf(state.pets.filter { it.isAdopted }) }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Pocket Pet") }) },
        bottomBar = {
            // bottom bar navigation here
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            if (navController.currentDestination?.route != navItem.screen.route) {
                                navController.navigate(navItem.screen.route) {
                                    launchSingleTop =
                                        true // prevent multiple copies of same destination
                                    restoreState = true // restore state to previously selected item
                                }
                            }
                        },
                        label = { Text(text = navItem.label) },
                        icon = { navItem.icon }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = setOnClickListener@{
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(context, R.raw.soundtrack)
                }
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    isMusicPlaying = false
                } else {
                    mediaPlayer?.start()
                    isMusicPlaying = true
                }
            }) {
                val volumeIcon = if (isMusicPlaying) R.drawable.unmute else R.drawable.mute
                // Image source: https://icons8.com/icon/56021/speaker
                Image(
                    painter = painterResource(id = volumeIcon),
                    contentDescription = if (isMusicPlaying) "Playing Music" else "Mute Music",
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                )
            }
        }
    ) { innerPadding ->
        // set the Nav Graph
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Kitchen.route) {
                KitchenScreen(
                    navController = navController,
                    selectedPet = selectedPet
                )
            }
            composable(Screen.Pool.route) {
                PoolScreen(
                    navController = navController,
                    selectedPet = selectedPet
                )
            }
            composable(Screen.Love.route) {
                LoveScreen(
                    navController = navController,
                    selectedPet = selectedPet
                )
            }
            composable(Screen.Home.route) {
                MyPetsScreen(
                    viewModel,
                    onEvent = onEvent,
                    onPetClick = { pet ->
                        selectedPet = pet
                        navController.navigate(Screen.PetCare.createCareRoute(pet.name))
                    },
                    onRemoveFromFavorites = { pet ->
                        adoptedPets = adoptedPets.toMutableList().apply {
                            remove(pet)
                        }
                        viewModel.onEvent(PetEvent.ReleasePet(pet))
                    }
                )
            }
            composable("PetCare/{petName}") { backStackEntry ->
                selectedPet?.let {
                    PetCareScreen(
                        viewModel,
                        navController = navController,
                        selectedPet = selectedPet!!,
                        onEvent = onEvent)
                }

            }

            composable(Screen.Adoption.route) {
                AdoptionScreen(
                    viewModel,
                    onAddToFavorites = { pet ->
                        viewModel.onEvent(PetEvent.AdoptPet(pet))
                    },
                    onPetClick = { pet ->
                        selectedPet = pet
                        navController.navigate(Screen.PetDetail.createRoute(pet.name)) // Ensure proper navigation
                    },
                    onAdoptPet = { pet ->
                        if (pet.isAdopted) {
                            viewModel.onEvent(PetEvent.ReleasePet(pet))
                        } else {
                            viewModel.onEvent(PetEvent.AdoptPet(pet))
                        }
                    }
                )
            }
            composable("PetDetail/{petName}") { backStackEntry ->
                PetDetailScreen(
                    selectedPet = selectedPet
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}