package com.grigorevmp.catwidget.data.network.anime

import com.grigorevmp.catwidget.data.dto.image.AnimeImageDto
import retrofit2.http.GET
import retrofit2.http.Url

interface AnimeImageApi {
    @GET
    suspend fun getAnimePicture(@Url category: String) : AnimeImageDto
}