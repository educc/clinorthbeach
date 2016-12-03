package com.ecacho.clinorthbeach.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ecacho on 11/13/16.
 */
public class Backend {
    private static final String TAG = "app.Backend";

    public static String manageIntent(JSONObject jsonObj){
        String result = "No puedo procesar su solicitud";
        try{
            JSONArray jsarray = jsonObj.getJSONArray("intents");
            if( jsarray.length() > 0){
                JSONObject jsIntent = jsarray.getJSONObject(0);
                String name = jsIntent.getString("intent");
                if( name.equalsIgnoreCase("VerSaldo")){
                    result = "Su saldo es de 100 soles.";
                }
            }
        }catch(JSONException ex){
            Log.e(TAG, ex.toString());
        }
        return result;
    }


}
