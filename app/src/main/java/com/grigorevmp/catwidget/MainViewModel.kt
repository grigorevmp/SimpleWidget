package com.grigorevmp.catwidget

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.grigorevmp.catwidget.MainViewModel.PictureType.*
import com.grigorevmp.catwidget.data.dto.image.AnimeImageDto
import com.grigorevmp.catwidget.data.dto.image.BaseImageDto
import com.grigorevmp.catwidget.data.dto.image.CatImageDto
import com.grigorevmp.catwidget.data.dto.image.DogImageDto
import com.grigorevmp.catwidget.data.network.BaseImageService
import com.grigorevmp.catwidget.data.network.anime.AnimeImageService
import com.grigorevmp.catwidget.data.network.cat.CatImageService
import com.grigorevmp.catwidget.data.network.dog.DogImageService
import com.grigorevmp.catwidget.utils.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val catImageService = CatImageService()
    private val dogImageService = DogImageService()
    private val animeImageService = AnimeImageService()

    suspend fun getPictureByTypeAndChangeState(
        context: Context,
        type: PictureType,
        errorAction: () -> Unit,
        successAction: (String) -> Unit,
        animeType: String = AnimeImageService.AnimeTypeEnum.SFW.type
    ) {
        Log.d("ViewModel", "Searching for picture from main activity")

        when (type) {
            Cat -> getPictureByService(context, catImageService, errorAction, successAction)
            Dog -> getPictureByService(context, dogImageService, errorAction, successAction)
            Anime -> getAnimePictureByService(
                context,
                errorAction,
                successAction,
                animeType
            )
        }
    }



    private suspend fun getPictureByService(
        context: Context,
        service: BaseImageService,
        errorAction: () -> Unit,
        successAction: (String) -> Unit
    ) {
        service.getPicture(context).flowOn(Dispatchers.IO).catch { _ ->
            Log.d("ViewModel", "Picture getting error")

            withContext(Dispatchers.Main) {
                errorAction()
            }
        }.collect { image ->
            Log.d("ViewModel", "Picture got successfully")

            val pictureUrl = getUrlByImageType(image)

            Preferences.pictureUrl = pictureUrl

            withContext(Dispatchers.Main) {
                successAction(pictureUrl)
            }
        }
    }

    private suspend fun getAnimePictureByService(
        context: Context,
        errorAction: () -> Unit,
        successAction: (String) -> Unit,
        animeType: String = AnimeImageService.AnimeTypeEnum.SFW.type
    ) {
        animeImageService.getCustomPicture(context, animeType).flowOn(Dispatchers.IO).catch { _ ->
            Log.d("ViewModel", "Picture getting error")

            withContext(Dispatchers.Main) {
                errorAction()
            }
        }.collect { image ->
            Log.d("ViewModel", "Picture got successfully")

            val pictureUrl = image.url

            Preferences.pictureUrl = pictureUrl

            withContext(Dispatchers.Main) {
                successAction(pictureUrl)
            }
        }
    }

    private fun getUrlByImageType(image: BaseImageDto): String {
        return when (image) {
            is CatImageDto -> image.file
            is DogImageDto -> image.message
            is AnimeImageDto -> image.url
        }
    }



    enum class PictureType {
        Cat,
        Dog,
        Anime
    }
}