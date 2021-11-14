package com.example.verifieruidai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class FinalResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_result)

        val flag = intent.getStringExtra("flag")
        val msg = intent.getStringExtra("msg")

        val msgtv = findViewById<View>(R.id.msgTV) as TextView
        val verifiedImg = findViewById<View>(R.id.imageViewRight) as ImageView
        val notverifiedImg = findViewById<View>(R.id.imageViewWrong) as ImageView

        if (flag == "1") {
            verifiedImg.visibility = View.VISIBLE
            notverifiedImg.visibility = View.INVISIBLE
        }else{
            verifiedImg.visibility = View.INVISIBLE
            notverifiedImg.visibility = View.VISIBLE
        }
        msgtv.text = msg
    }

    fun startActivity(view: android.view.View) {
        startActivity(Intent(this, MainActivity::class.java))
    }
}