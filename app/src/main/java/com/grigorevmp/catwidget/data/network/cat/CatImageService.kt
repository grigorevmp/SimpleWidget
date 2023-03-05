package com.grigorevmp.catwidget.data.network.cat

import android.content.Context
import com.grigorevmp.catwidget.data.network.BaseImageService
import com.grigorevmp.catwidget.utils.Preferences
import kotlinx.coroutines.flow.flow
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CatImageService : BaseImageService(
    baseUrl = "https://aws.random.cat/"
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

        val service = loader.create(CatImageApi::class.java)

        emit(service.getCatPicture())
    }

    override suspend fun getPictureFromWidget(context: Context, updateWidget: () -> Unit) {
        val okHttpClient = OkHttpClient().newBuilder()
        val cache = Cache(context.cacheDir, 4000)

        okHttpClient.cache(cache).build()

        val loader = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient.cache(cache).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = loader.create(CatImageApi::class.java)

        val dogPicture = service.getCatPicture()

        Preferences.pictureUrl = dogPicture.file

        updateWidget()
    }
}