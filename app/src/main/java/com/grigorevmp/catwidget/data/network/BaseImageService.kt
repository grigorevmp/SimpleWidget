package com.grigorevmp.catwidget.data.network

import android.content.Context
import com.grigorevmp.catwidget.data.dto.image.BaseImageDto
import kotlinx.coroutines.flow.Flow

abstract class BaseImageService(
    protected val baseUrl: String
) {
    abstract fun getPicture(context: Context): Flow<BaseImageDto>

    abstract suspend fun getPictureFromWidget(context: Context, updateWidget: () -> Unit)
}