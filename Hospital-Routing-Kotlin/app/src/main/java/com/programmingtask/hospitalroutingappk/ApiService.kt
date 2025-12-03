package com.programmingtask.hospitalroutingappk

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/maps/api/directions/json")
    suspend fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String
    ): Response<RouteResponse>
}