package com.example.chat.Chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.chat.DB.DataBase;
import com.example.chat.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements OnClickListener {
    private static List<Message> messages;
    private static List<Message> systemMessages;
    private static ArrayAdapter<Message> adapters;
    private static ArrayAdapter<Message> systemAdapters;
    private static WifiP2pInfo info;
    private static List<Client> clients;
    private static DataBase dataBase;
    private static int companionId;
    private static int userId;
    private int PORT = 8000;
    private boolean needLoadMessage = true;
    private String name;
    private String clientName;
    private ListView listView;
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader fromGroupOwner;
    private PrintWriter toGroupOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageButton send = findViewById(R.id.send);
        send.setOnClickListener(this);

        listView = findViewById(R.id.chat);
        clients = new ArrayList<>();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        info = (WifiP2pInfo) bundle.get("info");
        name = bundle.getString("name");

        dataBase = new DataBase(this);

        try {
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize () throws IOException {
        if(clients != null) {
            clients.clear();
        }

        if(info.isGroupOwner){
            serverSocket = new ServerSocket(PORT);
            getClientInfo.execute();
        } else {
            connectToOwner.execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    AsyncTask<Void,Void,Void> connectToOwner = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            InetAddress groupOwner = info.groupOwnerAddress;
            socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(groupOwner.getHostAddress(), PORT));
                fromGroupOwner = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                toGroupOwner = new PrintWriter(socket.getOutputStream(), true);
                toGroupOwner.println(name);
                clientName = fromGroupOwner.readLine();
                getSupportActionBar().setTitle(clientName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            listenToGroupOwner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    @SuppressLint("StaticFieldLeak")
    AsyncTask<Void,Void,Void> getClientInfo = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
                clientName = reader.readLine();
                writer.println(name);

                Client client = new Client(clientName, reader, writer);
                client.startListening();
                clients.add(client);
                if(clients.size() == 1) {
                    getSupportActionBar().setTitle(clientName);
                } else {
                    getSupportActionBar().setTitle("Груповой чат");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    @SuppressLint("StaticFieldLeak")
    AsyncTask<Void,Void,Void> listenToGroupOwner = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                while(true) {
                    String data;
                    if((data = fromGroupOwner.readLine()) != null) {
                        receive(data);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.send) {
            message();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        while (needLoadMessage) {
            loadMessageFromDB();
        }
    }

    private void message() {
        EditText editText = findViewById(R.id.editText);
        String text = editText.getText().toString();

        if (!text.equals("")) {
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            String minute;
            minute = today.minute < 10 ? "0" + today.minute : String.valueOf(today.minute);
            String time = today.hour + ":" + minute;
            Message message = new Message(text, true, "", time);
            @SuppressLint("StaticFieldLeak")
            AsyncTask<Message,Void,Void> sendMessage = new AsyncTask<Message, Void, Void>() {
                @Override
                protected Void doInBackground(Message... chatMessage) {
                    if(info.isGroupOwner && chatMessage.length != 0){
                        for (Client client:clients) {
                            PrintWriter dataOut = client.getDataOut();
                            dataOut.println(chatMessage[0].getJSONString(name));
                        }
                    } else if (chatMessage.length != 0){
                        toGroupOwner.println(chatMessage[0].getJSONString(name));
                    }
                    return null;
                }
            };
            sendMessage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
            messages.add(message);
            dataBase.addMessage(dataBase, true, companionId, userId, text, time);
            adapters.notifyDataSetChanged();
            editText.setText("");
        }
    }

    private void loadMessageFromDB() {
        if (needLoadMessage && clientName != null) {
            companionId = dataBase.getIdByNameFromCustomTable(dataBase, DataBase.COMPANIONS_TABLE, clientName);
            companionId = companionId == 0 ? dataBase.addCompanion(dataBase, clientName) : companionId;
            userId = dataBase.getIdByNameFromCustomTable(dataBase, DataBase.USERS_TABLE, name);

            messages = dataBase.getMessages(dataBase, companionId, userId);
            adapters = new MessageAdapter(this, R.layout.foreign_bubble, messages);
            listView.setAdapter(adapters);
            needLoadMessage = false;
        }
    }

    private void setSystemMessasge(Message message) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Time today = new Time(Time.getCurrentTimezone());
                today.setToNow();
                String minute;
                minute = today.minute < 10 ? "0" + today.minute : String.valueOf(today.minute);
                String time = today.hour + ":" + minute;
                Message message = new Message(name + " покинул беседу@", true, "", time);
                @SuppressLint("StaticFieldLeak")
                AsyncTask<Message,Void,Void> sendMessage = new AsyncTask<Message, Void, Void>() {
                    @Override
                    protected Void doInBackground(Message... chatMessage) {
                        if(info.isGroupOwner && chatMessage.length != 0){
                            for (Client client:clients) {
                                PrintWriter dataOut = client.getDataOut();
                                dataOut.println(chatMessage[0].getJSONString(name));
                            }
                        } else if (chatMessage.length != 0){
                            toGroupOwner.println(chatMessage[0].getJSONString(name));
                        }
                        return null;
                    }
                };
                sendMessage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
                onBackPressed(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public static void receive(String data){
        Message message = new Message(data);
        Log.d("Chat","received something");
        Log.d("Chat", message.getText());
        String[] splitMessage = message.getText().split(" ", 2);
        if (splitMessage.length > 1 && splitMessage[1].equals("покинул беседу@")) {
            message.setText(message.getText().replaceAll("@", ""));
            message.setSender("ChatBot");
            messages.add(message);
        } else {
            messages.add(message);
            dataBase.addMessage(dataBase, message.isOwned(), companionId, userId, message.getText(), message.getTime());
        }
        updateOnMain();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(info.isGroupOwner) {
                serverSocket.close();
            } else {
                socket.close();
            }
        }catch(Exception e){}
    }

    public static Handler UIHandler = new Handler(Looper.getMainLooper());

    public static void updateOnMain() {
        UIHandler.post(() -> adapters.notifyDataSetChanged());
    }
}
