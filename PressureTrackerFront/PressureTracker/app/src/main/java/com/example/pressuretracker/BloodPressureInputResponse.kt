package com.example.pressuretracker

import java.util.*
/**
 * ++
 */
data class BloodPressureInputResponse(val inputid:Int,
                                      val systolic:Int,
                                      val diastolic:Int,
                                      val heartrate:Int,
                                      val descriptionid:Int?,
                                      val feelings:String?,
                                      val meals:String?,
                                      val paindescription:String?,
                                      val activitydescription:String?,
                                      val stresslevel:Int?,
                                      val date:Date?)
