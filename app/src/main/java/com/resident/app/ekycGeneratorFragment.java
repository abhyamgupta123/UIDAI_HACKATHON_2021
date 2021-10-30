package com.resident.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
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

        // Getting data from previoud fragment:-
        _txnid = getArguments().getString("_txn");
        aadharNumber = getArguments().getString("aadhar");

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
//                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.add(R.id.fraagment_view, new ekycEncryptorFragment());
//                fragmentTransaction.commit();
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
                        String status = response.getString("status");
                        Log.d(TAG, "status code value important =>>>>>> " + status);
                        if (status.contains("Success")){
                            String _kycstr = response.getString("eKycXML");
                            String _requestDate = response.getString("requestDate");

                            // adding values to pass to ther activity:-
                            Bundle args = new Bundle();
                            args.putString("_kycstr", _kycstr);                           // adding data to pass in next activity.
                            args.putString("_requestDate", _requestDate);                 // adding data to pass in next activity.

                            Fragment ekycEncryptorfragment = new ekycEncryptorFragment();
                            ekycEncryptorfragment.setArguments(args);

                            // Now transferring again to captcha generation fragment.
                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.add(R.id.fraagment_view, ekycEncryptorfragment);
                            fragmentTransaction.commit();
                        }else if (status.contains("400")){
//                            Log.e(TAG, "DONEEEEEE================");
                            String errorCode = response.getString("errorCode");
                            if (errorCode.contains("UES-VAL-002")){
                                Log.e(TAG, "OTP IS Invelid...");
                                Toast.makeText(thiscontext, "Invalid OTP! Try again.", Toast.LENGTH_SHORT)
                                        .show();

//                                    // Now transferring again to captcha generation fragment.
//                                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//                                    fragmentTransaction.add(R.id.fraagment_view, captchafragment);
////                                  fragmentTransaction.addToBackStack(null);
//                                    fragmentTransaction.commit();

                            }else if (errorCode.contains("UES-VAL-004")){
                                Log.e(TAG, "Invalid Request!");
                                Toast.makeText(thiscontext, "Unknown Error, Try contacting Developer!", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    } catch (Exception e) {
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