package com.example.verifieruidai

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.integration.android.IntentIntegrator


class QRActivity : AppCompatActivity() {

    private lateinit var mQrResultLauncher: ActivityResultLauncher<Intent>
    private val TAG = "QRActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qractivity)


        mQrResultLauncher=
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(it.resultCode, it.data)

                    if (result.contents != null) {
                        val QrResponse = result.contents
                        val arr = QrResponse.split(",").toTypedArray()
//                        val dec1 = findViewById<TextView>(R.id.ver)
                        Log.i(TAG, "QR result data string is " + QrResponse.toString())
                        Log.i(TAG, "QR result data array is is " + arr[0] + arr[1] + arr[2] + arr[3])
//                        dec1.text = "Hello!, Resident"+arr[1]
                        if (arr[0] == "UIDAI://"){
                            val resultActivity = Intent(this, ResultActivity::class.java)
                            resultActivity.putExtra("QRData", QrResponse)
                            resultActivity.putExtra("flag", "true")
                            Toast.makeText(this, "Data Recieved Sucessfully", Toast.LENGTH_LONG).show()
                            startActivity(resultActivity)
                        }else {
                            Toast.makeText(this, "QR Code is Wrong", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                }
            }
        startScanner()
    }

    private fun startScanner() {
        val scanner = IntentIntegrator(this)
        // QR Code Format
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        // Set Text Prompt at Bottom of QR code Scanner Activity
        scanner.setPrompt("QR Code Scanner Prompt Text")
        // Start Scanner (don't use initiateScan() unless if you want to use OnActivityResult)
        mQrResultLauncher.launch(scanner.createScanIntent())
    }
}