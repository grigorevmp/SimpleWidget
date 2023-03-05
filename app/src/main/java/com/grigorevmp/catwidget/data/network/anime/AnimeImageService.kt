package com.grigorevmp.catwidget.data.network.anime

import android.content.Context
import com.grigorevmp.catwidget.data.network.BaseImageService
import com.grigorevmp.catwidget.utils.Preferences
import kotlinx.coroutines.flow.flow
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AnimeImageService : BaseImageService(
    baseUrl = "https://api.waifu.pics/sfw/megumin"
) {
    override fun getPicture(context: Context) = flow {
        val okHttpClient = OkHttpClient().newBuilder()
        val cache = Cache(context.cacheDir, 4000)

        okHttpClient.cache(cache).build()

        val loader = Retrofit.Builder()
            .baseUrl("https://api.waifu.pics/sfw/")
            .client(okHttpClient.cache(cache).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = loader.create(AnimeImageApi::class.java)

        emit(service.getAnimePicture(AnimeImageService.ImageSfwCategory.Waifu.category))
    }

    fun getCustomPicture(
        context: Context,
        type: String = AnimeTypeEnum.SFW.type
    ) = flow {
        val okHttpClient = OkHttpClient().newBuilder()
        val cache = Cache(context.cacheDir, 4000)

        okHttpClient.cache(cache).build()

        val loader = Retrofit.Builder()
            .baseUrl("https://api.waifu.pics/${Preferences.animeImageType}/")
            .client(okHttpClient.cache(cache).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = loader.create(AnimeImageApi::class.java)

        val animeCategory = when (type) {
            AnimeTypeEnum.SFW.type -> Preferences.animeImageCategory
            AnimeTypeEnum.NSFW.type -> Preferences.animeImageRestrictedCategory
            else -> Preferences.animeImageCategory
        }

        emit(service.getAnimePicture(animeCategory))

    }

    override suspend fun getPictureFromWidget(context: Context, updateWidget: () -> Unit) {
        val okHttpClient = OkHttpClient().newBuilder()
        val cache = Cache(context.cacheDir, 4000)

        okHttpClient.cache(cache).build()

        val loader = Retrofit.Builder()
            .baseUrl("https://api.waifu.pics/${Preferences.animeImageType}/")
            .client(okHttpClient.cache(cache).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = loader.create(AnimeImageApi::class.java)

        val dogPicture = service.getAnimePicture(AnimeImageService.ImageSfwCategory.Waifu.category)

        Preferences.pictureUrl = dogPicture.url

        updateWidget()
    }

    suspend fun getPictureFromWidgetCustom(
        context: Context,
        type: String = AnimeTypeEnum.SFW.type,
        updateWidget: () -> Unit
    ) {
        val okHttpClient = OkHttpClient().newBuilder()
        val cache = Cache(context.cacheDir, 4000)

        okHttpClient.cache(cache).build()

        val loader = Retrofit.Builder()
            .baseUrl("https://api.waifu.pics/${Preferences.animeImageType}/")
            .client(okHttpClient.cache(cache).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = loader.create(AnimeImageApi::class.java)

        val animeCategory = when (type) {
            AnimeTypeEnum.SFW.type -> Preferences.animeImageCategory
            AnimeTypeEnum.NSFW.type -> Preferences.animeImageRestrictedCategory
            else -> Preferences.animeImageCategory
        }

        val dogPicture = service.getAnimePicture(animeCategory)

        Preferences.pictureUrl = dogPicture.url

        updateWidget()
    }

    enum class AnimeTypeEnum(val type: String) {
        SFW("sfw"),
        NSFW("nsfw")
    }

    interface ImageAnimeCategory {
        fun getCategoryName(): String
    }

    enum class ImageSfwCategory(val category: String) : ImageAnimeCategory {
        Waifu("waifu"),
        Neko("neko"),
        Shinobu("shinobu"),
        Megumin("megumin"),
        Bully("bully"),
        Cuddle("cuddle"),
        Cry("cry"),
        Hug("hug"),
        Awoo("awoo"),
        Kiss("kiss"),
        Lick("lick"),
        Pat("pat"),
        Smug("smug"),
        Bonk("bonk"),
        Yeet("yeet"),
        Blush("blush"),
        Smile("smile"),
        Wave("wave"),
        Highfive("highfive"),
        Handhold("handhold"),
        Nom("nom"),
        Bite("bite"),
        Glomp("glomp"),
        Slap("slap"),
        Kill("kill"),
        Kick("kick"),
        Happy("happy"),
        Wink("wink"),
        Poke("poke"),
        Dance("dance"),
        Cringe("cringe");

        override fun getCategoryName() = this.category
        companion object {
            fun getValuesNames() = values().map { it.category }
        }
    }

    enum class ImageNsfwCategory(val category: String) : ImageAnimeCategory {
        Waifu("waifu"),
        Neko("neko"),
        Trap("trap"),
        Blowjob("blowjob");

        override fun getCategoryName() = this.category

        companion object {
            fun getValuesNames() = values().map { it.category }
        }
    }
}