package com.example.mptaskandroid2025;

import org.json.JSONException;
import org.json.JSONObject;

public class MPTask {

    private int id = 0;
    private String text = "";
    private Boolean completed = false;

    public MPTask(){ }

    public MPTask(int idVal, String textVal, Boolean completedVal){
        this.id = idVal;
        this.text = textVal;
        this.completed = completedVal;
    }//constructor

    public MPTask(JSONObject task){
        try {
            this.id = task.getInt("id");
            this.text = task.getString("text");
            this.completed = task.getBoolean("completed");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }//constructor


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String toString(){
        JSONObject task = new JSONObject();
        try {
            task.put("id",getId());
            task.put("text",getText());
            task.put("completed",getCompleted());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return task.toString();
    }
}//end of class
