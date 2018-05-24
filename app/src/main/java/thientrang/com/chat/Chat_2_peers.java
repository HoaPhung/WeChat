package thientrang.com.chat;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;



/**
 * Created by THIEN TRANG on 06/05/2018.
 */

public class Chat_2_peers extends AppCompatActivity {
    static final int MESSAGE_READ=1;
    ImageView btn_send,btn_disconnect, btn_back, btn_form;
    EditText edt_msg;
    ListView listView;
    //ListView list_chat;

    boolean isMine = true;
    boolean isServer = true;

    SendRecieve sendRecieve;
    ServerClass serverClass;
    ClientClass clientClass;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    //ArrayAdapter<String> messageAdapter;
    private List<ChatMessage> chatMessages;
    private ArrayAdapter<ChatMessage> adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_2_peers_layout);
        try {
            init();
            action();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    //messageAdapter.add("other: " + tempMsg);
                    //list_chat.smoothScrollToPosition(messageAdapter.getCount() - 1);
                    ChatMessage chatMessage = new ChatMessage(tempMsg, false);
                    chatMessages.add(chatMessage);
                    adapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    });


    private void action() {
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = edt_msg.getText().toString();
                sendRecieve.write(msg.getBytes());
                isMine = true;
                ChatMessage chatMessage = new ChatMessage(edt_msg.getText().toString(), true);
                chatMessages.add(chatMessage);
                adapter.notifyDataSetChanged();
                edt_msg.setText("");
            }
        });

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(Chat_2_peers.this, "Disconnected successful", Toast.LENGTH_SHORT).show();
                        Chat_2_peers.super.onBackPressed();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(Chat_2_peers.this, "Disconnect failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Chat_2_peers.super.onBackPressed();
            }
        });

        btn_form.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Chat_2_peers.this, FormActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init() throws UnknownHostException {
        btn_send = (ImageView) findViewById(R.id.btn_send);
        btn_disconnect = (ImageView) findViewById(R.id.btn_disconnect);
        edt_msg = (EditText) findViewById(R.id.edt_msg);
        listView = (ListView) findViewById(R.id.list_chat);
        btn_back = (ImageView) findViewById(R.id.btn_back3);
        btn_form = (ImageView) findViewById(R.id.btn_form);


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);

        Intent intent = getIntent();
        String socketType = intent.getStringExtra("typeSocket");
        if (socketType.equals("server")){
            serverClass = new ServerClass();
            serverClass.execute("server");
            isServer = true;

        }
        else if (socketType.equals("client")){
            String temp = intent.getStringExtra("address");
            String address = temp.substring(1, temp.length());
            InetAddress inetAddres =  InetAddress.getByName(address);
            clientClass = new ClientClass(inetAddres);
            clientClass.execute("client");
            isServer = false;
        }

        //messageAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.layout_message, R.id.messageTextView);
        //list_chat.setAdapter(messageAdapter);
        chatMessages = new ArrayList<>();
        adapter = new MessageAdapter(this, R.layout.left_layout, chatMessages);
        listView.setAdapter(adapter);

    }


    private class SendRecieve extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        public SendRecieve(Socket skt){
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket!=null){
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes>0){
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        public void write(final byte[] bytes){
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    public class ServerClass extends AsyncTask{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class ClientClass extends AsyncTask{
        Socket socket;
        String hostAdd;
        public ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            socket=new Socket();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

