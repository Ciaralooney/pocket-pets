package com.example.mygame.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mygame.R
import com.example.mygame.data.Pet
import com.example.mygame.data.PetDao
import com.example.mygame.data.PetDatabase
import com.example.mygame.services.NotificationWorker.Companion.CHANNEL_ID
import com.example.mygame.services.NotificationWorker.Companion.CHANNEL_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderViewModel(
    private val workManager: WorkManager,
    private val dao: PetDao
) : ViewModel() {

    fun scheduleReminder(delayInSeconds: Long) {
        val reminderWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "reminder_work",
            ExistingWorkPolicy.REPLACE,
            reminderWork
        )
    }
    fun sendFirstNotification(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NotificationWorker.CHANNEL_ID,
                    NotificationWorker.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for alarm notifications"
                }
                notificationManager.createNotificationChannel(channel)
            }
            val notification = NotificationCompat.Builder(context, NotificationWorker.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Notifications")
                .setContentText("You have enabled notifications")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(1, notification)
        } else {
            Log.d("ReminderViewModel", "Notification permission not granted")
        }
    }

}
class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "pet_alerts"
        const val CHANNEL_NAME = "Pet Alerts"
    }

    override fun doWork(): Result {
        val petDao = PetDatabase.getInstance(applicationContext).dao
        monitorPetsAndNotify(petDao)
        return Result.success()
    }
/*
    private fun monitorPetsAndNotify(petDao: PetDao) {
        CoroutineScope(Dispatchers.IO).launch {
            petDao.getAdoptedPets(true).collect { adoptedPets ->
                adoptedPets.forEach { pet ->
                    combine(
                        petDao.getHungerMeter(pet.id),
                        petDao.getHappyMeter(pet.id)
                    ) { hunger, happy ->
                        Pair(hunger, happy)
                    }.collect { (hunger, happy) ->
                        if (hunger == 0 || happy == 0) {
                            sendNotification(pet, hunger, happy)
                        }
                    }
                }
            }
        }
    }

 */

    private fun monitorPetsAndNotify(petDao: PetDao) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                petDao.getAdoptedPets(true).collect { adoptedPets ->
                    adoptedPets.forEach { pet ->
                        launch {
                            combine(
                                petDao.getHungerMeter(pet.id),
                                petDao.getHappyMeter(pet.id)
                            ) { hunger, happy ->
                                Pair(hunger, happy)
                            }.collect { (hunger, happy) ->
                                if (hunger == 0 || happy == 0) {
                                    sendNotification(pet, hunger, happy)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationWorker", "Error monitoring pets: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(pet: Pet, hunger: Int, happy: Int) {
        val message = when {
            hunger == 0 && happy == 0 -> "${pet.name} is hungry and sad"
            hunger == 0 -> "${pet.name} is craving a cinnamon bun!"
            happy == 0 -> "${pet.name} is feeling blue"
            else -> return
        }

        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${pet.name} Needs Your Attention")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(pet.id, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for pet hunger or happiness"
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
