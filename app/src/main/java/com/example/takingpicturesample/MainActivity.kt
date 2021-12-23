package com.example.takingpicturesample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.takingpicturesample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onInit()
    }

    private fun onInit() {
        binding.fabMain.setOnClickListener {
            if (View.GONE == binding.frameBg.visibility) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }
        binding.frameBg.setOnClickListener {
            closeFABMenu()
        }
    }

    private fun showFABMenu() {
        binding.fabCamera.visible()
        binding.fabGalery.visible()
        binding.frameBg.visible()
        binding.fabMain.animate().rotationBy(180F)
        binding.fabCamera.animate().translationY(-resources.getDimension(R.dimen.standard_74))
        binding.fabGalery.animate().translationY(-resources.getDimension(R.dimen.standard_128))
    }

    private fun closeFABMenu() {
        binding.frameBg.gone()
        binding.fabCamera.gone()
        binding.fabGalery.gone()
        binding.fabMain.animate().rotation(0F)
        binding.fabCamera.animate().translationY(0f)
        binding.fabGalery.animate().translationY(0f)
    }

    companion object {
        fun View.gone() {
            visibility = View.GONE
        }

        fun View.visible() {
            visibility = View.VISIBLE
        }
    }

}