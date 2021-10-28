package com.resident.app;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class api_methods {

    public final static String classname = "API METHODS";

    IResult mResultCallback = null;
    Context context;

    api_methods(IResult resultCallback, Context context) {
        this.mResultCallback = resultCallback;
        this.context = context;
    }

    public void generate_captcha(String requestType, String TAG, String url){
        RequestQueue queue = Volley.newRequestQueue(context);  // passing context is neccessary.
        JSONObject js = new JSONObject();
        try {
            js.put("langCode","en");
            js.put("captchaLength","3");
            js.put("captchaType","2");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "respose -> " + response.toString());
                        try {
//                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG + classname, "respose messsage is -->" + response.getString("message"));
                            mResultCallback.notifySuccess(requestType, response);
                        } catch (JSONException e) {
                            Log.e(TAG + classname, "Some error is parsing and sending respose message to activity class");
                            e.printStackTrace();
                        }
                        Log.d(TAG + classname, response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        mResultCallback.notifyError("POSTCALL", error);
                        Log.d(TAG, "Error.Response -> " + error);
                        Log.d(TAG, "Error.Response -> " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(postRequest);
    }

    public void generate_otp(String requestType, String TAG, String url, String aadharNumber, String captchaValue, String captchaTnxId){
        RequestQueue queue = Volley.newRequestQueue(context);  // passing context is neccessary.

        // generating uuid for sending it to the UIADI server
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        JSONObject js = new JSONObject();
        try {
            js.put("uidNumber",aadharNumber);
            js.put("captchaTxnId", captchaTnxId);
            js.put("captchaValue",captchaValue);
            js.put("transactionId","MYAADHAAR:" + uuidAsString);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "respose -> " + response.toString());
                        try {
//                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG + classname, "respose messsage is -->" + response.getString("message"));
                            mResultCallback.notifySuccess(requestType, response);
                        } catch (JSONException e) {
                            Log.e(TAG + classname, "Some error is parsing and sending respose message to activity class");
                            e.printStackTrace();
                        }
                        Log.d(TAG + classname, response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        mResultCallback.notifyError("POSTCALL", error);
                        Log.d(TAG, "Error.Response -> " + error);
                        Log.d(TAG, "Error.Response -> " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("x-request-id", uuidAsString);
                headers.put("appid", "MYAADHAAR");
                headers.put("Accept", "*/*");
                headers.put("Accept-Language", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        queue.add(postRequest);
    }
}
