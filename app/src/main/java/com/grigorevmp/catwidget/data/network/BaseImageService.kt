package com.grigorevmp.catwidget.data.network

import android.content.Context
import kotlinx.coroutines.flow.Flow

abstract class BaseImageService<T>(
    protected val baseUrl: String
) {
    abstract fun getPicture(context: Context): Flow<T>
}