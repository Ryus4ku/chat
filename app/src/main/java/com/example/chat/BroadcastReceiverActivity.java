package com.example.chat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.victor.loading.rotate.RotateLoading;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channel;

public class BroadcastReceiverActivity extends AppCompatActivity implements ConnectionInfoListener {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private String name;
    private WifiP2pDeviceList deviceList;
    private ListView mListView;
    private TextView emptyText;
    private ArrayAdapter<String> wifiP2PArrayAdapter;
    private WifiP2pDevice connectedPartner;
    private String TAG = "##BoadcastReceiverAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_receiver);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View bv = findViewById(R.id.broadcastActivity);
        bv.setBackgroundColor(getResources().getColor(R.color.colorLightGrey));

        Button button = findViewById(R.id.refreshButton);

        button.setOnClickListener(v -> onRefresh());

        Bundle extras = getIntent().getExtras();
        name = extras.getString("nameText");

        getSupportActionBar().setTitle("Поиск устройств");

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, this, peerListListener);

        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, group -> {
                if (group != null && mManager != null && mChannel != null) {
                    mManager.removeGroup(mChannel, new ActionListener() {
                        @Override
                        public void onSuccess() { Log.d(TAG, "removeGroup onSuccess -"); }

                        @Override
                        public void onFailure(int reason) { Log.d(TAG, "removeGroup onFailure -" + reason); }
                    });
                }
            });
        }

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        try {
            Method m = mManager.getClass().getMethod("setDeviceName", new Class[]{Channel.class, String.class, ActionListener.class});
            m.invoke(mManager, mChannel, name, new ActionListener() {
                @Override
                public void onSuccess() { Log.d(TAG, "Name change successful."); }

                @Override
                public void onFailure(int reason) { Log.d(TAG, "name change failed: " + reason); }
            });
        } catch (Exception e) { Log.d(TAG, "No such method"); }


        mManager.discoverPeers(mChannel, new ActionListener() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(int reason) {}
        });

        mListView = findViewById(R.id.ListView);
        emptyText = findViewById(android.R.id.empty);
        mListView.setEmptyView(emptyText);
        wifiP2PArrayAdapter = new ArrayAdapter<>(this, R.layout.fragment_peer, R.id.textView);
        mListView.setAdapter(wifiP2PArrayAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "item clicked");
            TextView textView = view.findViewById(R.id.textView);

            WifiP2pDevice device = null;
            for (WifiP2pDevice deviceForCycle : deviceList.getDeviceList()) {
                device = deviceForCycle.deviceName.equals(textView.getText()) ? deviceForCycle : device;
            }

            if (device != null) {
                Log.d(TAG, " calling connectToPeer");
                connectToDevice(device);
            }
        });
        receiveConnectRequest.execute();
    }

    private void connectToDevice(final WifiP2pDevice device) {
        this.connectedPartner = device;
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new ActionListener() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(int reason) { }
        });
    }

    private PeerListListener peerListListener = new PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            RotateLoading rotateLoading = findViewById(R.id.rotateloading);
            rotateLoading.start();
            Log.d("INPeerListListener", "Works");
            TextView textView2 = findViewById(R.id.textView2);
            if (!peers.toString().equals("")) {
                textView2.setText("Список устройств:");
            } else {
                textView2.setText("Нет доступных устройств");
            }

            for (WifiP2pDevice device : peers.getDeviceList()) {
                device.deviceName = device.deviceName.replace("[Phone]", "");
            }

            deviceList = new WifiP2pDeviceList(peers);
            wifiP2PArrayAdapter.clear();
            for (WifiP2pDevice device : peers.getDeviceList()) {
                wifiP2PArrayAdapter.add(device.deviceName);
                Log.d("INPeerListListenerNAME:", device.deviceName);
            }

            mListView.setEmptyView(emptyText);
            mListView.setAdapter(wifiP2PArrayAdapter);
            rotateLoading.stop();
        }
    };

    @SuppressLint("StaticFieldLeak")
    AsyncTask<Void, Void, Void> receiveConnectRequest = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                ServerSocket server = new ServerSocket();
                Socket client = server.accept();
                BufferedReader dataIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter dataOut = new PrintWriter(client.getOutputStream(), true);
                String in;
                while (true) {
                    if ((in = dataIn.readLine()) != null) {
                        String request;
                        String name;
                        try {
                            JSONObject json = new JSONObject(in);
                            request = json.getString("request");
                            name = json.getString("name");
                        } catch (JSONException jsonException) {
                            request = "";
                            name = "";
                        }

                        if (request.equals("connection request")) {
                            String ack = "";
                            try {
                                ack = new JSONObject().put("type", "ack").toString();
                            } catch (JSONException jsonException) {
                                Log.d(TAG, "creating ack failed :" + jsonException.getMessage());
                            }
                            dataOut.println(ack);
                            Intent intent = new Intent(BroadcastReceiverActivity.this, ChatActivity.class);
                            startActivity(intent);
                            break;
                        }
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(TAG, "pre connect " + (info.groupFormed));
        if (info.groupFormed) {
            Intent intent = new Intent(BroadcastReceiverActivity.this, ChatActivity.class);
            intent.putExtra("info", info);
            intent.putExtra("name", name);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, group -> {
                if (group != null && mManager != null && mChannel != null) {
                    mManager.removeGroup(mChannel, new ActionListener() {
                        @Override
                        public void onSuccess() { Log.d(TAG, "removeGroup onSuccess2 -"); }

                        @Override
                        public void onFailure(int reason) { Log.d(TAG, "removeGroup onFailure2 -" + reason); }
                    });
                }
            });
        }
    }

    public void onRefresh() {
        mManager.discoverPeers(mChannel, new ActionListener() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(int reason) {}
        });
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
}
