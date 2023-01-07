package com.grigorevmp.catwidget.data.dto.image

import com.google.gson.annotations.SerializedName

data class CatImageDto(
    @SerializedName("file") val file: String
): BaseImageDto