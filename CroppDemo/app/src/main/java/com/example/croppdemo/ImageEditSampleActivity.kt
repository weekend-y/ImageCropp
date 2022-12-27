package com.example.croppdemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_image_edit_sample.*
import me.minetsh.imaging.IMGEditActivity
import me.minetsh.imaging.IMGGalleryActivity
import me.minetsh.imaging.gallery.model.IMGChooseMode
import me.minetsh.imaging.gallery.model.IMGImageInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ImageEditSampleActivity : AppCompatActivity() {
    private val mFunction by lazy { intent.getSerializableExtra("function") as? String }
    private var mImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_edit_sample)

        btn_choose_image.setOnClickListener {
            chooseImages()
        }
    }

    // 使用系统当前时间加以调整作为照片的名称
    private fun getPhotoFile(): File {
        val dir = Environment.getExternalStorageDirectory()
        val myDir = File(dir, "/Pictures/cropDemo")
        myDir.mkdirs()
        val dirName = "$dir/Pictures/cropDemo"
        return File(dirName, getPhotoFileName())
    }

    private fun getPhotoFileName(): String {
        val date = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat(
            "'CROP'_yyyyMMddHHmmSS", Locale.getDefault()
        )
        return dateFormat.format(date) + ".jpg"
    }

    private fun chooseImages() {
        startActivityForResult(
            IMGGalleryActivity.newIntent(
                this, IMGChooseMode.Builder()
                    .setSingleChoose(true)
                    .build()
            ),
            REQ_IMAGE_CHOOSE
        )
    }

    private fun onChooseImages(images: List<IMGImageInfo>?) {
        val image = images?.get(0)
        if (image != null) {

            sdv_image.setImageURI(image.uri, null)

            mImageFile = getPhotoFile();

            startActivityForResult(
                Intent(this, IMGEditActivity::class.java)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, image.uri)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_FUNCTION, mFunction)
                    .putExtra(
                        IMGEditActivity.EXTRA_IMAGE_SAVE_PATH,
                        mImageFile?.absolutePath
                    ),
                REQ_IMAGE_EDIT
            )
        }
    }

    private fun showTips(path: String?) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("图片已保存到$path")
            .setPositiveButton(
                "好的"
            ) { dialog, which -> dialog.dismiss() }.create()
        dialog.show()
    }

    private fun onImageEditDone() {
        sdv_image_edit.setImageURI(Uri.fromFile(mImageFile), null)
        showTips(mImageFile?.absolutePath);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_IMAGE_CHOOSE -> {
                if (resultCode == Activity.RESULT_OK) {
                    onChooseImages(IMGGalleryActivity.getImageInfos(data))
                }
            }
            REQ_IMAGE_EDIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    onImageEditDone()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}