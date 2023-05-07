package com.example.miniprojet.models;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.miniprojet.MainActivity;
import com.example.miniprojet.Server;
import com.example.miniprojet.interfaces.RequestFinished;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private int id,sender, reciever;
    private String contenu, etat;
    private static ArrayList<Message> messages;
    public Message(int sender, int reciever, String contenu, String etat) {
        this.sender = sender;
        this.reciever = reciever;
        this.contenu = contenu;
        this.etat = etat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getReciever() {
        return reciever;
    }

    public void setReciever(int reciever) {
        this.reciever = reciever;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
    public static void getMessages (int id, Context ctx, RequestFinished req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlMessages(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);

                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to get messages. "+res.getString("message"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList();
                                l.add(res.get("error") );
                                req.onError(l);
                            }

                            else{
                                JSONArray result = res.getJSONArray("result");
                                User user = User.getInstance(ctx);
                                ArrayList<PersonItem> l = new ArrayList<PersonItem>();
                                ArrayList<Integer> ids = new ArrayList<>();
                                for (int i = 0; i < result.length(); i++) {

                                    JSONObject r = result.getJSONObject(i);
                                    if (ids.contains(r.getInt("idClient")))
                                        continue;
                                    else {

                                        if (!r.get("idClient").equals(user.getID())) {

                                            PersonItem item = new PersonItem();
                                            item.setUserID(r.getInt("idClient"));
                                            item.setTitle(r.getString("nomClient")+" "+r.getString("prenomClient")+" ");
                                            item.setSubTitle("");
                                            if (user.getID() == r.getInt("idClient_sender")) {
                                                item.setSubTitle("You: ");
                                            }
                                            item.setSubTitle(item.getSubTitle() + "" + r.getString("contenuMessage"));
                                            item.setData(r.getString("date"));
                                            l.add(item);
                                            ids.add(item.getUserID());
                                        }
                                    }
                                }
                                Toast.makeText(ctx, "Messages:\n"+messages, Toast.LENGTH_SHORT).show();
                                req.onFinish(l);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx,"an error occurred.", Toast.LENGTH_SHORT).show();
                            ArrayList l = new ArrayList<>();
                            l.add(e.getMessage());
                            req.onError(l);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx,"Connection Error", Toast.LENGTH_SHORT).show();
                        ArrayList l = new ArrayList();
                        l.add(error.getMessage());
                        req.onError(l);
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String,String>();
                params.put("idClient", id+"");
                return params;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }
}
