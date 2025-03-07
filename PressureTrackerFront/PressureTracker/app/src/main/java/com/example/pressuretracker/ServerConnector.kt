package com.example.pressuretracker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ++
 */
object ServerConnector {

    val appServer: Retrofit;
    val serviceApp : IApi;
    init{
            appServer = Retrofit.Builder()
                .baseUrl("http://192.168.1.86:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
            serviceApp = appServer.create(IApi::class.java)
        
    }
}