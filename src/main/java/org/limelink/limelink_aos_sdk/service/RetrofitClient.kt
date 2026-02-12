package org.limelink.limelink_aos_sdk.service

import org.limelink.limelink_aos_sdk.service.limelink.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var baseUrl: String = "https://limelink.org/"
    private var retrofitInstance: Retrofit? = null
    private var apiServiceInstance: ApiService? = null

    fun initialize(url: String) {
        baseUrl = url
        retrofitInstance = null
        apiServiceInstance = null
    }

    val instance: Retrofit
        get() {
            return retrofitInstance ?: Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofitInstance = it }
        }

    val apiService: ApiService
        get() {
            return apiServiceInstance ?: instance.create(ApiService::class.java)
                .also { apiServiceInstance = it }
        }
}
