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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RendezVous {
    private int idClient, idAnnonce;
    private String dateRendezVous, lieuRendezVous, etatRendezVous;

    public RendezVous() {
    }

    public RendezVous(int idClient, int idAnnonce, String dateRendezVous, String lieuRendezVous, String etatRendezVous) {
        this.idClient = idClient;
        this.idAnnonce = idAnnonce;
        this.dateRendezVous = dateRendezVous;
        this.lieuRendezVous = lieuRendezVous;
        this.etatRendezVous = etatRendezVous;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdAnnonce() {
        return idAnnonce;
    }

    public void setIdAnnonce(int idAnnonce) {
        this.idAnnonce = idAnnonce;
    }

    public String getDateRendezVous() {
        return dateRendezVous;
    }

    public void setDateRendezVous(String dateRendezVous) {
        this.dateRendezVous = dateRendezVous;
    }

    public String getLieuRendezVous() {
        return lieuRendezVous;
    }

    public void setLieuRendezVous(String lieuRendezVous) {
        this.lieuRendezVous = lieuRendezVous;
    }

    public String getEtatRendezVous() {
        return etatRendezVous;
    }

    public void setEtatRendezVous(String etatRendezVous) {
        this.etatRendezVous = etatRendezVous;
    }
    public static void getRendezVous(int idAnnonce, int idClient, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlRendezVous(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(ctx,"failed to get RendezVous. "+res.getString("error"), Toast.LENGTH_LONG).show();
                        ArrayList l = new ArrayList();
                        l.add(res.get("error") );
                        Req.onError(l);
                    }

                    else{
                        JSONArray result = res.getJSONArray("result");
                        User user = User.getInstance(ctx);
                        ArrayList l = new ArrayList<>();
                        if(result.length() < 1){
                            Toast.makeText(ctx, "RendezVous not found", Toast.LENGTH_SHORT).show();
                            l.add("RendezVous not found");
                            Req.onError(l);
                            return;
                        }
                        else{
                            JSONObject r = result.getJSONObject(0);
                            RendezVous rendezVous = new RendezVous();
                            rendezVous.setIdAnnonce(r.getInt("idAnnonce"));
                            rendezVous.setIdClient(r.getInt("idClient"));
                            rendezVous.setDateRendezVous(r.getString("dateRendezVous"));
                            rendezVous.setLieuRendezVous(r.getString("lieuRendezVous"));
                            rendezVous.setEtatRendezVous(r.getString("etatRendezVous"));
                            l.add(rendezVous);
                            Req.onFinish(l);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ctx,"Response Handling Error.", Toast.LENGTH_SHORT).show();
                    ArrayList l = new ArrayList<>();
                    l.add(e.getMessage());
                    Req.onError(l);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx,"Connection Error", Toast.LENGTH_SHORT).show();
                ArrayList l = new ArrayList();
                l.add(error.getMessage());
                Req.onError(l);
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> Params = new HashMap<>();
                Params.put("idAnnonce", idAnnonce+"");
                Params.put("idClient", idClient+"");
                return Params;
            }
        };
        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }
    public static void RemoveRendezVous(int idAnnonce, int idClient, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlDelRendezVous(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(ctx,"failed to remove RendezVous. "+res.getString("error"), Toast.LENGTH_LONG).show();
                        ArrayList l = new ArrayList();
                        l.add(res.get("error") );
                        Req.onError(l);
                    }

                    else{
                        ArrayList l = new ArrayList<>();
                        Req.onFinish(l);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ctx,"Response Handling Error.", Toast.LENGTH_SHORT).show();
                    ArrayList l = new ArrayList<>();
                    l.add(e.getMessage());
                    Req.onError(l);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx,"Connection Error", Toast.LENGTH_SHORT).show();
                ArrayList l = new ArrayList();
                l.add(error.getMessage());
                Req.onError(l);
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> Params = new HashMap<>();
                Params.put("idAnnonce", idAnnonce+"");
                Params.put("idClient", idClient+"");
                return Params;
            }
        };
        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }
    public static void AddRendezVous(RendezVous rendezVous, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlAddRendezVous(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(ctx,"failed to add RendezVous. "+res.getString("error"), Toast.LENGTH_LONG).show();
                        ArrayList l = new ArrayList();
                        l.add(res.get("error") );
                        Req.onError(l);
                    }

                    else{
                        ArrayList l = new ArrayList<>();
                        Req.onFinish(l);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ctx,"Response Handling Error.", Toast.LENGTH_SHORT).show();
                    ArrayList l = new ArrayList<>();
                    l.add(e.getMessage());
                    Req.onError(l);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx,"Connection Error", Toast.LENGTH_SHORT).show();
                ArrayList l = new ArrayList();
                l.add(error.getMessage());
                Req.onError(l);
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> Params = new HashMap<>();
                Params.put("idAnnonce", rendezVous.getIdAnnonce()+"");
                Params.put("idClient", rendezVous.getIdClient()+"");
                Params.put("dateRendezVous", rendezVous.getDateRendezVous()+"");
                Params.put("lieuRendezVous", rendezVous.getLieuRendezVous()+"");
                Params.put("etatRendezVous", rendezVous.getEtatRendezVous()+"");
                return Params;
            }
        };
        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }

    public static void updateRendezVous(RendezVous rendezVous, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlUpdateRendezVous(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(ctx,"failed to update RendezVous. "+res.getString("error"), Toast.LENGTH_LONG).show();
                        ArrayList l = new ArrayList();
                        l.add(res.get("error") );
                        Req.onError(l);
                    }

                    else{
                        ArrayList l = new ArrayList<>();
                        Req.onFinish(l);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ctx,"Response Handling Error.", Toast.LENGTH_SHORT).show();
                    ArrayList l = new ArrayList<>();
                    l.add(e.getMessage());
                    Req.onError(l);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx,"Connection Error", Toast.LENGTH_SHORT).show();
                ArrayList l = new ArrayList();
                l.add(error.getMessage());
                Req.onError(l);
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> Params = new HashMap<>();
                Params.put("idAnnonce", rendezVous.getIdAnnonce()+"");
                Params.put("idClient", rendezVous.getIdClient()+"");
                Params.put("dateRendezVous", rendezVous.getDateRendezVous()+"");
                Params.put("lieuRendezVous", rendezVous.getLieuRendezVous()+"");
                Params.put("etatRendezVous", rendezVous.getEtatRendezVous()+"");
                return Params;
            }
        };
        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }
    public static void getAllRendezVous(JSONObject filterObj, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlAllRendezVous(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);

                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to get RendezVous. "+res.getString("error"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList();
                                l.add(res.get("error") );
                                Req.onError(l);
                            }

                            else{
                                JSONArray result = res.getJSONArray("result");
                                ArrayList<RendezVous> l = new ArrayList<RendezVous>();
                                for (int i = 0; i < result.length(); i++) {

                                    JSONObject r = result.getJSONObject(i);
                                    RendezVous rendezVous = new RendezVous();
                                    rendezVous.setIdAnnonce(r.getInt("idAnnonce"));
                                    rendezVous.setIdClient(r.getInt("idClient"));
                                    rendezVous.setDateRendezVous(r.getString("dateRendezVous"));
                                    rendezVous.setLieuRendezVous(r.getString("lieuRendezVous"));
                                    rendezVous.setEtatRendezVous(r.getString("etatRendezVous"));
                                    l.add(rendezVous);
                                }
                                Req.onFinish(l);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx,"Response Handling Error.", Toast.LENGTH_SHORT).show();
                            ArrayList l = new ArrayList<>();
                            l.add(e.getMessage());
                            Req.onError(l);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx,"Connection Error", Toast.LENGTH_SHORT).show();
                        ArrayList l = new ArrayList();
                        l.add(error.getMessage());
                        Req.onError(l);
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String,String>();
                params.put("filterObj", filterObj.toString()+"");
                return params;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }
}
