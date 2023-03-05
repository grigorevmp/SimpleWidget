package com.grigorevmp.catwidget

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.grigorevmp.catwidget.data.network.anime.AnimeImageService
import com.grigorevmp.catwidget.databinding.ActivityMainBinding
import com.grigorevmp.catwidget.utils.Preferences
import com.grigorevmp.catwidget.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val vm = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        reloadResolver()
        initDate(Preferences.showFullMonth)
        initListeners()
    }

    private fun initListeners() {
        binding.fabReload.setOnClickListener {
            reloadResolver()
        }

        binding.sMonth.isChecked = Preferences.showFullMonth
        binding.sReload.isChecked = Preferences.fullReloadOnTap

        if (Preferences.openCalendarOnTap) {
            binding.sOpenCalendar.isChecked = true
            binding.sReload.isChecked = false
            Preferences.fullReloadOnTap = false
        }

        if (Preferences.fullReloadOnTap) {
            binding.tvDesc.text = getString(R.string.reload_desc_2)
        }

        when (Preferences.pictureType) {
            Utils.ImageTypeEnum.Anime.type -> {
                Utils.viewsScaleInOut(
                    viewsToScaleIn = listOf(binding.cvAnime),
                    viewsToScaleOut = listOf(binding.cvDog, binding.cvCat)
                )
            }

            Utils.ImageTypeEnum.Dog.type -> {
                Utils.viewsScaleInOut(
                    viewsToScaleIn = listOf(binding.cvDog),
                    viewsToScaleOut = listOf(binding.cvAnime, binding.cvCat)
                )
            }

            Utils.ImageTypeEnum.Cat.type -> {
                Utils.viewsScaleInOut(
                    viewsToScaleIn = listOf(binding.cvCat),
                    viewsToScaleOut = listOf(binding.cvAnime, binding.cvDog)
                )
            }
        }

        binding.sOpenCalendar.setOnClickListener {
            Preferences.openCalendarOnTap = binding.sOpenCalendar.isChecked

            if (binding.sOpenCalendar.isChecked) {
                Preferences.fullReloadOnTap = false
                binding.sReload.isChecked = false
            }
        }

        binding.sMonth.setOnCheckedChangeListener { _, _ ->
            Preferences.showFullMonth = binding.sMonth.isChecked
            initDate(binding.sMonth.isChecked)
        }

        binding.cvCat.setOnClickListener {
            Utils.viewsScaleInOut(
                viewsToScaleIn = listOf(binding.cvCat),
                viewsToScaleOut = listOf(binding.cvAnime, binding.cvDog)
            )
            Preferences.pictureType = Utils.ImageTypeEnum.Cat.type
            reloadResolver()
        }

        binding.cvDog.setOnClickListener {
            Utils.viewsScaleInOut(
                viewsToScaleIn = listOf(binding.cvDog),
                viewsToScaleOut = listOf(binding.cvCat, binding.cvAnime)
            )
            Preferences.pictureType = Utils.ImageTypeEnum.Dog.type
            reloadResolver()
        }

        binding.cvAnime.setOnClickListener {
            Utils.viewsScaleInOut(
                viewsToScaleIn = listOf(binding.cvAnime),
                viewsToScaleOut = listOf(binding.cvCat, binding.cvDog)
            )
            Preferences.pictureType = Utils.ImageTypeEnum.Anime.type
            reloadResolver()
        }

        binding.fabInfo.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.sReload.setOnCheckedChangeListener { _, _ ->
            if (binding.sReload.isChecked) {
                Preferences.openCalendarOnTap = false
                binding.sOpenCalendar.isChecked = false
            }

            Preferences.fullReloadOnTap = binding.sReload.isChecked
            if (binding.sReload.isChecked) {
                binding.tvDesc.text = getString(R.string.reload_desc_2)
            } else {
                binding.tvDesc.text = getString(R.string.reload_desc_1)
            }
        }

        binding.cRestricted.isChecked =
            Preferences.animeImageType == AnimeImageService.AnimeTypeEnum.NSFW.type

        binding.cRestricted.setOnCheckedChangeListener { _, isChecked ->
            Preferences.animeImageType = (
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
            showSortDialog()
        }
    }

    private fun showSortDialog() {
        val options: Array<CharSequence> = if (Preferences.animeImageType == AnimeImageService.AnimeTypeEnum.NSFW.type) {
            AnimeImageService.ImageNsfwCategory.getValuesNames().toTypedArray()
        } else {
            AnimeImageService.ImageSfwCategory.getValuesNames().toTypedArray()
        }

        val selectedId = options.indexOf(Preferences.animeImageCategory as CharSequence)

        val builder2 = MaterialAlertDialogBuilder(this@MainActivity)
        builder2.setTitle(R.string.anime_categories)
        builder2.setSingleChoiceItems(options, selectedId) { dialog, which ->
            if (Preferences.animeImageType == AnimeImageService.AnimeTypeEnum.NSFW.type)
                Preferences.animeImageRestrictedCategory = options[which].toString()
            else
                Preferences.animeImageCategory = options[which].toString()
            reloadResolver()
            dialog.cancel()
        }

        builder2.setNegativeButton(getString(R.string.dismiss)) { dialog, _ -> dialog.dismiss() }
        builder2.setCancelable(true)
        builder2.show()
    }

    private fun reloadResolver() {
        showState(StateEnum.Loading)

        CoroutineScope(SupervisorJob()).launch {
            when (Preferences.pictureType) {
                Utils.ImageTypeEnum.Dog.type -> getDog()
                Utils.ImageTypeEnum.Cat.type -> getCat()
                Utils.ImageTypeEnum.Anime.type -> getAnime(Preferences.animeImageType, Preferences.animeImageCategory)
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

        return sdf.format(Date()).capitalized()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getMonth(isFullMonth: Boolean): String {
        val sdf = if (isFullMonth) SimpleDateFormat("LLLL") else SimpleDateFormat("MMM")
        return sdf.format(Date()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    private fun getProgressBar(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(this)

        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        return circularProgressDrawable
    }

    private fun showState(state: StateEnum, mode: Utils.ImageTypeEnum = Utils.ImageTypeEnum.Cat) {
        when (state) {
            StateEnum.Error -> showErrorState()
            StateEnum.Loading -> showLoadingState()
            StateEnum.Success -> showSuccessState(mode)
        }
    }

    private fun showErrorState() {
        Utils.viewsGoneAndShow(
            viewsToGone = listOf(binding.tvTextMonth, binding.tvTextDay),
            viewsToHide = listOf(binding.ivImagePreview),
            viewsToShow = listOf(binding.warnImage, binding.warnText),
        )
    }

    private fun showLoadingState() {
        binding.pbLoading.show()

        Utils.viewsGoneAndShow(
            viewsToGone = listOf(binding.warnImage, binding.warnText, binding.tvTextMonth, binding.tvTextDay),
            viewsToHide = listOf(binding.ivImagePreview),
            viewsToShow = listOf(binding.tvLoading),
        )
    }

    private fun showSuccessState(mode: Utils.ImageTypeEnum) {
        binding.pbLoading.hide()

        Utils.viewsGoneAndShow(
            viewsToGone = listOf(binding.tvLoading, binding.warnImage, binding.warnText),
            viewsToShow = listOf(binding.ivImagePreview, binding.tvTextDay, binding.tvTextMonth),
        )

        when (mode) {
            Utils.ImageTypeEnum.Anime -> {
                Utils.viewsGoneAndShow(viewsToShow = listOf(binding.cCategory))
                if (BuildConfig.showRestrictedContent) {
                    Utils.viewsGoneAndShow(viewsToShow = listOf(binding.cRestricted))
                }
            }

            else -> {
                Utils.viewsGoneAndShow(
                    viewsToGone = listOf(binding.cCategory, binding.cRestricted),
                )
            }
        }
    }

    private suspend fun getCat() {
        vm.getPictureByTypeAndChangeState(
            this,
            MainViewModel.PictureType.Cat,
            errorAction = { showState(StateEnum.Error) },
            successAction = { file ->
                showState(StateEnum.Success, Utils.ImageTypeEnum.Cat)
                setImage(file)
            }
        )
    }

    private suspend fun getDog() {
        vm.getPictureByTypeAndChangeState(
            this,
            MainViewModel.PictureType.Dog,
            errorAction = { showState(StateEnum.Error) },
            successAction = { file ->
                showState(StateEnum.Success, Utils.ImageTypeEnum.Dog)
                setImage(file)
            }
        )
    }

    private suspend fun getAnime(
        type: String = AnimeImageService.AnimeTypeEnum.SFW.type,
        category: String = AnimeImageService.ImageSfwCategory.Waifu.category
    ) {
        vm.getPictureByTypeAndChangeState(
            this,
            MainViewModel.PictureType.Anime,
            errorAction = { showState(StateEnum.Error) },
            successAction = { file ->
                showState(StateEnum.Success, Utils.ImageTypeEnum.Anime)
                setImage(file)
            },
            type
        )
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



    enum class StateEnum {
        Error, Loading, Success
    }



    // Extensions

    private fun String.capitalized(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.getDefault())
            }
            else {
                it.toString()
            }
        }
    }
}