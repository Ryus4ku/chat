package com.example.chat.Chat;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private String TAG = "##Message";
    private String text;
    private boolean owned;
    private String sender;
    private String time;

    public Message(String text, boolean owned, String sender, String time) {
        this.text = text;
        this.owned = owned;
        this.sender = sender;
        this.time = time;
    }

    public Message(String string) {
        try {
            JSONObject json = new JSONObject(string);
            this.text = (String) json.get("text");
            this.owned = (boolean) json.get("owned");
            this.sender = (String) json.get("sender");
            this.time = (String) json.get("time");
        } catch (JSONException e) {
            Log.d(TAG, "couldn't parse JSON string: " + e.getMessage());
        }
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isOwned() {
        return owned;
    }

    public String getSender() {
        return sender;
    }

    public String getTime() {return time;}

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getJSONString(String sender) {
        String string;
        try {
            JSONObject json = new JSONObject()
                    .put("text", text)
                    .put("owned", false)
                    .put("sender", sender)
                    .put("time", time);
            string = json.toString();
        } catch (JSONException e) {
            Log.d(TAG, "couldn't construct JSONObject: " + e.getMessage());
            string = "";
        }
        return string;
    }
}
