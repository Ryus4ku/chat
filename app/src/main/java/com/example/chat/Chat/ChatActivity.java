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

import com.example.chat.DB.DBHelper;
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
    private ListView listView;
    static private List<ChatMessage> messages;
    static private ArrayAdapter<ChatMessage> adapters;
    static private WifiP2pInfo info;
    static private List<ChatClient> clients;
    private ServerSocket serverSocket;
    private Socket socket;
    private String name;
    private int PORT = 8000;
    BufferedReader fromGroupOwner;
    PrintWriter toGroupOwner;
    private static DBHelper dbHelper;
    static int id;
    String clientName;
    boolean needLoadMessage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageButton send = findViewById(R.id.send);
        send.setOnClickListener(this);
        listView = findViewById(R.id.chat);
        clients = new ArrayList<>();
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        info = (WifiP2pInfo) bundle.get("info");
        name = bundle.getString("name");

        dbHelper = new DBHelper(this);
        /*messages = new ArrayList<>();
        adapters = new ChatMessageAdapter(this, R.layout.foreign_bubble, messages);
        listView.setAdapter(adapters);*/

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
                String partnerName = fromGroupOwner.readLine();
                clientName = partnerName;
                getSupportActionBar().setTitle(partnerName);
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
                Socket clientSocket = serverSocket.accept();
                BufferedReader dataIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter dataOut = new PrintWriter(clientSocket.getOutputStream(),true);
                clientName = dataIn.readLine();
                dataOut.println(name);
                ChatClient client = new ChatClient(clientName, dataIn, dataOut);
                client.startListening();
                clients.add(client);
                if(clients.size() == 1) {
                    getSupportActionBar().setTitle(clientName);
                } else {
                    getSupportActionBar().setTitle("Group Chat");
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

    private void message() {
        EditText editText = findViewById(R.id.editText);
        String text = editText.getText().toString();

        if (needLoadMessage) {
            id = dbHelper.checkCompanion(dbHelper, clientName);
            if (id == 0) {
                id = dbHelper.addCompanion(dbHelper, clientName);
            }

            messages = dbHelper.getMessages(dbHelper, id);
            adapters = new ChatMessageAdapter(this, R.layout.foreign_bubble, messages);
            listView.setAdapter(adapters);

            needLoadMessage = false;
        }

        if (!text.equals("")) {
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            String minute;
            if (today.minute < 10) {
                minute = "0" + today.minute;
            } else {
                minute = String.valueOf(today.minute);
            }

            String time = today.hour + ":" + minute;

            ChatMessage chatMessage = new ChatMessage(text, true, "", time);
            messages.add(chatMessage);
            dbHelper.addMessage(dbHelper, true, id, text, time);
            adapters.notifyDataSetChanged();
            editText.setText("");
            /* DONE: Send your message "text" with your Name and a timestamp to the chat partners
             * If you are the group owner send your message to all chat partners in clients list.
             * If you are not the group owner send your message only to him
             */
            @SuppressLint("StaticFieldLeak")
            AsyncTask<ChatMessage,Void,Void> sendMessage = new AsyncTask<ChatMessage, Void, Void>() {
                @Override
                protected Void doInBackground(ChatMessage... chatMessage) {
                    if(info.isGroupOwner && chatMessage.length != 0){
                        for (ChatClient client:clients) {
                            PrintWriter dataOut = client.getDataOut();
                            dataOut.println(chatMessage[0].getJSONString(name));
                        }
                    } else if (chatMessage.length != 0){
                        toGroupOwner.println(chatMessage[0].getJSONString(name));
                    }
                    return null;
                }
            };
            sendMessage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, chatMessage);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /* DONE: When a message is received, the following has to be done:
     * 1. If you are the group owner forward message to all other chat partners in the clients list
     * 2. Retreive the following values from the message: the "text", the "sender", the "time_stamp"
     * 3. Create a new ChatMessage msg = new ChatMessage(text.toString(), false, sender, time_stamp);
     * 4. Add this message to our message list: Messages.add(msg);
     * 5. Notify the adapter, so that it can draw the new message on the listview: adapter.notifyDataSetChanged();
     */
    public static void receive(String data){
        ChatMessage chatMessage = new ChatMessage(data);
        Log.d("Chat","received something");
        Log.d("Chat",chatMessage.getText());
        messages.add(chatMessage);
        dbHelper.addMessage(dbHelper, chatMessage.isOwned(), id, chatMessage.getText(), chatMessage.getTime());
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
        UIHandler.post(() ->
                adapters.notifyDataSetChanged()
        );
    }
}
