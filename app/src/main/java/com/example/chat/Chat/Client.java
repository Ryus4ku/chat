package com.example.chat.Chat;

import com.example.chat.Listener;

import java.io.BufferedReader;
import java.io.PrintWriter;

class Client {
    private BufferedReader dataIn;
    private PrintWriter dataOut;
    private String name;
    private Listener listener;

    public Client(String name, BufferedReader dataIn, PrintWriter dataOut){
        this.dataIn = dataIn;
        this.dataOut = dataOut;
        this.name = name;

        listener = new Listener(dataIn);
    }

    public PrintWriter getDataOut(){return dataOut;}

    public void startListening(){
        listener.listening = true;
        listener.start();
    }
}
