package com.example.mygame.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.runtime.*

@Composable
fun proximitySensorHandler(
    context: Context,
    onNear: () -> Unit,
    onFar: () -> Unit
): Boolean {


    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val proximitySensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) }
    var isNear by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val distance = event.values[0]
                Log.d("Proximity", "Distance: $distance, IsNear: $isNear")
                if (distance < 5f) {
                    isNear = true
                    onNear()
                } else {
                    isNear = false
                    onFar()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    return isNear
}
