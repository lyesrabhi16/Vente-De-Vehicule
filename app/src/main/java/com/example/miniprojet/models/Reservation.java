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

public class Reservation {
    private int idClient, idAnnonce;
    private String dateDebut, dateFin, lieuReservation, etatReservation;

    public Reservation() {
    }

    public Reservation(int idClient, int idAnnonce, String dateDebut, String dateFin, String lieuReservation) {
        this.idClient = idClient;
        this.idAnnonce = idAnnonce;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieuReservation = lieuReservation;
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

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public String getLieuReservation() {
        return lieuReservation;
    }

    public void setLieuReservation(String lieuReservation) {
        this.lieuReservation = lieuReservation;
    }
    public String getEtatReservation() {
        return etatReservation;
    }

    public void setEtatReservation(String etatReservation) {
        this.etatReservation = etatReservation;
    }



    public static void getReservation(int idAnnonce, int idClient, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlReservation(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(ctx,"failed to get reservation. "+res.getString("error"), Toast.LENGTH_LONG).show();
                        ArrayList l = new ArrayList();
                        l.add(res.get("error") );
                        Req.onError(l);
                    }

                    else{
                        JSONArray result = res.getJSONArray("result");
                        User user = User.getInstance(ctx);
                        ArrayList l = new ArrayList<>();
                        if(result.length() < 1){
                            Toast.makeText(ctx, "Reservation not found", Toast.LENGTH_SHORT).show();
                            l.add("reservation not found");
                            Req.onError(l);
                            return;
                        }
                        else{
                            JSONObject r = result.getJSONObject(0);
                            Reservation reservation = new Reservation();
                            reservation.setIdAnnonce(r.getInt("idAnnonce"));
                            reservation.setIdClient(r.getInt("idClient"));
                            reservation.setDateDebut(r.getString("dateDebut"));
                            reservation.setDateFin(r.getString("dateFin"));
                            reservation.setLieuReservation(r.getString("lieuReservation"));
                            reservation.setEtatReservation(r.getString("etatReservation"));
                            l.add(reservation);
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
    public static void RemoveReservation(int idAnnonce, int idClient, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlDelReservation(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(ctx,"failed to remove reservation. "+res.getString("error"), Toast.LENGTH_LONG).show();
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
    public static void AddReservation(Reservation reservation, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlAddReservation(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);

                    if(res.has("error")){
                        Toast.makeText(ctx,"failed to add reservation. "+res.getString("error"), Toast.LENGTH_LONG).show();
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
                Params.put("idAnnonce", reservation.getIdAnnonce()+"");
                Params.put("idClient", reservation.getIdClient()+"");
                Params.put("dateDebut", reservation.getDateDebut()+"");
                Params.put("dateFin", reservation.getDateFin()+"");
                Params.put("lieuReservation", reservation.getLieuReservation()+"");
                Params.put("etatReservation", reservation.getEtatReservation()+"");
                return Params;
            }
        };
        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }

    public static void getReservations(JSONObject filterObj, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlReservations(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);

                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to get reservations. "+res.getString("error"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList();
                                l.add(res.get("error") );
                                Req.onError(l);
                            }

                            else{
                                JSONArray result = res.getJSONArray("result");
                                ArrayList<Reservation> l = new ArrayList<Reservation>();
                                for (int i = 0; i < result.length(); i++) {

                                    JSONObject r = result.getJSONObject(i);
                                    Reservation reservation = new Reservation();
                                    reservation.setIdAnnonce(r.getInt("idAnnonce"));
                                    reservation.setIdClient(r.getInt("idClient"));
                                    reservation.setDateDebut(r.getString("dateDebut"));
                                    reservation.setDateFin(r.getString("dateFin"));
                                    reservation.setLieuReservation(r.getString("lieuReservation"));
                                    reservation.setEtatReservation(r.getString("etatReservation"));
                                    l.add(reservation);
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
