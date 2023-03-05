package com.grigorevmp.catwidget.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.AppWidgetTarget
import com.bumptech.glide.request.target.Target
import com.grigorevmp.catwidget.R
import com.grigorevmp.catwidget.data.network.anime.AnimeImageService
import com.grigorevmp.catwidget.data.network.cat.CatImageService
import com.grigorevmp.catwidget.data.network.dog.DogImageService
import com.grigorevmp.catwidget.utils.Preferences
import com.grigorevmp.catwidget.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class BaseWidget : AppWidgetProvider() {

    private val dogImageService = DogImageService()
    private val catImageService = CatImageService()
    private val animeImageService = AnimeImageService()



    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)

        if (context == null) return
        if (oldWidgetIds == null) return

        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

        for (appWidgetId in oldWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        if (context == null) return
        if (appWidgetIds == null) return
        if (appWidgetManager == null) return

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

        if (context == null) return

        when (intent.action) {
            "FULL_UPDATE" -> {
                callUpdate(context, intent, appWidgetManager, Preferences.pictureType)
            }

            "LOCAL_UPDATE" -> {
                if (Preferences.openCalendarOnTap) {
                    val calendarUri = CalendarContract.CONTENT_URI
                        .buildUpon()
                        .appendPath("time")
                        .build()
                    val localIntent = Intent(Intent.ACTION_VIEW, calendarUri)
                    localIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(localIntent)
                }
                val extras = intent.extras
                val appWidgetId = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: return

                updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
                )
            }

            else -> {
                val extras = intent.extras
                val appWidgetId = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: return

                updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
                )
            }
        }
    }



    private fun getImage(context: Context, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.base_widget)
        views.setTextViewText(R.id.tvTextDay, getDay())
        views.setTextViewText(R.id.tvTextMonth, getMonth(Preferences.showFullMonth))

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        val appWidgetTarget = AppWidgetTarget(context, R.id.ivImagePreview, views, appWidgetId)

        views.setViewVisibility(R.id.pbLoading, View.GONE)
        views.setViewVisibility(R.id.ivImagePreview, View.VISIBLE)

        GlobalScope.launch(Dispatchers.IO) {
            Glide.with(context)
                .asBitmap()
                .load(Preferences.pictureUrl)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                })
                .placeholder(circularProgressDrawable)
                .override(500, 500)
                .into(appWidgetTarget)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.base_widget)

        if (Preferences.fullReloadOnTap) {
            views.setOnClickPendingIntent(
                R.id.widget, getPendingSelfIntentFullUpdate(context, appWidgetId)
            )
        } else {
            views.setOnClickPendingIntent(
                R.id.widget, getPendingSelfIntent(context, appWidgetId)
            )
        }

        getImage(context, appWidgetId)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getPendingSelfIntent(
        context: Context,
        appWidgetId: Int
    ): PendingIntent {
        val intent = Intent(context, BaseWidget::class.java)
            .setAction("LOCAL_UPDATE")
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

        return PendingIntent.getBroadcast(context, appWidgetId, intent, FLAG_IMMUTABLE)
    }

    private fun getPendingSelfIntentFullUpdate(
        context: Context,
        appWidgetId: Int
    ): PendingIntent {
        val intent = Intent(context, BaseWidget::class.java)
            .setAction("FULL_UPDATE")
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

        return PendingIntent.getBroadcast(context, appWidgetId, intent, FLAG_IMMUTABLE)
    }

    private fun callUpdate(
        context: Context?, intent: Intent,
        appWidgetManager: AppWidgetManager,
        type: String?
    ) {
        val currentContext = context ?: return

        val extras = intent.extras
        val appWidgetId = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: return

        CoroutineScope(Dispatchers.IO).launch {

            when (type) {
                Utils.ImageTypeEnum.Dog.type -> dogImageService.getPictureFromWidget(
                    currentContext.applicationContext
                ) {
                    launch {
                        withContext(Dispatchers.Main){
                            updateAppWidget(
                                currentContext,
                                appWidgetManager,
                                appWidgetId
                            )
                        }
                    }
                    Log.i("Widget", "Widget updated")
                }

                Utils.ImageTypeEnum.Cat.type -> catImageService.getPictureFromWidget(
                    currentContext.applicationContext
                ) {
                    launch {
                        withContext(Dispatchers.Main){
                            updateAppWidget(
                                currentContext,
                                appWidgetManager,
                                appWidgetId
                            )
                        }
                    }
                    Log.i("Widget", "Widget updated")
                }

                Utils.ImageTypeEnum.Anime.type -> animeImageService.getPictureFromWidgetCustom(
                    currentContext.applicationContext,
                    Preferences.animeImageType,
                ) {
                    launch {
                        withContext(Dispatchers.Main){
                            updateAppWidget(
                                currentContext,
                                appWidgetManager,
                                appWidgetId
                            )
                        }
                    }
                    Log.i("Widget", "Widget updated")
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDay(): String {
        val sdf = SimpleDateFormat("dd")
        return sdf.format(Date())
    }

    @SuppressLint("SimpleDateFormat")
    private fun getMonth(isFullMonth: Boolean): String {
        val sdf =
            if (isFullMonth) SimpleDateFormat("LLLL")
            else SimpleDateFormat("MMM")
        return sdf.format(Date())
    }
}

