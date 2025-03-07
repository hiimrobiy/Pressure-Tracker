package com.example.pressuretracker

import java.io.Serializable
import java.util.*
/**
 * ++
 */
data class SugarLevelInput(var id:Int, val level : Double, val date:Date?, val description: Description?) :Serializable
