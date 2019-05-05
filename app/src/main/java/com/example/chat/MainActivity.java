package com.example.chat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chat.DB.DBHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG = "mainLogs";
    private ArrayList<String> users;
    private DBHelper dbHelper;
    private ListView userList;
    private boolean permissionToAdd = false;
    ArrayAdapter<String> adapter;
    Button button;
    int orangeColor;

    private int ACCESS_WIFI_STATE;
    private int CHANGE_WIFI_STATE;
    private int CHANGE_NETWORK_STATE;
    private int INTERNET;
    private int ACCESS_NETWORK_STATE;
    private int WRITE_EXTERNAL_STORAGE;
    private int READ_PHONE_STATE;
    private int READ_EXTERNAL_STORAGE;

    private static final int REQUEST_CODE_ACCESS_WIFI_STATE = 1;
    private static final int REQUEST_CODE_CHANGE_WIFI_STATE = 2;
    private static final int REQUEST_CODE_CHANGE_NETWORK_STATE = 3;
    private static final int REQUEST_CODE_INTERNET = 4;
    private static final int REQUEST_CODE_ACCESS_NETWORK_STATE = 5;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 6;
    private static final int REQUEST_CODE_READ_PHONE_STATE = 7;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 8;
    private static final int ACCESS_FINE_LOCATION = 9;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ACCESS_WIFI_STATE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        CHANGE_WIFI_STATE = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        CHANGE_NETWORK_STATE = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE);
        INTERNET = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        ACCESS_NETWORK_STATE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        READ_PHONE_STATE = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        READ_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (ACCESS_WIFI_STATE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_CODE_ACCESS_WIFI_STATE);
        }
        if (CHANGE_WIFI_STATE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CHANGE_WIFI_STATE}, REQUEST_CODE_CHANGE_WIFI_STATE);
        }
        if (CHANGE_NETWORK_STATE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CHANGE_NETWORK_STATE}, REQUEST_CODE_CHANGE_NETWORK_STATE);
        }
        if (INTERNET != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, REQUEST_CODE_INTERNET);
        }
        if (ACCESS_NETWORK_STATE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_NETWORK_STATE}, REQUEST_CODE_ACCESS_NETWORK_STATE);
        }
        if (WRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
        if (READ_PHONE_STATE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE);
        }
        if (READ_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        }

        View bv = findViewById(R.id.mainActivity);
        bv.setBackgroundColor(getResources().getColor(R.color.colorLightGrey));

        EditText editText = findViewById(R.id.editText);
        final int whiteColor = getResources().getColor(R.color.white);
        editText.setBackgroundColor(whiteColor);

        button = findViewById(R.id.joinButton);
        orangeColor = getResources().getColor(R.color.colorOrange);
        button.setBackgroundColor(orangeColor);
        button.setEnabled(false);
        button.setBackgroundColor(getResources().getColor(R.color.colorGrey));

        userList = findViewById(R.id.userListView);
        dbHelper = new DBHelper(this);

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0){
                    buttonAddDisabled();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!users.isEmpty()) {
                    int repeatUser = 0;
                    repeatUser = users.contains(s.toString()) ? repeatUser + 1 : repeatUser;
                    if (repeatUser > 0) {
                        buttonAddDisabled();
                    } else {
                        buttonAddEnabled();
                    }
                } else {
                    buttonAddEnabled();
                }
            }
        });
    }

    private void buttonAddDisabled() {
        button.setText("Добавить пользователя");
        button.setEnabled(false);
        button.setBackgroundColor(getResources().getColor(R.color.colorGrey));
        permissionToAdd = false;
    }

    private void buttonAddEnabled() {
        button.setText("Добавить пользователя");
        button.setEnabled(true);
        button.setBackgroundColor(orangeColor);
        permissionToAdd = true;
    }

    public void joinPressed(View view) {
        EditText editText = findViewById(R.id.editText);
        String message = editText.getText().toString();
        if (permissionToAdd) {
            dbHelper.insertNewUser(dbHelper, message);
            users = dbHelper.getAllUsers(dbHelper);
            adapter = new ArrayAdapter<>(this, R.layout.user_fragment, users);
            userList.setAdapter(adapter);
            userList.setOnItemClickListener((parent, newView, position, id) -> {
                TextView textView = newView.findViewById(R.id.userTextView);
                Intent intent = new Intent(MainActivity.this, BroadcastReceiverActivity.class);
                intent.putExtra("nameText", getCurrentName(textView));
                startActivity(intent);
            });
        }
        buttonAddDisabled();
    }

    @Override
    protected void onStart() {
        super.onStart();
        users = dbHelper.getAllUsers(dbHelper);
        if (!users.isEmpty()) {
            adapter = new ArrayAdapter<>(this, R.layout.user_fragment, users);
            userList.setAdapter(adapter);
            userList.setOnItemClickListener((parent, view, position, id) -> {
                TextView textView = view.findViewById(R.id.userTextView);
                Intent intent = new Intent(MainActivity.this, BroadcastReceiverActivity.class);
                intent.putExtra("nameText", getCurrentName(textView));
                startActivity(intent);
            });
        } else {
            permissionToAdd = true;
        }
    }

    private String getCurrentName(TextView textView){
        String username = "";
        for (String user : users) {
            username = user.equals(textView.getText()) ? user : username;
        }
        return username;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_WIFI_STATE:
            case REQUEST_CODE_CHANGE_WIFI_STATE:
            case REQUEST_CODE_CHANGE_NETWORK_STATE:
            case REQUEST_CODE_INTERNET:
            case REQUEST_CODE_ACCESS_NETWORK_STATE:
            case REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
            case REQUEST_CODE_READ_PHONE_STATE:
            case REQUEST_CODE_READ_EXTERNAL_STORAGE:
            case ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                } else {/* permission denied*/}
                return;
        }
    }
}
