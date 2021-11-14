package com.example.verifieruidai

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import util.StateReq
import util.StateResp
import java.io.File
import java.util.*
import net.lingala.zip4j.ZipFile

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    var qrDataArray = arrayOf("startdata")
    private lateinit var mQrResultLauncher: ActivityResultLauncher<Intent>


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openQRActivity(view: android.view.View) {
        startActivity(Intent(this, QRActivity::class.java))
    }
}