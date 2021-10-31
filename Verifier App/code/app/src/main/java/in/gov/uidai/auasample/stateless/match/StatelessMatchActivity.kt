package `in`.gov.uidai.auasample.stateless.match

import `in`.gov.uidai.auasample.R
import `in`.gov.uidai.auasample.input.contract.StatelessMatchRequest
import `in`.gov.uidai.auasample.input.contract.StatelessMatchResponse
import `in`.gov.uidai.auasample.input.contract.ekyc.OfflineEkyc
import `in`.gov.uidai.auasample.input.views.RegisterRequestBuilderDialogFragment
import `in`.gov.uidai.auasample.utils.Utils
import `in`.gov.uidai.auasample.utils.readAsText
import `in`.gov.uidai.auasample.utils.readEKYCData
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_stateless_match.*
import kotlinx.android.synthetic.main.dialog_register_request.edtTransactionId
import kotlinx.android.synthetic.main.dialog_register_request.ekycDocumentSpinner
import kotlinx.android.synthetic.main.dialog_register_request.ivUserImage
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class StatelessMatchActivity : AppCompatActivity() {

    private lateinit var dataAdapter: ArrayAdapter<String>

    private lateinit var mQrResultLauncher: ActivityResultLauncher<Intent>

    private val executors = Executors.newFixedThreadPool(1)

    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastReadEKYCDocument: String? = null

    private val client = OkHttpClient()

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, StatelessMatchActivity::class.java))
        }

        const val ACTION = "in.gov.uidai.rdservice.face.STATELESS_MATCH"
        const val REQUEST = "request"
        const val RESPONSE = "response"
        const val STATELESS_MATCH_REQ_CODE = 123
        const val DOCUMENT_PICKER_REQ_CODE = 124
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stateless_match)

        supportActionBar?.title = getString(R.string.title_stateless_match)

//        setupEKYCDropDown()

        edtTransactionId.setText(Utils.getTransactionID())

        btnPerformStatelessMatch.setOnClickListener { launchStatelessMatch() }

        btnDone.setOnClickListener { onBackPressed() }

        tvEkycDocumentSelector.setOnClickListener { launchDocumentPicker() }

        mQrResultLauncher=
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(it.resultCode, it.data)

                    if (result.contents != null) {
                        val a = result.contents
//                        val dec1 = findViewById<TextView>(R.id.ver)
//                        a.toString()
                        val arr = a.split(",").toTypedArray()
//                        dec1.text = "Hello!, Resident"+arr[1]
                        if (arr[0] == "UIDAI//"){
                            Log.d("TAG", "Successfull")
                            Toast.makeText(applicationContext, "QR VERIFIED SUCESSFULLY", Toast.LENGTH_LONG).show()
                            run(arr[3])
                        }else {

                        }
                    }
                }
            }

        startScanner()
    }

//    fun responseBody(){
//        body.
//    }

    fun run(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response){
                Log.e("TAG", "SOMETHING")
                Log.e("TAG", "MY DATA" + response.body().toString())
                Toast.makeText(applicationContext, response.body().toString(), Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun launchStatelessMatch() {
        val statelessMatchRequest = StatelessMatchRequest()
        statelessMatchRequest.requestId = edtTransactionId.text.toString()
        statelessMatchRequest.signedDocument = lastReadEKYCDocument
        statelessMatchRequest.language = Utils.LANGUAGE
        statelessMatchRequest.enableAutoCapture = Utils.ENABLE_AUTO_CAPTURE.toString()
        Log.e("MY LOG", "==========>>>" + statelessMatchRequest.toXml())
        val intent = Intent(ACTION).apply {
            putExtra(REQUEST, statelessMatchRequest.toXml())
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, STATELESS_MATCH_REQ_CODE)
        } else {
            showToast(R.string.error_face_rd_plus_not_installed)
        }
    }

    private fun launchDocumentPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, DOCUMENT_PICKER_REQ_CODE)
        else
            showToast(R.string.error_file_mgr_not_installed)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && null != data) {
            if (requestCode == STATELESS_MATCH_REQ_CODE) {
                handleMatchResponse(StatelessMatchResponse.fromXML(data.getStringExtra(RESPONSE)))
            } else if (requestCode == DOCUMENT_PICKER_REQ_CODE) {
                data.data?.let { uri ->
                    onEKYDocumentSelected(uri)
                }
            }
        }
    }

    private fun getAssetManager(): AssetManager {
        return assets!!
    }

    private fun setupEKYCDropDown() {
        val docs = getAssetManager().list(RegisterRequestBuilderDialogFragment.EKYC_DOC_PATH)
        docs?.sort()

        dataAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, docs!!
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        ekycDocumentSpinner.adapter = dataAdapter

        ekycDocumentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View, position: Int, id: Long
            ) {
                onEKYDocumentSelected(dataAdapter.getItem(position)!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun onEKYDocumentSelected(docUri: Uri) {
        executors.execute {
            lastReadEKYCDocument = docUri.readAsText(contentResolver)

            validateEkycDocumentAndUpdateUserImage()

            mainHandler.post {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                    if (lastReadEKYCDocument == null) {
                        tvEkycDocumentName.text = null
                        showToast(R.string.error_invalid_ekyc_file)
                    } else
                        tvEkycDocumentName.text = Utils.queryName(contentResolver, docUri)
                }
            }
        }
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

    private fun onEKYDocumentSelected(docName: String) {
        //Document may take time to load, during loading don't give user the option to submit the details
        btnPerformStatelessMatch.isEnabled = false
        executors.execute {
            lastReadEKYCDocument =
                getAssetManager().readEKYCData(RegisterRequestBuilderDialogFragment.EKYC_DOC_PATH + File.separator + docName)

            validateEkycDocumentAndUpdateUserImage()
        }
    }

    @WorkerThread
    private fun validateEkycDocumentAndUpdateUserImage() {
        lastReadEKYCDocument?.let {
            val ekyc = convertEKYCStringToModel(lastReadEKYCDocument)

            ekyc?.let {
                mainHandler.post {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        ivUserImage.setImageBitmap(Utils.convertToBitmap(ekyc.uidData.pht))
                        btnPerformStatelessMatch.isEnabled = true
                    }
                }
            }
        } ?: kotlin.run {
            mainHandler.post {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                    ivUserImage.setImageBitmap(null)
                    btnPerformStatelessMatch.isEnabled = false
                }
            }
        }
    }

    private fun convertEKYCStringToModel(ekyc: String?): OfflineEkyc? {
        return try {
            OfflineEkyc.fromXML(ekyc)
        } catch (e: Exception) {
            null
        }
    }

    private fun handleMatchResponse(response: StatelessMatchResponse) {
        if (response.isSuccess) {
            setSuccessResponse("Match successful for transaction - ${response.requestId}")
        } else {
            setErrorResponse("Match failed for transaction - ${response.requestId} with error : ${response.errInfo}.")
        }
    }

    //Use this method to show success response
    private fun setSuccessResponse(message: String) {
        inputContainer.visibility = View.GONE
        statelessMatchResponse.visibility = View.VISIBLE

        imgResponseStatus.setImageResource(R.drawable.ic_success)
        responseText.text = message
    }

    //Use this method to show error response
    private fun setErrorResponse(message: String) {
        inputContainer.visibility = View.GONE
        statelessMatchResponse.visibility = View.VISIBLE

        imgResponseStatus.setImageResource(R.drawable.ic_fail)
        responseText.text = message
    }

    private fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }
}