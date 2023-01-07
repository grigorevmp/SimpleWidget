package com.grigorevmp.catwidget.data.dto.image

import com.google.gson.annotations.SerializedName

data class DogImageDto(
    @SerializedName("message") val message: String
): BaseImageDto