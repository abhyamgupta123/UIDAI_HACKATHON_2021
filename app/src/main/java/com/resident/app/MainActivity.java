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



        captchaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String aadharNumber = aadharNumberField.getEditText().getText().toString().trim();
                callingApiFunction("POSTCALL", captcha_generation_url);
            }
        });
    }

    void callingApiFunction(String requestType, String url){
        // calling the desired method:-
        method = new api_methods(mResultCallback, MainActivity.this);
        method.generate_captcha(requestType, TAG, url);
    }


    void initVolleyCallback(){
        mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                try {
                    Log.d(TAG, "response got succesfully");
                    Log.d(TAG, response.toString());
                    int statusCode = response.getInt("statusCode");
                    if (statusCode == 200){

                        // setting visibility of all views
                        captchafield.setVisibility(View.VISIBLE);
                        captchaStringField.setVisibility(View.VISIBLE);
                        aadharNumberField.setVisibility(View.VISIBLE);
                        getOtpButton.setVisibility(View.VISIBLE);
                        refreshcaptchaButton.setVisibility(View.VISIBLE);
                        captchaButton.setVisibility(View.GONE);

                        // setting image to captcha field:-
                        String base64captcha = response.getString("captchaBase64String");
                        byte[] bytes=Base64.decode(base64captcha, Base64.DEFAULT);
                        Bitmap bitmap_captcha = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        captchafield.setImageBitmap(bitmap_captcha);

                        // setting on click listener for Get Otp button:-
                        captchaButton.setOnClickListener(new View.OnClickListener() {
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
                            }
                        });

                        refreshcaptchaButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                captchaStringField.getEditText().setText("");
                                callingApiFunction("POSTCALL", captcha_generation_url);
                                Snackbar.make(findViewById(R.id.captchaCoordinatorLayout), "Captcha Refreshed!",
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });

                    }else{
                        Toast.makeText(MainActivity.this, "Couldn't able to get Captcha, Please try again Later", Toast.LENGTH_LONG).show();
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
                    Log.e(TAG, "Has not contained message field...");
                    e.printStackTrace();
                }
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSONObjest post" + response.toString());
            }

            @Override
            public void notifySuccessArray(String requestType, JSONArray response) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSONArray post" + response.toString());
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Toast.makeText(MainActivity.this, "Some error occured while Logging in...", Toast.LENGTH_SHORT).show();
//                dialog.hide();
                Log.e(TAG, "Volley requester " + requestType);
                Log.e(TAG, "Volley error===>>>" + "That didn't work!");
            }

            @Override
            public void ErrorString(String requestType, String error) {
                Log.e(TAG, "Volley requester " + requestType);
                Log.e(TAG, "Volley string error ===>>" + error);
            }
        };
    }




}