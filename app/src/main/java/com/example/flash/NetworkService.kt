package com.example.flash

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface IpApiService {
    @GET("json")
    suspend fun getIpInfo(): IpInfo
}

object RetrofitClient {
    private const val BASE_URL = "http://ip-api.com/"

    val instance: IpApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IpApiService::class.java)
    }
}
