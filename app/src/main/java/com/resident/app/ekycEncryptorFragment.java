package com.resident.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class ekycEncryptorFragment extends Fragment {

    private static final String TAG = ekycEncryptorFragment.class.getName();
    private static final String sharedPreferenceString = "UIDAI.GOV.INDIA";

    private TextInputLayout passField1;
    private TextInputLayout passField2;
    private Button generateekycButton;
//    private Button decryptButton;


    private String keyXML;
    private String requestDate;

    // Global variable for storing active context:-
    private Context thiscontext;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public ekycEncryptorFragment() {
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
        View view = inflater.inflate(R.layout.fragment_ekyc_encryptor, container, false);

        // getting the context for this fragment:-
        thiscontext = container.getContext();

        // Finding Views from corresponding fragment:-
        passField1 = (TextInputLayout) view.findViewById(R.id.pass1Field);
        passField2 = (TextInputLayout) view.findViewById(R.id.pass2Field);
        generateekycButton = (Button) view.findViewById(R.id.createEkycButton);
//      decryptButton = (Button) view.findViewById(R.id.decrypt);


        // Using Sharedpreference:-
        sharedPreferences = thiscontext.getApplicationContext().getSharedPreferences(sharedPreferenceString, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        // Setting on click listene:-
        generateekycButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String _pass1 = passField1.getEditText().getText().toString().trim();
                String _pass2 = passField2.getEditText().getText().toString().trim();

                if (_pass1.length()<6){
                    passField1.setError("Length Less than 6");
                }
                if (_pass2.length()<6){
                    passField2.setError("Length Less than 6");
                }

                if(TextUtils.isEmpty(_pass1)){
                    passField1.setError("required");
                }
                if(TextUtils.isEmpty(_pass2)){
                    passField2.setError("required");
                }

                if (_pass1.equals(_pass2)){
                    // Getting data from previoud fragment:-
                    keyXML = getArguments().getString("_kycstr");
                    requestDate = getArguments().getString("_requestDate");
                    Log.e(TAG, "==========>>>>>>>>>>>>> requestDate is " + requestDate);

                    try {
                        String encryptedString = encryptString(keyXML, _pass1);

                        // putting value in shared preference:-
                        editor.putString("encodedKyc", encryptedString);
                        editor.putString("requestedDate", requestDate + ".zip");
                        editor.putBoolean("ekycFlag", true);
                        editor.commit();

                        Toast.makeText(thiscontext, "eKYC registered Successfully...", Toast.LENGTH_SHORT)
                                .show();
                        // Now transferring again to captcha generation fragment.
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.add(R.id.fraagment_view, new Mainfragment());
                        fragmentTransaction.commit();

//                        Toast.makeText(thiscontext, encryptedString, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(thiscontext, "Error Encrypting your EKYC, Try Again.", Toast.LENGTH_LONG)
                                .show();
                    }
                }else{
                    Snackbar.make(getActivity().findViewById(R.id.encryptEkycFragmentCoordinatorLayout), "Passwords Doesn't Match",
                            Snackbar.LENGTH_LONG)
                            .show();
                    passField1.setError("NOT MATCHING");
                    passField2.setError("NOT MATCHING");
                }
            }
        });
//        decryptButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String _pass1 = passField1.getEditText().getText().toString().trim();
//                String _pass2 = passField2.getEditText().getText().toString().trim();
//
//                try{
//                    String decryptedString = decryptString(t, _pass1);
//                    Toast.makeText(thiscontext, decryptedString, Toast.LENGTH_SHORT).show();
//                } catch (Exception e){
//                    e.printStackTrace();
//                    Toast.makeText(thiscontext, "Error Encrypting your EKYC, Try Again.", Toast.LENGTH_LONG)
//                            .show();
//                }
//            }
//        });
        return view;
    }

//    private String decryptString(String outputString, String password) throws Exception{
//        SecretKeySpec key = generateKey(password);
//        Cipher c = Cipher.getInstance("AES");
//        c.init(Cipher.DECRYPT_MODE, key);
//        byte[] decodeedValue = Base64.decode(outputString, Base64.DEFAULT);
//        byte[] decValue = c.doFinal(decodeedValue);
//        String decryptedValue = new String(decValue);
//        return decryptedValue;
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String encryptString(String dataString, String secretPass) throws Exception{
        SecretKeySpec key = generateKey(secretPass);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(dataString.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, android.util.Base64.DEFAULT);
        return encryptedValue;
    }

    private SecretKeySpec generateKey(String secretPassword) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = secretPassword.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }
}