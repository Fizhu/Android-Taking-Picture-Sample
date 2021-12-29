package com.example.takingpicturesample

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.takingpicturesample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var permissions: ActivityResultLauncher<Array<String>>

    private var PERMISSIONS_CAMERA_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    private var PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    private var cameraUri: Uri? = null

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
        binding.fabGalery.setOnClickListener {
            closeFABMenu()
            openFileChooser()
        }
        binding.fabCamera.setOnClickListener {
            closeFABMenu()
            checkCameraPermission()
        }
        permissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->

            result.forEach { (key, value) ->

                if (value == false) {
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(this, key)

                    if (!showRationale) {//user also CHECKED "never ask again"
                        showPermissionDetails("Permission Title", "Permission Messages") {
                            val packageName: String = packageName
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null)
                            )
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    } else {// user did NOT check "never ask again"
                        showPermissionDetails("Permission Title", "Permission Messages") {
                            permissions.launch(arrayOf(key))
                        }
                    }
                } else {
                    checkCameraPermission()
                }
            }
        }
    }

    private fun showFABMenu() {
        binding.fabCamera.visible()
        binding.fabGalery.visible()
        binding.frameBg.visible()
        binding.fabMain.animate().rotationBy(180F)
        binding.cvCamera.animate().translationY(-resources.getDimension(R.dimen.standard_74))
        binding.cvGalery.animate().translationY(-resources.getDimension(R.dimen.standard_128))
        binding.fabCamera.animate().translationY(-resources.getDimension(R.dimen.standard_74))
        binding.fabGalery.animate().translationY(-resources.getDimension(R.dimen.standard_128))
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    if (View.VISIBLE == binding.frameBg.visibility) {
                        binding.cvCamera.visible()
                        binding.cvGalery.visible()
                    }
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }

            })
    }

    private fun closeFABMenu() {
        binding.frameBg.gone()
        binding.fabCamera.gone()
        binding.fabGalery.gone()
        binding.cvCamera.gone()
        binding.cvGalery.gone()
        binding.fabMain.animate().rotation(0F)
        binding.fabCamera.animate().translationY(0f)
        binding.fabGalery.animate().translationY(0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    if (View.GONE == binding.frameBg.visibility) {
                        binding.cvCamera.gone()
                        binding.cvGalery.gone()
                    }
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }

            })
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                loadImage(cameraUri)
            }
        }

    private val galeryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                loadImage(it.data?.data)
            }
        }

    private fun loadImage(uri: Uri?) {
        Glide.with(this)
            .load(uri)
            .into(binding.iv)
    }

    private fun openCamera() {
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image Desc")
        val imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        cameraUri = imageUri
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        galeryLauncher.launch(Intent.createChooser(intent, "Choose a file"))
    }

    private fun showPermissionDetails(
        title: String,
        messages: String,
        onPositiveClick: () -> Unit
    ) {
        if (!this.isFinishing) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.permission_denied_dialog)
            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setBackgroundDrawableResource(android.R.color.transparent)
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            }
            val tvTitle = dialog.findViewById(R.id.tv_title) as TextView
            val tvMsg = dialog.findViewById(R.id.tv_msg) as TextView
            val btnOkay: Button = dialog.findViewById(R.id.btn_okay) as Button
            val btnCancel: Button = dialog.findViewById(R.id.btn_cancel) as Button
            tvTitle.text = title
            tvMsg.text = messages
            btnOkay.setOnClickListener {
                onPositiveClick.invoke()
                dialog.dismiss()
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermissions(PERMISSIONS)) {
                permissions.launch(PERMISSIONS)
            } else {
                openCamera()
            }
        } else {
            if (!hasPermissions(
                    PERMISSIONS_CAMERA_STORAGE
                )
            ) {
                permissions.launch(PERMISSIONS_CAMERA_STORAGE)
            } else {
                openCamera()
            }
        }
    }

    companion object {
        fun View.gone() {
            visibility = View.GONE
        }

        fun View.visible() {
            visibility = View.VISIBLE
        }

        fun Context.hasPermissions(permissions: Array<String>): Boolean =
            permissions.all {
                ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
    }

}