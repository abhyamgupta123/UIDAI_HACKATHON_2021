package com.resident.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

public class ekycGeneratorFragment extends Fragment {

    // For Logging Purpose and traceback
    private static final String TAG = ekycGeneratorFragment.class.getName();

    // FOR APIs Work
    private IResult mResultCallback = null;
    private api_methods method;

    private TextInputLayout otpField;
    private Button generateEkycButton;

    private Context thiscontext;

    // to be used in api requesting.
    private String _txnid;
    private String aadharNumber;

    // URLs for calling APIs.
    private String ekyc_generate_url = "https://stage1.uidai.gov.in/eAadhaarService/api/downloadOfflineEkyc";

    public ekycGeneratorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_ekyc_generator, container, false);

        // getting the context for this fragment:-
        thiscontext = container.getContext();

        initVolleyCallback();

        // Getting data from previoud activity.
        Intent currentIntent = getActivity().getIntent();
        _txnid = currentIntent.getStringExtra("_txn");
        aadharNumber = currentIntent.getStringExtra("aadhar");

        // Assigning and finding views from activity:-
        otpField = (TextInputLayout) view.findViewById(R.id.otpfield);
        generateEkycButton = (Button) view.findViewById(R.id.generateEkyc);

        // Initialising API methods class before.
        method = callingApiFunction();

        // Setting onclick listener for generateEkyc Button:-
        generateEkycButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otpString = otpField.getEditText().getText().toString().trim();
                method.generate_ekyc("EKYC", TAG, ekyc_generate_url, aadharNumber, _txnid, otpString, "3112");
            }
        });

        return view;
    }

    api_methods callingApiFunction(){
        return new api_methods(mResultCallback, thiscontext);
    }

    void initVolleyCallback(){
        mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                if (requestType.contains("EKYC")){
                    try {
                        Log.i(TAG, "Response for EKYC Recieved Successfully");
                        Log.i(TAG, response.toString());
//                        int statusCode = response.getInt("statusCode");
//
//                        if (statusCode == 200){
//
//                        }else{
//                            Toast.makeText(getApplicationContext(), "Couldn't able to get Captcha, Please try again...", Toast.LENGTH_LONG)
//                                    .show();
//                        }

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
                        Toast.makeText(thiscontext, "Some error Occured, Please Try Again Later.", Toast.LENGTH_SHORT)
                                .show();
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
                Toast.makeText(thiscontext, "Some error occured while Logging in...", Toast.LENGTH_SHORT)
                        .show();
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