package com.example.mptaskandroid2025;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MPTaskState {

    private static MPTaskState Instance = null;
    public String logTag = "MPTaskMessage";

    private String mServerURL = "";

    private Context mContext;
    private UserAuth mUser;
    private RequestQueue queue;
    private MPTaskStateListener listener;

    LocalStorageHandler storageHandler;

    public ArrayList<MPTask> mPendingTasksArray = new ArrayList<>();
    public ArrayList<MPTask> mCompletedTasksArray = new ArrayList<>();

    public int size = 0;

    public int getNewId(){
        return size + 1;
    }

    public JSONArray getJSONTasks(){
        JSONArray taskList = new JSONArray();
        MPTask task;

        for(int i = 0; i < mPendingTasksArray.size(); i++){
            task = mPendingTasksArray.get(i);
            try {
                taskList.put(new JSONObject(task.toString()));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        for(int i = 0; i < mCompletedTasksArray.size(); i++){
            task = mCompletedTasksArray.get(i);
            try {
                taskList.put(new JSONObject(task.toString()));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return taskList;
    }

    public static MPTaskState getInstance(Context context){
        if(Instance == null){
            Instance = new MPTaskState();
            Instance.mContext = context;
            Instance.setServerURL(context.getString(R.string.webapp_server_url));
            Instance.mUser = UserAuth.getInstance(context);
            Instance.queue = Volley.newRequestQueue(context);
        }

        return(Instance);
    }//end of getInstance function

    private JSONObject getUserStorageState(){
        JSONObject storageState = null;
        if(storageHandler == null) storageHandler = new LocalStorageHandler(mContext);
        try {
            String stringValue = storageHandler.getString(mUser.getEmail());
            storageState = new JSONObject(stringValue);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return storageState;
    }

    public void updateCurrentTasksInStorage(){
        if(mUser.getDemo()) return;
        try {
            JSONObject currentState = getUserStorageState();
            currentState.put("tasks",getJSONTasks().toString());
            storageHandler.setString(mUser.getEmail(),currentState.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setServerURL(String URL){ mServerURL = URL; }
    public String getServerURL(){
        return mServerURL;
    }

    public void addTaskToList(MPTask task){
        if(task.getCompleted()){
            mCompletedTasksArray.add(task);
        }else{
            mPendingTasksArray.add(task);
        }
        size = size + 1;
        updateCurrentTasksInStorage();
    }

    public MPTask findTaskById(int id){
        MPTask task = new MPTask();

        for(int i=0; i < mPendingTasksArray.size(); i++){
            if(mPendingTasksArray.get(i).getId() == id){
                task = mPendingTasksArray.get(i);
            }
        }

        for(int i=0; i < mCompletedTasksArray.size(); i++){
            if(mCompletedTasksArray.get(i).getId() == id){
                task = mCompletedTasksArray.get(i);
            }
        }

        return task;
    }


    public void resetLists(){
        mPendingTasksArray = new ArrayList<>();
        mCompletedTasksArray = new ArrayList<>();
    }

    public void fillTaskList(JSONArray tasks){
        JSONObject task;
        int sizeCount = 0;
        try {
            for(int i = 0; i < tasks.length(); i++) {
                task = tasks.getJSONObject(i);
                if(task.getBoolean("completed")){
                    mCompletedTasksArray.add(new MPTask(task.getInt("id"),task.getString("text"),true));
                }else{
                    mPendingTasksArray.add(new MPTask(task.getInt("id"),task.getString("text"),false));
                }
                sizeCount+= 1;
            }
            size+= sizeCount;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUser(UserAuth user){
        mUser = user;
    }

    public void sendTaskRequest(final String requestType, MPTask task){
        Instance.listener.taskRequestReceived(requestType,task);
    }

    public interface MPTaskStateListener{
        public void taskRequestReceived(String operation, MPTask task);
    }// end of interface
    public void setMPTaskStateListener(MPTaskStateListener listener) {
        this.listener = listener;
    }

    private JSONObject getBaseResponse(){
        JSONObject response = new JSONObject();
        try {
            response.put("status", false);
            response.put("error","Issues connecting to the server");
            response.put("message","");
            response.put("tasks",new JSONArray() );
            response.put("task",new JSONObject() );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }
}// end of class
