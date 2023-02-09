package com.example.croppdemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.croppdemo.util.ContextHelper
import com.example.croppdemo.util.GlideEngine
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureSelectionConfig.selectorStyle
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_image_edit_sample.*
import me.minetsh.imaging.IMGCropUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ImageEditSampleActivity : AppCompatActivity() {
    private val mFunction by lazy { intent.getSerializableExtra("function") as? String }
    private var pickUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_edit_sample)

        btn_choose_image.setOnClickListener {
            chooseImages()
        }
    }

    private fun getPhotoFile(): File {
        val dir = Environment.getExternalStorageDirectory()
        val myDir = File(dir, "/Pictures/cropDemo")
        myDir.mkdirs()
        val dirName = "$dir/Pictures/cropDemo"
        return File(dirName, getPhotoFileName())
    }

    // 使用系统当前时间加以调整作为照片的名称
    private fun getPhotoFileName(): String {
        val date = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat(
            "'CROP'_yyyyMMddHHmmSS", Locale.getDefault()
        )
        return dateFormat.format(date) + ".jpg"
    }

    private fun getPathFile(dir: String): File? {
        return ContextHelper.getAppContext().getExternalFilesDir(File.separatorChar + dir)
    }

    fun getCropPhotoFileUri(context: Context): Uri {
        return if (Build.VERSION.SDK_INT < 29) {
            Uri.fromFile(getPhotoFile())
        } else {        //安卓10以上采用了分区存储，使用私有目录来保存裁剪后的图片即可
            val dir = getPathFile("crop") ?: context.filesDir
            dir?.mkdirs()
            Uri.fromFile(File(dir, getPhotoFileName()))
        }
    }

    private fun chooseImages() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setMaxSelectNum(1)
            .setSelectorUIStyle(selectorStyle)//设置动画效果
            .setImageEngine(GlideEngine.createGlideEngine())
            .forResult(resultCallback)
    }

    private var resultCallback: OnResultCallbackListener<LocalMedia> = object :
        OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: java.util.ArrayList<LocalMedia>?) {
            if (result != null && result.size != 0) {
                val localMedia = result[0]
                val thePath = if (localMedia.isCompressed)localMedia.compressPath else localMedia.realPath

                if (TextUtils.isEmpty(thePath)) {
                    return  //路径为空
                }
                val file = File(thePath)
                if (!file.exists()) {//文件不存在
                    return
                }
                Log.d("weekend", "crop src file path=$thePath")
                pickUri = getCropPhotoFileUri(this@ImageEditSampleActivity)

                IMGCropUtil.StartIMGCrop(
                    this@ImageEditSampleActivity,
                    IMGCropUtil.getImageUri(this@ImageEditSampleActivity, file),
                    IMGCropUtil.getInstance().getPath(this@ImageEditSampleActivity, pickUri),
                    mFunction,
                    IMGCropUtil.INTENT_CROP
                )
            }
        }

        override fun onCancel() {}
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
        sdv_image_edit.setImageURI(pickUri, null)
        showTips(IMGCropUtil.getInstance().getPath(this@ImageEditSampleActivity, pickUri));
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IMGCropUtil.INTENT_CROP -> {
                if (resultCode == Activity.RESULT_OK) {
                    onImageEditDone()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}