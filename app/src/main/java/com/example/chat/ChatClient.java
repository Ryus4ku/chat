package com.example.chat;

import java.io.BufferedReader;
import java.io.PrintWriter;

class ChatClient {
    private BufferedReader dataIn;
    private PrintWriter dataOut;
    private String name;
    private Listener listener;

    public ChatClient(String name, BufferedReader dataIn, PrintWriter dataOut){
        this.dataIn = dataIn;
        this.dataOut = dataOut;
        this.name = name;

        listener = new Listener(dataIn);
    }

    public PrintWriter getDataOut(){return dataOut;}

    public void startListening(){
        listener.listening=true;
        listener.start();
    }
}
