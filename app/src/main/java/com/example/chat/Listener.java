package com.example.chat;

import android.util.Log;

import com.example.chat.Chat.ChatActivity;

import java.io.BufferedReader;

public class Listener extends Thread{
    BufferedReader input;
    public boolean listening;

    public Listener(BufferedReader input){
        this.input = input;
    }

    public void run(){
        Log.d("Chat","Started listening to client");
        while(listening){
            try{
                String data;
                if((data = input.readLine()) != null){
                    Log.d("Chat","Listener calling receive");
                    ChatActivity.receive(data);
                }
            }catch(Exception exception){
                exception.printStackTrace();
            }
        }
    }
}
