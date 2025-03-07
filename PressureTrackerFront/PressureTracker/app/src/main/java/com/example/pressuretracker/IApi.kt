package com.example.pressuretracker

import retrofit2.Call
import retrofit2.http.*

/**
 * ++
 */
interface IApi {
    @GET("reminder/getReminders")
    fun getReminders (@Header("Authorization") authHeader:String):Call<MutableList<Reminder>>

    @POST("auth/login/")
    fun login(@Body user: User): Call<Tokens>

    @POST("auth/register/")
    fun register(@Body user: User): Call<Tokens>

    @POST("auth/verifyAccessToken/")
    fun verifyAccessToken(@Body tokens: Tokens): Call<Tokens>

    @GET("inputs/view/bloodpressureinput")
    fun getBloodPressureInputs(@Header("Authorization") authHeader:String?): Call<List<BloodPressureInputResponse>>

    @GET("inputs/view/glucoselevelinput")
    fun getBloodSugarInputs(@Header("Authorization") authHeader:String?): Call<List<SugarLevelInputResponse>>

    @POST("reminder/add/")
    fun addReminder(@Header("Authorization") authHeader:String?,@Body reminder: Reminder): Call<String>

    @POST("inputs/add/{type}")
    fun addBloodPressureInput(@Header("Authorization") authHeader:String?, @Body bloodPressureInput: BloodPressureInput, @Path("type") type:String): Call<String>

    @POST("inputs/add/{type}")
    fun addSugarLevelInput(@Header("Authorization") authHeader:String?, @Body sugarLevelInput: SugarLevelInput, @Path("type") type:String): Call<String>

    @DELETE("reminder/delete/{reminder_id}")
    fun deleteReminder(@Header("Authorization") authHeader:String?,@Path("reminder_id") reminder_id:String): Call<String>

    @DELETE("inputs/delete/{input_id}")
    fun deleteInput(@Header("Authorization") authHeader:String?,@Path("input_id") input_id:String): Call<String>
}