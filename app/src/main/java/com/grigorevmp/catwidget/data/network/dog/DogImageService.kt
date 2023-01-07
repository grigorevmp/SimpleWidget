package com.grigorevmp.catwidget.data.network.dog

import android.content.Context
import com.grigorevmp.catwidget.data.dto.image.DogImageDto
import com.grigorevmp.catwidget.data.network.BaseImageService
import kotlinx.coroutines.flow.flow
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DogImageService : BaseImageService<DogImageDto>(
    baseUrl = "https://dog.ceo/api/breeds/image/"
) {

    override fun getPicture(context: Context) = flow {
        val okHttpClient = OkHttpClient().newBuilder()
        val cache = Cache(context.cacheDir, 4000)

        okHttpClient.cache(cache).build()

        val loader = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient.cache(cache).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = loader.create(DogImageApi::class.java)

        emit(service.getDogPicture())
    }
}