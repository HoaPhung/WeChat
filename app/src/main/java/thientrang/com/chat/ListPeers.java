package thientrang.com.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by THIEN TRANG on 05/05/2018.
 */

public class ListPeers extends AppCompatActivity {
    ImageView btn_search, btn_back;
    ListView listView;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNamearray;
    WifiP2pDevice[] deviceArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_peers_layout);
        init();
        action();
    }

    private void action() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ListPeers.this, "Searching....", Toast.LENGTH_SHORT).show();

                    }
                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(ListPeers.this, "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListPeers.super.onBackPressed();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device=deviceArray[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress=device.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void init() {
        btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_search = (ImageView) findViewById(R.id.btn_search);
        listView = (ListView) findViewById(R.id.listpeers);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);

        mReceiver = new WifiDirectBroadcastReceiver(mManager,mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }


    // Find device is server or client send "server" or "client" for next activity
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            if (info.groupFormed && info.isGroupOwner) {
                Intent intent = new Intent(ListPeers.this, Chat_2_peers.class);
                intent.putExtra("typeSocket","server");
                startActivity(intent);
                Toast.makeText(ListPeers.this, "Host", Toast.LENGTH_SHORT).show();
            }else if (info.groupFormed){
                String address = groupOwnerAddress.toString();
                Intent intent = new Intent(ListPeers.this, Chat_2_peers.class);
                intent.putExtra("typeSocket","client");
                intent.putExtra("address",address);
                startActivity(intent);
                Toast.makeText(ListPeers.this, "Client", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Get list peer in wifi and update in list view
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersList) {
            if (!peersList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peersList.getDeviceList());
                deviceNamearray = new String[peersList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peersList.getDeviceList().size()];
                int index=0;
                for (WifiP2pDevice device:peersList.getDeviceList()){
                    deviceNamearray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                ArrayAdapter<String>adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNamearray){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView tv = (TextView)view. findViewById(android.R.id.text1);
                        tv.setTextColor(Color.parseColor("#515151"));
                        return view;
                    }
                };
                listView.setAdapter(adapter);
            }
            if (peers.size()==0){
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}
