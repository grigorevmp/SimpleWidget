package com.grigorevmp.catwidget

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        reloadResolver()
        initDate(Utils.getMonthLong())
        initListeners()

    }

    private fun scaleView(view: View, hide: Boolean = false) {
        if (hide) {
            view.animate().scaleX(0.7f).scaleY(0.7f)
        } else {
            view.animate().scaleX(1f).scaleY(1f)
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

        if (Utils.getAnimal() == "dog") {
            scaleView(binding.cvCat, true)
            scaleView(binding.cvDog)
        } else {
            scaleView(binding.cvCat)
            scaleView(binding.cvDog, true)
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
            scaleView(binding.cvCat)
            scaleView(binding.cvDog, true)
            Utils.setAnimal(ImageTypeEnum.Cat.type)
            reloadResolver()
        }

        binding.cvDog.setOnClickListener {
            scaleView(binding.cvCat, true)
            scaleView(binding.cvDog)
            Utils.setAnimal(ImageTypeEnum.Dog.type)
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
    }

    private fun reloadResolver() {
        CoroutineScope(SupervisorJob()).launch {
            when (Utils.getAnimal()) {
                ImageTypeEnum.Dog.type -> getDog()
                ImageTypeEnum.Cat.type -> getCat()
                else -> getCat()
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

    private fun showState(state: StateEnum) {
        when (state) {
            StateEnum.Error -> showErrorState()
            StateEnum.Loading -> showLoadingState()
            StateEnum.Success -> showSuccessState()
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

    private fun showSuccessState() {
        binding.pbLoading.hide()
        binding.tvLoading.visibility = View.GONE
        binding.warnImage.visibility = View.GONE
        binding.warnText.visibility = View.GONE
        binding.ivImagePreview.visibility = View.VISIBLE
        binding.tvTextMonth.visibility = View.VISIBLE
        binding.tvTextDay.visibility = View.VISIBLE
    }

    private suspend fun getCat() {
        withContext(Dispatchers.Main) {
            showState(StateEnum.Loading)
        }

        catImageService.getPicture(this).flowOn(Dispatchers.IO).catch { _ ->
            withContext(Dispatchers.Main) {
                showState(StateEnum.Error)
            }
        }.collect {
            Utils.setUrl(it.file)

            withContext(Dispatchers.Main) {
                showState(StateEnum.Success)
                setImage(it.file)
            }
        }
    }

    private suspend fun getDog() {
        withContext(Dispatchers.Main) {
            showState(StateEnum.Loading)
        }

        dogImageService.getPicture(this).flowOn(Dispatchers.IO).catch { e ->
            withContext(Dispatchers.Main) {
                showState(StateEnum.Error)
            }
        }.collect {
            Utils.setUrl(it.message)

            withContext(Dispatchers.Main) {
                showState(StateEnum.Success)
                setImage(it.message)
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
        Cat("cat"),
        Dog("dog")
    }



    enum class StateEnum() {
        Error, Loading, Success
    }
}