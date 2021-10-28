package com.resident.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private IResult mResultCallback = null;
    private api_methods method;

    private ImageView captchafield;
    private TextInputLayout captchaStringField;
    private TextInputLayout aadharNumberField;
    private Button captchaButton;
    private Button getOtpButton;
    private Button refreshcaptchaButton;

    private String captcha_generation_url = "https://stage1.uidai.gov.in/unifiedAppAuthService/api/v2/get/captcha";
    private String otp_generation_url = "https://stage1.uidai.gov.in/unifiedAppAuthService/api/v2/generate/aadhaar/otp";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVolleyCallback();

        // Finding fields for further process.
        captchafield = (ImageView) findViewById(R.id.captchaImage);
        captchaStringField = (TextInputLayout) findViewById(R.id.captastring);
        aadharNumberField = (TextInputLayout) findViewById(R.id.aadharNumber);
        captchaButton = (Button) findViewById(R.id.genrateCaptcha);
        getOtpButton = (Button) findViewById(R.id.getOtpButton);
        refreshcaptchaButton = (Button) findViewById(R.id.refreshCaptcha);

        // Initialising API methods class before.
        method = callingApiFunction();

        captchaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String aadharNumber = aadharNumberField.getEditText().getText().toString().trim();

                method.generate_captcha("CAPTCHA", TAG, captcha_generation_url);
            }
        });
    }

    api_methods callingApiFunction(){
        return new api_methods(mResultCallback, MainActivity.this);
    }

    void initVolleyCallback(){
        mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                if (requestType.contains("CAPTCHA")){
                    try {
                        Log.i(TAG, "Response For Captcha Recieved Successfully");
//                        Log.i(TAG, response.toString());
                        int statusCode = response.getInt("statusCode");
                        if (statusCode == 200){

                            // setting visibility of all views
                            captchafield.setVisibility(View.VISIBLE);
                            captchaStringField.setVisibility(View.VISIBLE);
                            aadharNumberField.setVisibility(View.VISIBLE);
                            getOtpButton.setVisibility(View.VISIBLE);
                            refreshcaptchaButton.setVisibility(View.VISIBLE);
                            captchaButton.setVisibility(View.GONE);

                            // Getting all the values from response
                            String base64captcha = response.getString("captchaBase64String");
                            String captchaTnxId = response.getString("captchaTxnId");


                            // setting image to captcha field:-
                            byte[] bytes=Base64.decode(base64captcha, Base64.DEFAULT);
                            Bitmap bitmap_captcha = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            captchafield.setImageBitmap(bitmap_captcha);

                            // setting on click listener for Get Otp button:-
                            getOtpButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String captchaString = captchaStringField.getEditText().getText().toString().trim();
                                    String aadharString = aadharNumberField.getEditText().getText().toString().trim();

                                    if(TextUtils.isEmpty(captchaString)){
                                        captchaStringField.setError("required");
                                    }
                                    if(TextUtils.isEmpty(aadharString)){
                                        aadharNumberField.setError("required");
                                    }

                                    // Calling the get otp method now:-
                                    method.generate_otp("OTPCALL", TAG, otp_generation_url, aadharString, captchaString, captchaTnxId);
                                }
                            });

                            // refreshing the captcha when requested.
                            refreshcaptchaButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    captchaStringField.getEditText().setText("");
                                    method.generate_captcha("CAPTCHA", TAG, captcha_generation_url);
                                    Snackbar.make(findViewById(R.id.captchaCoordinatorLayout), "Captcha Refreshed!",
                                            Snackbar.LENGTH_SHORT)
                                            .show();
                                }
                            });

                        }else{
                            Toast.makeText(MainActivity.this, "Couldn't able to get Captcha, Please try again...", Toast.LENGTH_LONG).show();
                        }

//                    String _message = response.getString("message");
//                    if (_message.contains("User does not exists")){
//                        id.setError("User Doesn't Exsist");
//                        dialog.hide();
//                    }else if(_message.contains("Invalid Credentials")){
//                        id.setError("Invalid Credentials");
//                        pass.setError("Invalid Credentials");
//                        dialog.hide();
//                    }
//                    else{
//                        String _name = response.getString("name");
//                        String _username = response.getString("username");
//                        String _image = response.getString("image");
//                        String _access_token = response.getString("access_token");
//                        String _refresh_tocken = response.getString("refresh_token");
//                        editor.putBoolean("isloggedin", true);
//                        editor.putString("access_token", _access_token);
//                        editor.putString("refresh_token", _refresh_tocken);
//                        editor.putString("name", _name);
//                        editor.putString("username", _username);
//                        editor.putString("image", _image);
//                        editor.commit();
//                        Toast.makeText(Login.this, "Login Successfull!", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(Login.this, MainActivity.class));
//                        dialog.hide();
//                        finishAffinity();
//                    }
                    } catch (Exception e) {
                        Log.e(TAG, "Some error occured while requesting Captcha...");
                        Toast.makeText(MainActivity.this, "Some error Occured, Please Try Again Later.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else if (requestType.contains("OTPCALL")){
                    try {
                        String status = response.getString("status");
                        if (status.contains("Success")){
                            Toast.makeText(MainActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, ));
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Some error occured while requesting Captcha...");
                        Toast.makeText(MainActivity.this, "Error Occured while sending OTP, Please Try Again Later.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
//                Log.d(TAG, "Volley requester " + requestType);
//                Log.d(TAG, "Volley JSONObjest post" + response.toString());
            }

            @Override
            public void notifySuccessArray(String requestType, JSONArray response) {
                Log.e(TAG, "Volley requester " + requestType);
                Log.e(TAG, "Volley JSONArray post" + response.toString());
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Toast.makeText(MainActivity.this, "Some error occured while Logging in...", Toast.LENGTH_SHORT).show();
//                dialog.hide();
                Log.e(TAG, "Error occured while requesting API for" + requestType);
                Log.e(TAG, "Volley error request type => " + requestType);
            }

            @Override
            public void ErrorString(String requestType, String error) {
                Log.e(TAG, "Volley requester " + requestType);
            }
        };
    }




}