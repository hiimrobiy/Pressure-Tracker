package com.example.pressuretracker

import java.io.Serializable
import java.util.*
/**
 * ++
 */
data class BloodPressureInput(var id:Int, val systolic : Int, val diastolic:Int, val heartRate:Int, val date:Date?, val description: Description?) :Serializable
