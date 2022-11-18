package com.example.croppdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

const val REQ_IMAGE_EDIT = 1

const val REQ_IMAGE_CHOOSE = 2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ll_pic_edit.setOnClickListener {
            startActivity(Intent(
                this, ImageEditSampleActivity::class.java).apply {
                    putExtra("function", "edit")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }

        ll_pic_crop.setOnClickListener {
            startActivity(Intent(
                this, ImageEditSampleActivity::class.java).apply {
                putExtra("function", "crop")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            )
        }
    }
}
