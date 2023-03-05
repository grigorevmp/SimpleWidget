package com.grigorevmp.catwidget.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.view.View

object Utils {

    fun init(application: Application) {
        Utils.application = application
    }

    private var sharedPreferences: SharedPreferences? = null

    private var application: Application? = null

    private const val preferences_file = "quickPassPreference"
    private const val UNSCALED_ICON_SIZE = 0.7f
    private const val SCALED_ICON_SIZE = 1f

    fun initSharedPreferences() {
        sharedPreferences = application?.getSharedPreferences(
            preferences_file,
            Context.MODE_PRIVATE
        )
    }

    fun viewsGoneAndShow(
        viewsToGone: List<View> = listOf(),
        viewsToHide: List<View> = listOf(),
        viewsToShow: List<View> = listOf()
    ) {
        for (view in viewsToGone) {
            view.gone()
        }

        for (view in viewsToHide) {
            view.hide()
        }

        for (view in viewsToShow) {
            view.show()
        }
    }

    fun viewsScaleInOut(
        viewsToScaleIn: List<View> = listOf(),
        viewsToScaleOut: List<View> = listOf()
    ) {
        for (view in viewsToScaleIn) {
            scaleView(view)
        }

        for (view in viewsToScaleOut) {
            scaleView(view, true)
        }
    }



    private fun scaleView(view: View, unscaled: Boolean = false) {
        if (unscaled) {
            view.animate().scaleX(UNSCALED_ICON_SIZE).scaleY(UNSCALED_ICON_SIZE)
        } else {
            view.animate().scaleX(SCALED_ICON_SIZE).scaleY(SCALED_ICON_SIZE)
        }
    }



    // Extensions

    private fun View.gone() {
        visibility = View.GONE
    }

    private fun View.hide() {
        visibility = View.INVISIBLE
    }

    private fun View.show() {
        visibility = View.VISIBLE
    }





    enum class ImageTypeEnum(val type: String) {
        Cat("cat"), Dog("dog"), Anime("anime"),
    }
}