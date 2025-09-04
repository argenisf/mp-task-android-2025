package com.example.mptaskandroid2025;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.sessionreplay.MPSessionReplay;
import com.mixpanel.android.sessionreplay.models.MPSessionReplayConfig;
import com.mixpanel.android.sessionreplay.sensitive_views.AutoMaskedView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.util.Collections;
import java.util.Formatter;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    MPTaskState mState;
    UserAuth mUser;
    Button demoAuthBtn;
    Button emailAuthBtn;
    EditText emailInput;
    Boolean activeRequest = false;
    Context mContext;
    ProgressBar mProgress;
    SharedPreferences sharedPref;
    String customServerURL = "http://10.0.0.228:8888/mp-server-side-training/";
    LocalStorageHandler storageHandler;
    public static final String MPToken = "f8bd7cddaf94642530004c3d0509691f";
    MixpanelAPI mMixpanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMixpanel = MixpanelAPI.getInstance(this,MPToken,false);
        mMixpanel.track("App loaded into memory");
        mMixpanel.flush();
        mMixpanel.setEnableLogging(true);

        MPSessionReplayConfig config = new MPSessionReplayConfig();
        config.setAutoStartRecording(true);
        config.setWifiOnly(false);
        config.setFlushInterval(10);
        config.setRecordingSessionsPercent(100);
        config.setEnableLogging(true);
        Set<AutoMaskedView> emptySet = Collections.emptySet();
        config.setAutoMaskedViews(emptySet);


        MPSessionReplay.initialize(this, MPToken, mMixpanel.getDistinctId(), config);

        mContext = this;
        demoAuthBtn = (Button) findViewById(R.id.demoAuthBtn);
        emailAuthBtn = (Button) findViewById(R.id.emailAuthBtn);
        emailInput = (EditText) findViewById(R.id.inputEmail);
        mProgress = (ProgressBar) findViewById(R.id.requestInProgress);
        sharedPref = mContext.getSharedPreferences("user_auth", Context.MODE_PRIVATE);
        storageHandler = new LocalStorageHandler(mContext);

        //hide progress bar by default
        mProgress.setVisibility(View.INVISIBLE);

        mState = MPTaskState.getInstance(mContext);
        //mState.setServerURL(customServerURL);
        mUser = UserAuth.getInstance(mContext);
        loadUserInfo();

        if(mUser.getState() == 1){
            //user already logged in, let's go to the activity
            Intent intent = new Intent(mContext, MainListView.class);
            startActivity(intent);
        }

        // Listener for Demo Button
        demoAuthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!activeRequest){
                    setActiveRequest();
                    mUser.handleAuthentication(mContext, "demo@mixpanel.com");
                }
            }
        });
        // Listener for Email auth button
        emailAuthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                if (email == "" || !email.contains("@mixpanel.com")) {
                    sendErrorMessage("mixpanel.com email required");
                }else{
                    if(!activeRequest){
                        setActiveRequest();
                        mUser.handleAuthentication(mContext, email);
                    }
                }
            }
        });

        // Listener for Authentication response
        mUser.setUserAuthListener(new UserAuth.UserAuthListener() {
            @Override
            public void authenticationRequestReceived(String email) {
                if(Objects.equals(email, "")) setInactiveRequest();
                String result = storageHandler.getString(email);
                JSONObject userState;
                if (Objects.equals(result, "")){
                    //create new user
                    userState = mUser.newUserDetails(email);
                    storageHandler.setString(email,userState.toString());
                    try {
                        JSONObject user = new JSONObject(userState.getString("user"));
                        mUser.setUserDetails(user);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    try {
                        userState = new JSONObject(result);
                        JSONObject user = new JSONObject(userState.getString("user"));
                        mUser.setUserDetails(user);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                mMixpanel.identify(String.valueOf(mUser.getUserID()));
                mMixpanel.getPeople().set("$email",mUser.getEmail());
                mMixpanel.track("user logged in");
                Intent intent = new Intent(mContext, MainListView.class);
                setInactiveRequest();
                startActivity(intent);
            }
        });

    }// end of onCreate

    private void setActiveRequest(){
        activeRequest = true;
        mProgress.setVisibility(View.VISIBLE);
        demoAuthBtn.setEnabled(false);
        emailAuthBtn.setEnabled(false);
        emailInput.setEnabled(false);
    }

    private void setInactiveRequest(){
        activeRequest = false;
        mProgress.setVisibility(View.INVISIBLE);
        demoAuthBtn.setEnabled(true);
        emailAuthBtn.setEnabled(true);
        emailInput.setEnabled(true);
    }

    private void sendErrorMessage(String message){
        new MaterialAlertDialogBuilder(mContext)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .show();
    }

    //user authentication
    private void loadUserInfo(){
        int userId = sharedPref.getInt("userId",0);
        if(userId > 0){
            mUser.setUserID(userId);
            mUser.setState(1);
            mUser.setEmail(sharedPref.getString("email",""));
            if(mUser.getEmail().equals("demo@mixpanel.com")){
                mUser.setDemo(true);
            }
        }
        //int highScore = sharedPref.getInt(getString(R.string.saved_high_score_key), defaultValue);
    }

}// end of MainActivity Class