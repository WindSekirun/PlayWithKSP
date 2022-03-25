package com.github.windsekirun.playwithksp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.github.windsekirun.playwithksp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setUpViews()
        setUpObservers()
    }

    private fun setUpViews() {

    }

    private fun setUpObservers() {
        viewModel.savedData.observe(this) {
            MaterialDialog(this).show {
                title(text = "success")
                message(text = it)
                positiveButton { }
            }
        }

        viewModel.errorData.observe(this) {
            MaterialDialog(this).show {
                title(text = "error happened")
                message(text = it.message)
                positiveButton { }
            }
        }
    }
}