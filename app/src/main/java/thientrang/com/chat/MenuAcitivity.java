package thientrang.com.chat;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by THIEN TRANG on 06/05/2018.
 */

public class MenuAcitivity extends AppCompatActivity {
    Button btn_chatroom;
    Spinner btn_lang;
    TextView name_device;
    ArrayList<String> lang;
    ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        init();
        action();

    }

    private void action() {
        btn_chatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuAcitivity.this, ListPeers.class);
                startActivity(intent);
            }
        });
    }

    private void init() {
        btn_chatroom= findViewById(R.id.btn_chatroom);
        name_device = findViewById(R.id.name_device);
        name_device.setText(getDeviceName());
        btn_lang = (Spinner) findViewById(R.id.btn_language);
        lang= new ArrayList<>();
        lang.add("");
        lang.add("English");
        lang.add("Vietnamese");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, lang);
        btn_lang.setAdapter(arrayAdapter);
   }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
