package com.resident.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;

public class eventDetails extends Fragment {

    // For Logging Purpose and traceback
    private static final String TAG = ekycEncryptorFragment.class.getName();
    private static final String sharedPreferenceString = "UIDAI.GOV.INDIA";

    // Global variable for storing active context:-
    private Context thiscontext;

    private String _kycstr;
    private String _requestDate;

    // For shared preferences:-
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // views variables:-
    private TextInputLayout nameField;
    private TextInputLayout eventIdField;
    private Button registerEventButton;

    public eventDetails() {
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
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        // getting the context for this fragment:-
        thiscontext = container.getContext();

        // Finding Views from corresponding fragment:-
        nameField = (TextInputLayout) view.findViewById(R.id.fullName);
        eventIdField = (TextInputLayout) view.findViewById(R.id.eventId);
        registerEventButton = (Button) view.findViewById(R.id.registerEvent);

        // Using Sharedpreference:-
        sharedPreferences = thiscontext.getApplicationContext().getSharedPreferences(sharedPreferenceString, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        registerEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = nameField.getEditText().getText().toString().trim();
                String eventId = eventIdField.getEditText().getText().toString().trim();

                if(TextUtils.isEmpty(userName)){
                    nameField.setError("required");
                }
                if(TextUtils.isEmpty(eventId)){
                    eventIdField.setError("required");
                }

                if (!(TextUtils.isEmpty(userName) && TextUtils.isEmpty(eventId))){
                    editor.putString("Username", userName);
                    editor.putString("eventUID", eventId);
                    editor.commit();

                    // Getting data from previoud fragment:-
                    _kycstr = getArguments().getString("_kycstr");
                    _requestDate = getArguments().getString("_requestDate");

                    // adding values to pass to ther activity:-
                    Bundle args = new Bundle();
                    args.putString("_kycstr", _kycstr);
                    args.putString("_requestDate", _requestDate);

                    Fragment ekycEncryptorfragment = new ekycEncryptorFragment();
                    ekycEncryptorfragment.setArguments(args);

                    // Now transferring again to captcha generation fragment.
                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fraagment_view, ekycEncryptorfragment);
                    fragmentTransaction.commit();
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}