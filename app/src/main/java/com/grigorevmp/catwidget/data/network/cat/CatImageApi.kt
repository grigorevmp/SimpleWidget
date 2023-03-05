package com.grigorevmp.catwidget.data.network.cat

import com.grigorevmp.catwidget.data.dto.image.CatImageDto
import retrofit2.http.GET

interface CatImageApi {
    @GET("meow")
    suspend fun getCatPicture() : CatImageDto
}