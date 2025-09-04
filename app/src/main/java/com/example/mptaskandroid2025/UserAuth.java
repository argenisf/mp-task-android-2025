package com.example.mptaskandroid2025;
import android.content.Context;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.Objects;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAuth {
    //0: pending authentication
    //1: authenticated
    //-1: issue authenticating
    private int mState;
    private Boolean mDemo;
    private int mUserID;
    private String mEmail;
    RequestQueue queue;
    private String logTag = "MPTaskMessage";
    private static UserAuth Instance = null;
    private UserAuthListener listener;

    private void initializeValues(){
        this.mState = 0;
        this.mDemo = false;
        this.mUserID = 0;
        this.mEmail = "";
    }

    private UserAuth(){
        initializeValues();
        this.listener = null;
    }// end of constructor

    public static UserAuth getInstance(Context mContext){
        if(Instance == null){
            Instance = new UserAuth();
        }
        return Instance;
    }

    public void setState(int value){ this.mState = value; }
    public void setDemo(boolean value){ this.mDemo = value; }
    public void setUserID(int value){ this.mUserID = value; }
    public void setEmail(String value){ this.mEmail = value; }

    public int getState(){ return this.mState; };
    public Boolean getDemo(){ return this.mDemo; };
    public int getUserID(){ return this.mUserID; };
    public String getEmail(){ return this.mEmail; };

    public void resetUser(){
        initializeValues();
    }

    int getRandomNumber(int x, int y) {
        return (int)(Math.random() * (y - x + 1)) + x;
    }

    public void handleAuthentication(Context context, String emailAddress){
        MPTaskState mTaskState = MPTaskState.getInstance(context);
        if(emailAddress.trim() == ""){
            setState(-1);
            return;
        }

        setState(1);
        Instance.listener.authenticationRequestReceived(emailAddress);

    }//end of function handleAuthentication

    public JSONObject newUserDetails(String email){
        JSONObject newUserState = new JSONObject();
        JSONObject newUser = new JSONObject();
        try {
            int newUserId = getRandomNumber(10000,100000);
            setUserID(newUserId);
            newUser.put("id",newUserId);

            setEmail(email);
            newUser.put("email",email);

            newUserState.put("user",newUser.toString());
            newUserState.put("tasks","[]");
            Instance.setState(1);
            if(Instance.getEmail().contains("demo@mixpanel.com")){
                Instance.setDemo(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newUserState;
    }// end of newUserDetails

    public void setUserDetails(JSONObject user){
        try {
            Instance.setEmail(user.getString("email"));
            Instance.setState(1);
            if(Instance.getEmail().contains("demo@mixpanel.com")){
                Instance.setDemo(true);
                Instance.setUserID(0);
            }else{
                Object userId = user.get("id");
                if(userId instanceof Integer){
                    Instance.setUserID(user.getInt("id"));
                }else{
                    Instance.setUserID(getRandomNumber(10000,100000));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface UserAuthListener{
        public void authenticationRequestReceived(String email);
    }// end of interface
    public void setUserAuthListener(UserAuthListener listener) {
        this.listener = listener;
    }

    private JSONObject getBaseResponse(){
        JSONObject response = new JSONObject();
        try {
            response.put("status", false);
            response.put("error","Issues connecting to the server");
            response.put("message","");
            response.put("user",new JSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

}// end of UserAuth class
