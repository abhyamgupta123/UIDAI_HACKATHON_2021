package com.resident.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class Mainfragment extends Fragment {

    private static final String TAG = ekycEncryptorFragment.class.getName();
    private static final String sharedPreferenceString = "UIDAI.GOV.INDIA";

    private String _pass;

    private Button generateQrButton;
    private Button logoutEkyc;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Global variable for storing active context:-
    private Context thiscontext;

    public Mainfragment() {
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
        View view = inflater.inflate(R.layout.fragment_mainfragment, container, false);

        // Finding the views:-
        generateQrButton = (Button) view.findViewById(R.id.generateQR);
        logoutEkyc = (Button) view.findViewById(R.id.logoutEKYC);

        // getting the context for this fragment:-
        thiscontext = container.getContext();

        // Using Sharedpreference:-
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(sharedPreferenceString, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Boolean ekycFlag = sharedPreferences.getBoolean("ekycFlag", false);

        if (!ekycFlag){
            // Going to captcha fragment because the ekyc is not registered.
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fraagment_view, new captchafragment());
            fragmentTransaction.commit();
        }

        String encryptedEkycString = sharedPreferences.getString("encodedKyc", "");
        String zipfilename = sharedPreferences.getString("_requestDate", "Empty.zip");

        if (!encryptedEkycString.isEmpty()){
            generateQrButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Enter Secret Password");
                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.password_prompt, (ViewGroup) getView(), false);
                    // Set up the input
                    final EditText input = (EditText) viewInflated.findViewById(R.id.passField);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    builder.setView(viewInflated);

                    // Set up the buttons
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // continue to decode string and other further process:-
                            _pass = input.getText().toString();
                            dialog.dismiss();
                            try {
                                String decryptedString = decryptString(encryptedEkycString, _pass);
                                writeStringAsFile(decryptedString, zipfilename);

                                Toast.makeText(thiscontext, decryptedString, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "Couldn't able to decrypt String due to some reason, maybe password is wrong.");
                                Toast.makeText(thiscontext, "Error Encrypting your EKYC, Try Again.", Toast.LENGTH_LONG)
                                        .show();
                            }
//                m_Text = input.getText().toString();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.show();
                }
            });
        }else{
            // Show the message for resigning for ekyc.
            Toast.makeText(thiscontext, "Register for eKYC...", Toast.LENGTH_LONG).show();
            // removing the saved strings:-

            editor.remove("encodedKyc");
            editor.remove("ekycFlag");
            editor.remove("requestedDate");

            // Going to captcha fragment because the ekyc is not registered.
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fraagment_view, new captchafragment());
            fragmentTransaction.commit();
        }

        logoutEkyc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // removing the saved strings:-
                editor.remove("encodedKyc");
                editor.remove("ekycFlag");
                editor.remove("requestedDate");
                editor.commit();

                deleteFiles("Emptry.zip");
                deleteFiles(zipfilename);

                // Going to captcha fragment because the ekyc is not registered.
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fraagment_view, new captchafragment());
                fragmentTransaction.commit();
            }
        });
        return view;
    }

    public void deleteFiles(String filename){
        File path = thiscontext.getExternalFilesDir("eKYC");

        File fdelete = new File(path, filename);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d(TAG, "file Deleted :" + path.toString() + filename);
            } else {
                System.out.println("file not Deleted :" + path.toString() + filename);
            }
        }
    }
    public void writeStringAsFile(String fileContents, String fileName) throws IOException {

        File path = thiscontext.getExternalFilesDir("eKYC");
        Log.d(TAG, "===========>>>>> File path is " + path);

        File file = new File(path, fileName);
        FileOutputStream stream = new FileOutputStream(file);

        try {
            stream.write(fileContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stream.close();
        }
    }

//    public String readFileAsString(String fileName) {
//        Context context = App.instance.getApplicationContext();
//        StringBuilder stringBuilder = new StringBuilder();
//        String line;
//        BufferedReader in = null;
//
//        try {
//            in = new BufferedReader(new FileReader(new File(context.getFilesDir(), fileName)));
//            while ((line = in.readLine()) != null) stringBuilder.append(line);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//
//        }
//
//        return stringBuilder.toString();
//    }

    private String decryptString(String outputString, String password) throws Exception{
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodeedValue = Base64.decode(outputString, Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodeedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
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