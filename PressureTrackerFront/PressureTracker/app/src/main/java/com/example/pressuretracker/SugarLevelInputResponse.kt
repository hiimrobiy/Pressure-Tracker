package com.example.pressuretracker

import java.util.*
/**
 * ++
 */
data class SugarLevelInputResponse(val inputid:Int,
                                  val levels:Int,
                                  val descriptionid:Int?,
                                  val feelings:String?,
                                  val meals:String?,
                                  val paindescription:String?,
                                  val activitydescription:String?,
                                  val stresslevel:Int?,
                                  val date: Date?)
