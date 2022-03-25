package com.github.windsekirun.playwithksp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.github.windsekirun.playwithksp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViews()
        setUpObservers()

        viewModel.init()
    }

    private fun setUpViews() {

    }

    private fun setUpObservers() {
        viewModel.errorData.observe(this) {
            MaterialDialog(this).show {
                title(text = "error happened")
                message(text = it.message)
                positiveButton {  }
            }
        }
    }
}