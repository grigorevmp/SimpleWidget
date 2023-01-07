package com.grigorevmp.catwidget.data.dto.image

import com.google.gson.annotations.SerializedName

data class AnimeImageDto(
    @SerializedName("url") val url: String
): BaseImageDto