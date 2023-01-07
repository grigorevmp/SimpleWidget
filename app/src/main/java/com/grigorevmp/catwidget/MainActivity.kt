package com.grigorevmp.catwidget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.grigorevmp.catwidget.data.network.anime.AnimeImageService
import com.grigorevmp.catwidget.data.network.cat.CatImageService
import com.grigorevmp.catwidget.data.network.dog.DogImageService
import com.grigorevmp.catwidget.databinding.ActivityMainBinding
import com.grigorevmp.catwidget.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val catImageService = CatImageService()
    private val dogImageService = DogImageService()
    private val animeImageService = AnimeImageService()

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val UNSCALED_ICON_SIZE = 0.7f
        const val SCALED_ICON_SIZE = 1f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        reloadResolver()
        initDate(Utils.getMonthLong())
        initListeners()

    }

    private fun scaleView(view: View, unscaled: Boolean = false) {
        if (unscaled) {
            view.animate().scaleX(UNSCALED_ICON_SIZE).scaleY(UNSCALED_ICON_SIZE)
        } else {
            view.animate().scaleX(SCALED_ICON_SIZE).scaleY(SCALED_ICON_SIZE)
        }
    }

    private fun initListeners() {
        binding.fabReload.setOnClickListener {
            reloadResolver()
        }

        binding.sMonth.isChecked = Utils.getMonthLong()
        binding.sReload.isChecked = Utils.getReload()

        if (Utils.getCalendar()) {
            binding.sOpenCalendar.isChecked = true
            binding.sReload.isChecked = false
            binding.tvDesc.visibility = View.GONE
            Utils.setReload(false)
        }

        if (Utils.getReload()) {
            binding.tvDesc.text = getString(R.string.reload_desc_2)
        }

        when (Utils.getImageType()) {
            ImageTypeEnum.Anime.type -> {
                scaleView(binding.cvDog, true)
                scaleView(binding.cvCat, true)
                scaleView(binding.cvAnime)
            }

            ImageTypeEnum.Dog.type -> {
                scaleView(binding.cvAnime, true)
                scaleView(binding.cvCat, true)
                scaleView(binding.cvDog)
            }

            ImageTypeEnum.Cat.type -> {
                scaleView(binding.cvAnime, true)
                scaleView(binding.cvDog, true)
                scaleView(binding.cvCat)
            }
        }

        binding.sOpenCalendar.setOnClickListener {
            Utils.setCalendar(binding.sOpenCalendar.isChecked)

            if (binding.sOpenCalendar.isChecked) {
                Utils.setReload(false)
                binding.sReload.isChecked = false
                binding.tvDesc.visibility = View.GONE
            }
        }

        binding.sMonth.setOnCheckedChangeListener { _, _ ->
            Utils.setMonth(binding.sMonth.isChecked)
            initDate(binding.sMonth.isChecked)
        }

        binding.cvCat.setOnClickListener {
            scaleView(binding.cvDog, true)
            scaleView(binding.cvAnime, true)
            scaleView(binding.cvCat)
            Utils.setImageType(ImageTypeEnum.Cat.type)
            reloadResolver()
        }

        binding.cvDog.setOnClickListener {
            scaleView(binding.cvCat, true)
            scaleView(binding.cvAnime, true)
            scaleView(binding.cvDog)
            Utils.setImageType(ImageTypeEnum.Dog.type)
            reloadResolver()
        }

        binding.cvAnime.setOnClickListener {
            scaleView(binding.cvCat, true)
            scaleView(binding.cvDog, true)
            scaleView(binding.cvAnime)
            Utils.setImageType(ImageTypeEnum.Anime.type)
            reloadResolver()
        }

        binding.fabInfo.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.sReload.setOnCheckedChangeListener { _, _ ->
            if (binding.sReload.isChecked) {
                Utils.setCalendar(false)
                binding.sOpenCalendar.isChecked = false
            }

            binding.tvDesc.visibility = View.VISIBLE
            Utils.setReload(binding.sReload.isChecked)
            if (binding.sReload.isChecked) {
                binding.tvDesc.text = getString(R.string.reload_desc_2)
            } else {
                binding.tvDesc.text = getString(R.string.reload_desc_1)
            }
        }

        binding.cRestricted.isChecked =
            Utils.getAnimeType() == AnimeImageService.AnimeTypeEnum.NSFW.type

        binding.cRestricted.setOnCheckedChangeListener { _, isChecked ->
            Utils.setAnimeType(
                if (isChecked) {
                    reloadResolver()
                    AnimeImageService.AnimeTypeEnum.NSFW.type
                } else {
                    reloadResolver()
                    AnimeImageService.AnimeTypeEnum.SFW.type
                }
            )
        }

        binding.cCategory.setOnClickListener {
            showSortDialog(applicationContext)
        }
    }

    private fun showSortDialog(context: Context) {
        val options = if (Utils.getAnimeType() == AnimeImageService.AnimeTypeEnum.NSFW.type) {
            AnimeImageService.ImageNsfwCategory.getValuesNames()
        } else {
            AnimeImageService.ImageSfwCategory.getValuesNames()
        }

        val builder = AlertDialog.Builder(this, R.style.MultiChoiceAlertDialog)
        val view = layoutInflater.inflate(R.layout.dialog_custom, null, false)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radiogroup)

        val purpleColor = ContextCompat.getColor(context, R.color.purple_700)
        val radioStyle = ContextThemeWrapper(radioGroup.context, R.style.MyRadioButton)
        for (option in options) {
            val radioButton = RadioButton(radioStyle)
            radioButton.text = option
            radioGroup.addView(radioButton)
        }
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            for (child in radioGroup.children) {
                child as RadioButton
                if (child.id == checkedId) {
                    child.setTextColor(purpleColor)
                    if (Utils.getAnimeType() == AnimeImageService.AnimeTypeEnum.NSFW.type)
                        Utils.setAnimeRestrictedCategory(child.text.toString())
                    else
                        Utils.setAnimeCategory(child.text.toString())
                } else {
                    child.setTextColor(Color.BLACK)
                }
            }
            reloadResolver()
        }
        builder.setView(view)
        builder.show()
    }

    private fun reloadResolver() {
        showState(StateEnum.Loading)

        CoroutineScope(SupervisorJob()).launch {
            when (Utils.getImageType()) {
                ImageTypeEnum.Dog.type -> getDog()
                ImageTypeEnum.Cat.type -> getCat()
                ImageTypeEnum.Anime.type ->
                    Utils.getAnimeType()?.let { type ->
                        Utils.getAnimeRestrictedCategory()?.let { category ->
                            getAnime(type, category)
                        }
                    }
            }
        }
    }

    private fun initDate(isFullMonth: Boolean = false) {
        binding.tvTextDay.text = getDay()
        binding.tvTextMonth.text = getMonth(isFullMonth)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDay(): String {
        val sdf = SimpleDateFormat("dd")
        return sdf.format(Date())
    }

    @SuppressLint("SimpleDateFormat")
    private fun getMonth(isFullMonth: Boolean): String {
        val sdf = if (isFullMonth) SimpleDateFormat("LLLL")
        else SimpleDateFormat("MMM")
        return sdf.format(Date())
    }

    private fun getProgressBar(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    private fun showState(state: StateEnum, mode: ImageTypeEnum = ImageTypeEnum.Cat) {
        when (state) {
            StateEnum.Error -> showErrorState()
            StateEnum.Loading -> showLoadingState()
            StateEnum.Success -> showSuccessState(mode)
        }
    }

    private fun showErrorState() {
        binding.tvTextMonth.visibility = View.GONE
        binding.tvTextDay.visibility = View.GONE
        binding.ivImagePreview.visibility = View.INVISIBLE
        binding.warnImage.visibility = View.VISIBLE
        binding.warnText.visibility = View.VISIBLE
    }

    private fun showLoadingState() {
        binding.pbLoading.show()
        binding.ivImagePreview.visibility = View.INVISIBLE
        binding.warnImage.visibility = View.GONE
        binding.warnText.visibility = View.GONE
        binding.tvTextMonth.visibility = View.GONE
        binding.tvTextDay.visibility = View.GONE
        binding.tvLoading.visibility = View.VISIBLE
    }

    private fun showSuccessState(mode: ImageTypeEnum) {
        binding.pbLoading.hide()
        binding.tvLoading.visibility = View.GONE
        binding.warnImage.visibility = View.GONE
        binding.warnText.visibility = View.GONE
        binding.ivImagePreview.visibility = View.VISIBLE
        binding.tvTextMonth.visibility = View.VISIBLE
        binding.tvTextDay.visibility = View.VISIBLE

        when (mode) {
            ImageTypeEnum.Anime -> {
                binding.cCategory.visibility = View.VISIBLE
                binding.cRestricted.visibility = View.VISIBLE
            }

            else -> {
                binding.cCategory.visibility = View.GONE
                binding.cRestricted.visibility = View.GONE
            }
        }
    }

    private suspend fun getCat() {
        catImageService.getPicture(this).flowOn(Dispatchers.IO).catch { _ ->
            withContext(Dispatchers.Main) {
                showState(StateEnum.Error)
            }
        }.collect {
            Utils.setUrl(it.file)

            withContext(Dispatchers.Main) {
                showState(StateEnum.Success, ImageTypeEnum.Cat)
                setImage(it.file)
            }
        }
    }

    private suspend fun getDog() {
        dogImageService.getPicture(this).flowOn(Dispatchers.IO).catch { e ->
            withContext(Dispatchers.Main) {
                showState(StateEnum.Error)
            }
        }.collect {
            Utils.setUrl(it.message)

            withContext(Dispatchers.Main) {
                showState(StateEnum.Success, ImageTypeEnum.Dog)
                setImage(it.message)
            }
        }
    }

    private suspend fun getAnime(
        type: String = "sfw",
        category: String = "waifu"
    ) {
        animeImageService.getCustomPicture(this, type, category).flowOn(Dispatchers.IO).catch { _ ->
            withContext(Dispatchers.Main) {
                showState(StateEnum.Error)
            }
        }.collect {
            Utils.setUrl(it.url)

            withContext(Dispatchers.Main) {
                showState(StateEnum.Success, ImageTypeEnum.Anime)
                setImage(it.url)
            }
        }
    }

    private fun setImage(imageUrl: String) {
        Glide.with(applicationContext).asDrawable().load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    showState(StateEnum.Error)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

            }).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(getProgressBar())
            .into(binding.ivImagePreview)
    }


    enum class ImageTypeEnum(val type: String) {
        Cat("cat"), Dog("dog"), Anime("anime"),
    }


    enum class StateEnum {
        Error, Loading, Success
    }
}