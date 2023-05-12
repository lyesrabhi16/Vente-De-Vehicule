package com.example.miniprojet.ui.messages.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.miniprojet.Server;
import com.example.miniprojet.SocketClient;
import com.example.miniprojet.adapters.ChatItemAdapter;
import com.example.miniprojet.databinding.ActivityChatBinding;
import com.example.miniprojet.models.ChatItem;
import com.example.miniprojet.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding CBind;
    private int R_userID;
    private String R_userName;

    private ArrayList chatItemsList;

    private ChatItemAdapter chatItemAdapter;

    private Socket socket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        CBind = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(CBind.getRoot());

        R_userID = getIntent().getIntExtra("userID", -1);
        R_userName = getIntent().getStringExtra("AccountName");

        CBind.buttonAccount.setText(R_userName);

        chatItemsList = new ArrayList<ChatItem>();

        chatItemAdapter = new ChatItemAdapter(chatItemsList);
        CBind.recylcerChat.setAdapter(chatItemAdapter);
        User user = User.getInstance(getApplicationContext());
        loadMessages(user.getID(), R_userID);

        socket = SocketClient.getSocket();
        socketListenersOff();
        socketListenersOn();


        CBind.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        CBind.buttonAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ChatActivity.this, "To be implemented", Toast.LENGTH_SHORT).show();
                /*
                Intent account = new Intent(getApplicationContext(), AccountActivity.class);
                account.putExtra("userID", R_userID);
                startActivity(account);
                 */
            }
        });

        CBind.textinputLayoutSend.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = CBind.edittextMessage.getText().toString().trim();
                if( msg.isEmpty() ) return;
                User user = User.getInstance(getApplicationContext());
                JSONObject message = new JSONObject();
                try {
                    message.put("idClient_sender", user.getID());
                    message.put("idClient_reciever", R_userID);
                    message.put("contenuMessage", msg);
                    message.put("etatMessage", "sending");
                    message.put("date", getDate("yyyy-MM-dd HH:mm:ss") );
                    socket.emit("message", message);
                    CBind.edittextMessage.setText("");

                } catch (JSONException e) {
                    Toast.makeText(CBind.getRoot().getContext(), "failed to send message", Toast.LENGTH_SHORT);
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        socketListenersOn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        socketListenersOff();
    }

    public void socketListenersOn(){
        if(socket.hasListeners("message")) return;

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject msg = new JSONObject(args[0].toString());
                            int id = (Integer) args[1];


                            ChatItem item = new ChatItem();
                            item.setHeader(R_userName);
                            item.setMessage(msg.getString("contenuMessage"));
                            item.setFooter(msg.getString("etatMessage"));
                            /*String[] d = item.getFooter().split(" ");
                            String date = d[0];
                            if(date.equals(getDate("dd-MM-yyyy"))){
                                String time = d[1];
                                item.setFooter(time);
                            }
                            else{
                                item.setFooter(date);
                            }

                             */

                            chatItemsList.add(item);
                            chatItemAdapter.setList(chatItemsList);
                            CBind.recylcerChat.setAdapter(chatItemAdapter);
                            CBind.recylcerChat.smoothScrollToPosition(chatItemsList.size()-1);
                            socket.emit("message-received", id);



                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "failed to receive message", Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });
        socket.on("message-sent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject msg = null;
                        try {
                            msg = new JSONObject(args[0].toString());
                            ChatItem item = new ChatItem();
                            item.setHeader(R_userName);
                            item.setMessage(msg.getString("contenuMessage"));
                            item.setFooter(msg.getString("etatMessage"));
                            chatItemsList.add(item);
                            chatItemAdapter.setList(chatItemsList);
                            CBind.recylcerChat.setAdapter(chatItemAdapter);
                            CBind.recylcerChat.smoothScrollToPosition(chatItemsList.size()-1);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
            }
        });
    }
    public void socketListenersOff(){
        socket.off("message");
    }

    public void loadMessages(int userID1, int userID2){

        CBind.progressBar2.setVisibility(View.VISIBLE);

        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlMessages(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CBind.progressBar2.setVisibility(View.GONE);
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(getApplicationContext(), "error: "+ res.get("error"), Toast.LENGTH_LONG).show();
                    }
                    else{
                        chatItemsList = new ArrayList<ChatItem>();
                        JSONArray resArr = res.getJSONArray("result");
                        for (int i = 0; i < resArr.length(); i++) {
                            JSONObject r = resArr.getJSONObject(i);

                            ChatItem item = new ChatItem();
                            item.setHeader(R_userName);
                            item.setMessage(r.getString("contenuMessage"));
                            item.setFooter(r.getString("etatMessage"));
                            /*String[] d = item.getFooter().split(" ");
                            String date = d[0];
                            if(date.equals(getDate("dd-MM-yyyy"))){
                                String time = d[1];
                                item.setFooter(time);
                            }
                            else{
                                item.setFooter(date);
                            }

                             */

                            chatItemsList.add(item);
                        }
                        chatItemAdapter.setList(chatItemsList);
                        CBind.recylcerChat.setAdapter(chatItemAdapter);
                        if (chatItemsList.size() > 0)
                          CBind.recylcerChat.smoothScrollToPosition(chatItemsList.size()-1);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Response error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CBind.progressBar2.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Connection error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> Params = new HashMap<>();
                Params.put("idClient1", ""+userID1);
                Params.put("idClient2", ""+userID2);
                return Params;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(getApplicationContext());
        reqQ.add(Sreq);
    }
    public String getDate(String format){
        String date;
        date = new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
        return date;
    }
}