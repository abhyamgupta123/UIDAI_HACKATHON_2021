package com.example.verifieruidai

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import net.lingala.zip4j.ZipFile
import org.json.JSONObject
import util.StateReq
import util.StateResp
import java.io.File
import java.util.*

class ResultActivity : AppCompatActivity() {

    private val TAG = "ResultActivity"
    var qrDataArray = arrayOf("startdata")


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val qrData = intent.getStringExtra("QRData")
        qrDataArray = qrData?.split(",")?.toTypedArray() ?: emptyArray()
        val flag = intent.getStringExtra("flag")
        if (flag=="true"){
            // getting the username and event id data
            val username = qrDataArray[1]
            val eventId = qrDataArray[2]

            // setting the data to appropriate place in UI
            val statelessTv = findViewById<View>(R.id.statelessText) as TextView
            val eventIdTv = findViewById<View>(R.id.eventid) as TextView
            statelessTv.text = "Verify " + username + " with"
            eventIdTv.text = "Event ID:- " + eventId
//            getEncodedData(qrDataArray[3])
        }
    }

    fun loadheadless(msg2: String) {
        val sendIntent = Intent()
        sendIntent.action = "in.gov.uidai.rdservice.face.STATELESS_MATCH"

        val statelessMatchRequest = StateReq()
        statelessMatchRequest.requestId = "qwe123"
        statelessMatchRequest.signedDocument = msg2
        statelessMatchRequest.language = "en"
        statelessMatchRequest.enableAutoCapture = "true"

        Log.d("rrrr", statelessMatchRequest.toXml())
        sendIntent.putExtra("request", statelessMatchRequest.toXml())
        Log.d(TAG,  msg2)
        if (sendIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(sendIntent, 123)
        } else {
            Log.d(TAG, "opssssssssssssssss")
        }
//        val tv = findViewById<View>(R.id.check) as TextView
//        tv.text = "Welcome to android"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getEncodedData(url: String) {
        val queue = Volley.newRequestQueue(this)
        val url = url

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val responseObj = JSONObject(response)
                    val status = responseObj.getString("status")
                    if (status=="OK"){
                        val data = responseObj.getJSONObject ("file").getString("data")
                        generator(data)
                        val filepath  = getExternalFilesDir(null)
                        val fullPath = filepath.toString() + "/KYC/offlineaadhaar20211028024757269.xml"
                        Log.e(TAG, "full path is "+ fullPath)
                        val xmlString = readFileDirectlyAsText(fullPath)
                        Log.e(TAG, "=====>>>>> XML DATA IS "+ xmlString + " |=============================================|")
                        Log.d(TAG, "===>>>data is " + data)
                        loadheadless(xmlString)
                    }else{
                        Toast.makeText(this, "KYC is Expired", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    // caught while parsing the response
                    Log.e(TAG, "problem occurred")
                    e.printStackTrace()
                }
//                Log.d(TAG, "response recieved sucessfully" + response.)
//                loadheadless(fin)
            },
            {
                    err ->
                Toast.makeText(this, "Some error occured while loading data from server", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error occured while fetching encoded string url" + err)
            }
        )

        queue.add(stringRequest)
    }

    fun readFileDirectlyAsText(fileName: String): String {
        return File(fileName).readText(Charsets.UTF_8)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val STATELESS_MATCH_REQ_CODE = 123
        val DOCUMENT_PICKER_REQ_CODE = 124
        if (resultCode == Activity.RESULT_OK && null != data) {
            Log.d("apiiiiiii", data.getStringExtra("response").toString())
            val resp = StateResp.fromXML(data.getStringExtra("response"))
            Log.d("resp", resp.toString())
//            val tv = findViewById<View>(R.id.check) as TextView
//            tv.text = data.getStringExtra("response")
            val finalResult = Intent(this, FinalResultActivity::class.java)
            if (resp.errCode==0){
                finalResult.putExtra("flag", "1")
                finalResult.putExtra("msg", "Match Verified for " + qrDataArray[2])
            }else{
                finalResult.putExtra("flag", "0")
                finalResult.putExtra("msg", resp.errInfo)
            }
            startActivity(finalResult)
            Toast.makeText(this, resp.errInfo, Toast.LENGTH_LONG).show()
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun decodingString(view: android.view.View) {
//        val u = "https://storage.abhis.me/file/04960cbe2c5647f3a8dc475f23a85944"
        getEncodedData(qrDataArray[3])
//        getEncodedData(u)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decode(en: String): ByteArray? {
        val decodedBytes = Base64.getDecoder().decode(en)
        return decodedBytes
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generator(encodedDataString: String){
        val out  = decode(encodedDataString)
        if (out != null) {
            zipcreate(out)
        }
        Log.e(TAG, "Unzipped is caeed now=============>>>>>>>>>>>>")
        unzip("3112")
    }

    fun zipcreate(bt: ByteArray) {
        val path = getExternalFilesDir(null)
        val letDirectory = File(path, "KYC")
        letDirectory.mkdirs()
        val file = File(letDirectory, "kyc.zip")
        file.writeBytes(bt)

        println("Written to file")
    }

    fun unzip(pass: String){
        val path1  = getExternalFilesDir(null)
        val letDirectory = File(path1, "KYC")
        val file = File(letDirectory, "kyc.zip")
        val zipFile = ZipFile(file,pass.toCharArray())
        zipFile.extractAll(path1.toString() + "/KYC/")
    }
}