package com.grigorevmp.catwidget

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.grigorevmp.catwidget.data.dto.CatImageDto
import com.grigorevmp.catwidget.data.dto.DogImageDto
import com.grigorevmp.catwidget.data.network.CatImageService
import com.grigorevmp.catwidget.data.network.DogImageService
import com.grigorevmp.catwidget.databinding.ActivityMainBinding
import com.grigorevmp.catwidget.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

            if(binding.sOpenCalendar.isChecked){
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
            Utils.setAnimal("cat")
            reloadResolver()
        }

        binding.cvDog.setOnClickListener {
            scaleView(binding.cvCat, true)
            scaleView(binding.cvDog)
            Utils.setAnimal("dog")
            reloadResolver()
        }

        binding.fabInfo.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.sReload.setOnCheckedChangeListener { _, _ ->
            if(binding.sReload.isChecked){
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
        if (Utils.getAnimal() == "dog") {
            getDog()
        } else {
            getCat()
        }
    }

    private fun initDate(isFullMonth: Boolean = false) {
        val dayText = findViewById<TextView>(R.id.tvTextDay)
        val monthText = findViewById<TextView>(R.id.tvTextMonth)

        dayText.text = getDay()
        monthText.text = getMonth(isFullMonth)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDay(): String {
        val sdf = SimpleDateFormat("dd")
        return sdf.format(Date())
    }

    @SuppressLint("SimpleDateFormat")
    fun getMonth(isFullMonth: Boolean): String {
        val sdf =
            if (isFullMonth) SimpleDateFormat("LLLL")
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

    private fun showErrorState() {
        binding.warnImage.visibility = View.VISIBLE
        binding.warnText.visibility = View.VISIBLE
        binding.ivImagePreview.visibility = View.INVISIBLE
        binding.tvTextMonth.visibility = View.GONE
        binding.tvTextDay.visibility = View.GONE
    }

    private fun showLoadingState() {
        binding.tvLoading.visibility = View.VISIBLE
        binding.ivImagePreview.visibility = View.INVISIBLE
        binding.warnImage.visibility = View.GONE
        binding.warnText.visibility = View.GONE
        binding.tvTextMonth.visibility = View.GONE
        binding.tvTextDay.visibility = View.GONE
        binding.pbLoading.show()
    }

    private fun showFinalState() {
        binding.tvLoading.visibility = View.GONE
        binding.ivImagePreview.visibility = View.VISIBLE
        binding.warnImage.visibility = View.GONE
        binding.warnText.visibility = View.GONE
        binding.tvTextMonth.visibility = View.VISIBLE
        binding.tvTextDay.visibility = View.VISIBLE
        binding.pbLoading.hide()
    }

    private fun getCat() {
        showLoadingState()

        catImageService.getPicture(this).enqueue(object : Callback<CatImageDto> {
            override fun onFailure(call: Call<CatImageDto>, t: Throwable) {
                showErrorState()
            }


            override fun onResponse(call: Call<CatImageDto>, response: Response<CatImageDto>) {
                showFinalState()
                response.body()?.file?.let { Utils.setUrl(it) }

                Glide.with(applicationContext)
                    .load(response.body()?.file)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            showErrorState()
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

                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(getProgressBar())
                    .into(binding.ivImagePreview)
            }
        })
    }

    private fun getDog() {
        showLoadingState()

        dogImageService.getPicture(this).enqueue(object : Callback<DogImageDto> {
            override fun onFailure(call: Call<DogImageDto>, t: Throwable) {
                showErrorState()
            }


            override fun onResponse(call: Call<DogImageDto>, response: Response<DogImageDto>) {
                showFinalState()

                response.body()?.message?.let { Utils.setUrl(it) }

                Glide.with(applicationContext)
                    .asDrawable()
                    .load(response.body()?.message)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            showErrorState()
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

                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(getProgressBar())
                    .into(binding.ivImagePreview)
            }
        })
    }
}