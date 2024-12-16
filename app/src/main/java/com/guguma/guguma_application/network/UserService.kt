package com.guguma.guguma_application.network

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserService {
    @POST("api/users/login")
    fun loginOrCreateUser(
        @Query("client_uuid") clientUuid: String,
        @Query("nickname") nickname: String? = null
    ): Call<UserResponse>

    @PUT("api/users/nickname")
    fun updateNickname(
        @Query("client_uuid") clientUuid: String,
        @Query("newNickname") newNickname: String
    ): Call<UserResponse>
}