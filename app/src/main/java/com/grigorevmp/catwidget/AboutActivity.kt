package com.grigorevmp.catwidget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grigorevmp.catwidget.databinding.ActivityAboutBinding

const val authorTelegramLink = "https://t.me/grigorevmp"
const val authorGitHubLink = "https://github.com/grigorevmp"

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.ivTelegram.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(authorTelegramLink)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }

        binding.cvGitHub.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(authorGitHubLink)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }
    }
}