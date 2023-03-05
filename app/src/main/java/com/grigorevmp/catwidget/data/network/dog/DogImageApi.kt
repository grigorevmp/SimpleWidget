package com.grigorevmp.catwidget.data.network.dog

import com.grigorevmp.catwidget.data.dto.image.DogImageDto
import retrofit2.http.GET

interface DogImageApi {
    @GET("random")
    suspend fun getDogPicture() : DogImageDto
}