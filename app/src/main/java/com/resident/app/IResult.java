package com.resident.app;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IResult {

    public void notifySuccess(String requestType, JSONObject response);
    public void notifySuccessArray(String requestType, JSONArray response);
    public void notifyError(String requestType, VolleyError error);
    public void ErrorString(String requestType, String error);

}
