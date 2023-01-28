package com.grigorevmp.catwidget.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.grigorevmp.catwidget.BuildConfig
import com.grigorevmp.catwidget.data.network.anime.AnimeImageService

object Utils {

    fun init(application: Application) {
        Utils.application = application
    }

    private var sharedPreferences: SharedPreferences? = null

    private var application: Application? = null

    private const val preferences_file = "quickPassPreference"

    fun setSharedPreferences() {
        sharedPreferences = application?.getSharedPreferences(
            preferences_file,
            Context.MODE_PRIVATE
        )
    }

    fun setReload(isReload: Boolean) {
        with(sharedPreferences!!.edit()) {
            putBoolean("prefReload", isReload)
            apply()
        }
    }

    fun setUrl(url: String) {
        with(sharedPreferences!!.edit()) {
            putString("prefUrl", url)
            apply()
        }
    }

    fun setImageType(type: String) {
        with(sharedPreferences!!.edit()) {
            putString("prefAnimal", type)
            apply()
        }
    }

    fun setMonth(isLong: Boolean) {
        with(sharedPreferences!!.edit()) {
            putBoolean("prefMonth", isLong)
            apply()
        }
    }

    fun setCalendar(openCal: Boolean) {
        with(sharedPreferences!!.edit()) {
            putBoolean("prefCalendar", openCal)
            apply()
        }
    }

    fun setAnimeType(animeType: String) {
        with(sharedPreferences!!.edit()) {
            putString("prefAnimeType", animeType)
            apply()
        }
    }

    fun setAnimeCategory(animeCategory: String) {
        with(sharedPreferences!!.edit()) {
            putString("prefAnimeCategory", animeCategory)
            apply()
        }
    }

    fun setAnimeRestrictedCategory(animeRestrictedCategory: String) {
        with(sharedPreferences!!.edit()) {
            putString("prefAnimeRestrictedCategory", animeRestrictedCategory)
            apply()
        }
    }

    fun getUrl() = sharedPreferences!!.getString("prefUrl", "")
    fun getImageType() = sharedPreferences!!.getString("prefAnimal", "cat")
    fun getMonthLong() = sharedPreferences!!.getBoolean("prefMonth", false)
    fun getReload() = sharedPreferences!!.getBoolean("prefReload", false)
    fun getCalendar() = sharedPreferences!!.getBoolean("prefCalendar", false)
    fun getAnimeType(): String? {
        return if (BuildConfig.showRestrictedContent) sharedPreferences!!.getString("prefAnimeType", AnimeImageService.AnimeTypeEnum.SFW.type)
        else AnimeImageService.AnimeTypeEnum.SFW.type
    }

    fun getAnimeCategory() = sharedPreferences!!.getString(
        "prefAnimeCategory",
        AnimeImageService.ImageSfwCategory.Awoo.category
    )

    fun getAnimeRestrictedCategory() = sharedPreferences!!.getString(
        "prefAnimeRestrictedCategory",
        AnimeImageService.ImageNsfwCategory.Waifu.category
    )
}