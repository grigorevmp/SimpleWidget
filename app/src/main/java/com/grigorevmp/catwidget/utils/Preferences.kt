package com.grigorevmp.catwidget.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.grigorevmp.catwidget.data.network.anime.AnimeImageService

object Preferences {

    fun init(application: Application) {
        Preferences.application = application
    }

    private const val preferences_file = "quickPassPreference"

    private var sharedPreferences: SharedPreferences? = null

    private var application: Application? = null

    var fullReloadOnTap
        set(value) { setReload(value) }
        get() = sharedPreferences?.getBoolean("prefReload", false) ?: false

    var pictureUrl
        set(value) { setUrl(value) }
        get() = sharedPreferences?.getString("prefUrl", "") ?: ""

    var pictureType
        set(value) { setImageType(value) }
        get() = sharedPreferences?.getString("prefAnimal", Utils.ImageTypeEnum.Cat.type) ?: Utils.ImageTypeEnum.Cat.type

    var showFullMonth
        set(value) { setMonth(value) }
        get() = sharedPreferences?.getBoolean("prefMonth", false) ?: false

    var openCalendarOnTap
        set(value) { setCalendar(value) }
        get() = sharedPreferences?.getBoolean("prefCalendar", false) ?: false

    var animeImageType
        set(value) { setAnimeType(value) }
        get() = sharedPreferences?.getString("prefAnimeType", AnimeImageService.AnimeTypeEnum.SFW.type) ?: AnimeImageService.AnimeTypeEnum.SFW.type

    var animeImageCategory
        set(value) { setAnimeCategory(value) }
        get() = sharedPreferences?.getString("prefAnimeCategory", AnimeImageService.ImageSfwCategory.Awoo.category) ?:  AnimeImageService.ImageSfwCategory.Awoo.category

    var animeImageRestrictedCategory
        set(value) { setAnimeRestrictedCategory(value) }
        get() = sharedPreferences?.getString("prefAnimeRestrictedCategory", AnimeImageService.ImageNsfwCategory.Waifu.category) ?:  AnimeImageService.ImageNsfwCategory.Waifu.category



    fun initSharedPreferences() {
        sharedPreferences = application?.getSharedPreferences(
            preferences_file,
            Context.MODE_PRIVATE
        )
    }



    private fun setReload(isReload: Boolean) {
        val sharedPreferences = sharedPreferences ?: return

        with(sharedPreferences.edit()) {
            putBoolean("prefReload", isReload)
            apply()
        }
    }

    private fun setUrl(url: String) {
        val sharedPreferences = sharedPreferences ?: return

        with(sharedPreferences.edit()) {
            putString("prefUrl", url)
            apply()
        }
    }

    private fun setImageType(type: String) {
        val sharedPreferences = sharedPreferences ?: return

        with(sharedPreferences.edit()) {
            putString("prefAnimal", type)
            apply()
        }
    }

    private fun setMonth(isLong: Boolean) {
        val sharedPreferences = sharedPreferences ?: return

        with(sharedPreferences.edit()) {
            putBoolean("prefMonth", isLong)
            apply()
        }
    }

    private fun setCalendar(openCal: Boolean) {
        val sharedPreferences = sharedPreferences ?: return

        with(sharedPreferences.edit()) {
            putBoolean("prefCalendar", openCal)
            apply()
        }
    }

    private fun setAnimeType(animeType: String) {
        val sharedPreferences = sharedPreferences ?: return

        with(sharedPreferences.edit()) {
            putString("prefAnimeType", animeType)
            apply()
        }
    }

    private fun setAnimeCategory(animeCategory: String) {
        val sharedPreferences = sharedPreferences ?: return

        with(sharedPreferences.edit()) {
            putString("prefAnimeCategory", animeCategory)
            apply()
        }
    }

    private fun setAnimeRestrictedCategory(animeRestrictedCategory: String) {
        val sharedPreferences = sharedPreferences ?: return

        with(sharedPreferences.edit()) {
            putString("prefAnimeRestrictedCategory", animeRestrictedCategory)
            apply()
        }
    }

}