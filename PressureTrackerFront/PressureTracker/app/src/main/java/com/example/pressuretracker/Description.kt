package com.example.pressuretracker

import java.io.Serializable

/**
 * ++
 */
data class Description(val id:Int,
                       val feelings:String?,
                       val meals:String?,
                       val painDescription:String?,
                       val activityDescription:String?,
                       val stressLevel:Int?,
                        ):Serializable
