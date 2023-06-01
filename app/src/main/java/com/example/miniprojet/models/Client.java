package com.example.miniprojet.models;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.miniprojet.Server;
import com.example.miniprojet.interfaces.RequestFinished;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Client {
    int idClient, age;
    String email, numTel, nom, prenom;

    public Client() {
    }

    public Client(int idClient, int age, String email, String numTel, String nom, String prenom) {
        this.idClient = idClient;
        this.age = age;
        this.email = email;
        this.numTel = numTel;
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public static void getClient(int id, Context ctx, RequestFinished Req){


        StringRequest req = new StringRequest(Request.Method.POST, Server.getUrlUser(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to get user info. "+res.getString("error"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList<>();
                                l.add(res.get("error"));
                                Req.onError(l);
                            }

                            else{
                                res = res.getJSONArray("user").getJSONObject(0);
                                Client c = new Client();
                                c.setNom(res.getString("nomClient"));
                                c.setPrenom(res.getString("prenomClient"));
                                c.setAge(res.getInt("ageClient"));
                                c.setEmail(res.getString("email"));
                                c.setNumTel(res.getString("numTel"));
                                c.setIdClient(res.getInt("idClient"));

                                ArrayList<Client> l = new ArrayList<Client>();
                                l.add(c);
                                Req.onFinish(l);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx,"Response Handling error :  "+e, Toast.LENGTH_SHORT).show();
                            ArrayList l = new ArrayList<>();
                            l.add(e);
                            Req.onError(l);

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx,"connection error."+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        ArrayList l = new ArrayList<>();
                        l.add(error);
                        Req.onError(l);
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String,String>();
                params.put("idClient", id + "");
                return params;

            }

            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(req);
    }

    public static void delClient(int id, Context ctx, RequestFinished Req){


        StringRequest req = new StringRequest(Request.Method.POST, Server.getUrlDelUser(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to delete user. "+res.getString("error"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList<>();
                                l.add(res.get("error"));
                                Req.onError(l);
                            }

                            else{
                                Toast.makeText(ctx, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                ArrayList l = new ArrayList();
                                l.add("success");
                                Req.onFinish(l);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx,"Response Handling error :  "+e, Toast.LENGTH_SHORT).show();
                            ArrayList l = new ArrayList<>();
                            l.add(e);
                            Req.onError(l);

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx,"connection error."+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        ArrayList l = new ArrayList<>();
                        l.add(error);
                        Req.onError(l);
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String,String>();
                params.put("idClient", id + "");
                return params;

            }

            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(req);
    }

    public static void updateClient(Client client, Context ctx, RequestFinished Req){


        StringRequest req = new StringRequest(Request.Method.POST, Server.getUrlUpdateUser(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to update user info. "+res.getString("error"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList<>();
                                l.add(res.get("error"));
                                Req.onError(l);
                            }

                            else{
                                Toast.makeText(ctx, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                ArrayList l = new ArrayList();
                                l.add("success");
                                Req.onFinish(l);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx,"Response Handling error :  "+e, Toast.LENGTH_SHORT).show();
                            ArrayList l = new ArrayList<>();
                            l.add(e);
                            Req.onError(l);

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx,"connection error."+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        ArrayList l = new ArrayList<>();
                        l.add(error);
                        Req.onError(l);
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String,String>();
                params.put("idClient", client.getIdClient() + "");
                params.put("nomClient", client.getNom() + "");
                params.put("prenomClient", client.getPrenom() + "");
                params.put("ageClient", client.getAge() + "");
                params.put("email", client.getEmail() + "");
                params.put("numTel", client.getNumTel() + "");
                return params;

            }

            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(req);
    }

}
