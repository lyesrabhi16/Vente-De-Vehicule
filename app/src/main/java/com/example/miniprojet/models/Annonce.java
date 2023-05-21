package com.example.miniprojet.models;

import android.content.Context;
import android.media.Image;
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

public class Annonce {
    Image img, userAvatar;
    int idAnnonce, idUser, annee, kilometrage;
    String prix;
    String title, type, qte , date, desc, userTitle, marque, modele, couleur, transmission, moteur, energie;

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }

    public String getPrix() {
        return prix;
    }

    public void setPrix(String prix) {
        this.prix = prix;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getMoteur() {
        return moteur;
    }

    public void setMoteur(String moteur) {
        this.moteur = moteur;
    }

    public String getEnergie() {
        return energie;
    }

    public void setEnergie(String energie) {
        this.energie = energie;
    }

    public Image getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(Image userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(String userTitle) {
        this.userTitle = userTitle;
    }

    public String getUserSubTitle() {
        return userSubTitle;
    }

    public void setUserSubTitle(String userSubTitle) {
        this.userSubTitle = userSubTitle;
    }

    String userSubTitle;

    public Annonce() {
    }


    public Image getImg() {
        return img;
    }

    public void setImg(Image img) {
        this.img = img;
    }

    public int getIdAnnonce() {
        return idAnnonce;
    }

    public void setIdAnnonce(int idAnnonce) {
        this.idAnnonce = idAnnonce;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQte() {
        return qte;
    }

    public void setQte(String qte) {
        this.qte = qte;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static void AddAnnonce(Annonce annonce, Context ctx, RequestFinished Req){
        Toast.makeText(ctx, "adding annonce...", Toast.LENGTH_SHORT).show();
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlAddAnnonce(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);

                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to add annonce. "+res.getString("error"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList();
                                l.add(res.get("error") );
                                Req.onError(l);
                            }

                            else{

                                ArrayList<PersonItem> l = new ArrayList<PersonItem>();

                                Req.onFinish(l);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx,"an error occurred.", Toast.LENGTH_SHORT).show();
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
                params.put("idClient", annonce.getIdUser()+"");
                params.put("titre", annonce.getTitle()+"");
                params.put("description", annonce.getDesc()+"");
                params.put("typeVehicule", annonce.getType()+"");
                params.put("marqueVehicule", annonce.getMarque()+"");
                params.put("modeleVehicule", annonce.getModele()+"");
                params.put("couleurVehicule", annonce.getCouleur()+"");
                params.put("transmissionVehicule", annonce.getTransmission()+"");
                params.put("kilometrageVehicule", annonce.getKilometrage()+"");
                params.put("anneeVehicule", annonce.getAnnee()+"");
                params.put("moteurVehicule", annonce.getMoteur()+"");
                params.put("energieVehicule", annonce.getEnergie()+"");
                params.put("prixVehicule", annonce.getPrix()+"");
                return params;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }

    public static void getUserAnnonces(int id, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlAnnoncesUser(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);

                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to get announcements. "+res.get("error"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList();
                                l.add(res.get("error") );
                                Req.onError(l);
                            }

                            else{
                                JSONArray resA = res.getJSONArray("result");
                                ArrayList<Annonce> l = new ArrayList<Annonce>();
                                for (int i = 0; i < resA.length(); i++) {
                                    JSONObject a = resA.getJSONObject(i);
                                    Annonce annonce = new Annonce();
                                    annonce.setIdAnnonce(a.getInt("idAnnonce"));
                                    annonce.setTitle(a.getString("titre"));
                                    annonce.setDesc(a.getString("description"));
                                    annonce.setType(a.getString("typeVehicule"));
                                    annonce.setMarque(a.getString("marqueVehicule"));
                                    annonce.setModele(a.getString("modeleVehicule"));
                                    annonce.setCouleur(a.getString("couleurVehicule"));
                                    annonce.setTransmission(a.getString("transmissionVehicule"));
                                    annonce.setKilometrage(a.getInt("kilometrageVehicule"));
                                    annonce.setAnnee(a.getInt("anneeVehicule"));
                                    annonce.setMoteur(a.getString("moteurVehicule"));
                                    annonce.setEnergie(a.getString("energieVehicule"));
                                    annonce.setPrix(a.getString("prixVehicule"));
                                    annonce.setIdUser(a.getInt("idClient"));
                                    Client.getClient(annonce.getIdUser(), ctx, new RequestFinished() {
                                        @Override
                                        public void onFinish(ArrayList args) {
                                            Client c = (Client) args.get(0);
                                            annonce.setUserTitle(c.getNom() + " " + c.getPrenom() + " ");
                                            annonce.setUserSubTitle(c.getEmail());
                                            l.add(annonce);
                                            Req.onFinish(l);
                                        }

                                        @Override
                                        public void onError(ArrayList args) {
                                            Req.onError(args);
                                        }
                                    });
                                }
                                Req.onFinish(l);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx,"an error occurred.", Toast.LENGTH_SHORT).show();
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
                params.put("idClient", id+"");
                return params;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }
    public static void getAnnonces(JSONObject filterObj, Context ctx, RequestFinished Req){
        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlAnnonces(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);

                            if(res.has("error")){
                                Toast.makeText(ctx,"failed to get announcements. "+res.get("error"), Toast.LENGTH_LONG).show();
                                ArrayList l = new ArrayList();
                                l.add(res.get("error") );
                                Req.onError(l);
                            }

                            else{
                                JSONArray resA = res.getJSONArray("result");
                                ArrayList<Annonce> l = new ArrayList<Annonce>();
                                for (int i = 0; i < resA.length(); i++) {
                                    JSONObject a = resA.getJSONObject(i);
                                    Annonce annonce = new Annonce();
                                    annonce.setIdAnnonce(a.getInt("idAnnonce"));
                                    annonce.setTitle(a.getString("titre"));
                                    annonce.setDesc(a.getString("description"));
                                    annonce.setType(a.getString("typeVehicule"));
                                    annonce.setMarque(a.getString("marqueVehicule"));
                                    annonce.setModele(a.getString("modeleVehicule"));
                                    annonce.setCouleur(a.getString("couleurVehicule"));
                                    annonce.setTransmission(a.getString("transmissionVehicule"));
                                    annonce.setKilometrage(a.getInt("kilometrageVehicule"));
                                    annonce.setAnnee(a.getInt("anneeVehicule"));
                                    annonce.setMoteur(a.getString("moteurVehicule"));
                                    annonce.setEnergie(a.getString("energieVehicule"));
                                    annonce.setPrix(a.getString("prixVehicule"));
                                    annonce.setIdUser(a.getInt("idClient"));
                                    Client.getClient(annonce.getIdUser(), ctx, new RequestFinished() {
                                        @Override
                                        public void onFinish(ArrayList args) {
                                            Client c = (Client) args.get(0);
                                            annonce.setUserTitle(c.getNom() + " " + c.getPrenom() + " ");
                                            annonce.setUserSubTitle(c.getEmail());
                                            l.add(annonce);
                                            Req.onFinish(l);
                                        }

                                        @Override
                                        public void onError(ArrayList args) {
                                            Req.onError(args);
                                        }
                                    });
                                }
                                Req.onFinish(l);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx,"an error occurred.", Toast.LENGTH_SHORT).show();
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
                if(filterObj.length()>0){
                    params.put("filterObj", filterObj.toString());
                }
                return params;
            }
        };

        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }
}
