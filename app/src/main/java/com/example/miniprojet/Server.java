package com.example.miniprojet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.miniprojet.interfaces.RequestFinished;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static String IP = "192.168.137.1";
    private static String urlServer   = "http://"+IP+":5000";//chemin du serveur
    private static String urlLogin = urlServer + "/signin";
    private static  String urlRegister = urlServer + "/signup";
    private static String urlUser = urlServer + "/user";
    private static String urlDelUser = urlUser + "/remove";
    private static String urlUpdateUser = urlUser + "/update";
    private static  String urlSearch = urlServer + "/search";
    private static String urlChats = urlServer +"/chats";
    private static String urlMessages = urlServer +"/messages";
    private static String urlAnnonce = urlServer +"/annonce";
    private static String urlAnnonces = urlServer +"/annonces";
    private static String urlAnnoncesUser = urlAnnonces +"/user";

    private static String urlAddAnnonce = urlAnnonce +"/add";
    private static String urlUpdateAnnonce = urlAnnonce +"/update";
    private static String urlDelAnnonce = urlAnnonce +"/remove";

    private static String urlReservation = urlServer + "/reservation";
    private static String urlReservations = urlServer + "/reservations";
    private static String urlAddReservation = urlReservation + "/add";
    private static String urlUpdateReservation = urlReservation + "/update";
    private static String urlDelReservation = urlReservation + "/remove";
    private static String urlRendezVous = urlServer + "/rendezvous";
    private static String urlAllRendezVous = urlRendezVous + "/all";
    private static String urlAddRendezVous = urlRendezVous + "/add";
    private static String urlUpdateRendezVous = urlRendezVous + "/update";
    private static String urlDelRendezVous = urlRendezVous + "/remove";

    private static String urlUpload = urlServer + "/upload";
    private static String urlUploadImage = urlUpload+"/image";
    private static String urlDownload = urlServer+"/download";
    private static String urlDownloadImage = urlDownload+"/image";

    public static final String TYPE_IMAGE_AVATAR = "AVATAR";
    public static final String TYPE_IMAGE_ANNONCE = "ANNONCE";


    public static String getUrlReservation() {
        return urlReservation;
    }

    public static void setUrlReservation(String urlReservation) {
        Server.urlReservation = urlReservation;
    }

    public static String getUrlReservations() {
        return urlReservations;
    }

    public static void setUrlReservations(String urlReservations) {
        Server.urlReservations = urlReservations;
    }

    public static String getUrlAddReservation() {
        return urlAddReservation;
    }

    public static void setUrlAddReservation(String urlAddReservation) {
        Server.urlAddReservation = urlAddReservation;
    }

    public static String getUrlDelReservation() {
        return urlDelReservation;
    }

    public static void setUrlDelReservation(String urlDelReservation) {
        Server.urlDelReservation = urlDelReservation;
    }

    public static String getUrlRendezVous() {
        return urlRendezVous;
    }

    public static void setUrlRendezVous(String urlRendezVous) {
        Server.urlRendezVous = urlRendezVous;
    }

    public static String getUrlAllRendezVous() {
        return urlAllRendezVous;
    }

    public static void setUrlAllRendezVous(String urlAllRendezVous) {
        Server.urlAllRendezVous = urlAllRendezVous;
    }

    public static String getUrlAddRendezVous() {
        return urlAddRendezVous;
    }

    public static void setUrlAddRendezVous(String urlAddRendezVous) {
        Server.urlAddRendezVous = urlAddRendezVous;
    }

    public static String getUrlDelRendezVous() {
        return urlDelRendezVous;
    }

    public static void setUrlDelRendezVous(String urlDelRendezVous) {
        Server.urlDelRendezVous = urlDelRendezVous;
    }

    public static String getUrlUpload() {
        return urlUpload;
    }

    public static void setUrlUpload(String urlUpload) {
        Server.urlUpload = urlUpload;
    }

    public static String getUrlUploadImage() {
        return urlUploadImage;
    }

    public static void setUrlUploadImage(String urlUploadImage) {
        Server.urlUploadImage = urlUploadImage;
    }

    public static String getUrlDownload() {
        return urlDownload;
    }

    public static void setUrlDownload(String urlDownload) {
        Server.urlDownload = urlDownload;
    }

    public static String getUrlDownloadImage() {
        return urlDownloadImage;
    }

    public static void setUrlDownloadImage(String urlDownloadImage) {
        Server.urlDownloadImage = urlDownloadImage;
    }

    public static String getUrlAnnonce() {
        return urlAnnonce;
    }

    public static void setUrlAnnonce(String urlAnnonce) {
        Server.urlAnnonce = urlAnnonce;
    }

    public static String getUrlLogin() {
        return urlLogin;
    }

    public static void setUrlLogin(String urlLogin) {
        Server.urlLogin = urlLogin;
    }

    public static String getUrlServer() {
        return urlServer;
    }

    public static void setUrlServer(String urlServer) {
        Server.urlServer = urlServer;
    }

    public Server(){

    }


    public static String getUrlRegister() {
        return urlRegister;
    }

    public static void setUrlRegister(String urlRegister) {
        Server.urlRegister = urlRegister;
    }

    public static String getUrlSearch() {
        return urlSearch;
    }

    public static void setUrlSearch(String urlSearch) {
        Server.urlSearch = urlSearch;
    }

    public static String getUrlMessages() {
        return urlMessages;
    }

    public static void setUrlMessages(String urlMessages) {
        Server.urlMessages = urlMessages;
    }

    public static String getUrlChats() {
        return urlChats;
    }

    public static void setUrlChats(String urlChats) {
        Server.urlChats = urlChats;
    }

    public static String getUrlAddAnnonce() {
        return urlAddAnnonce;
    }

    public static void setUrlAddAnnonce(String urlAddAnnonce) {
        Server.urlAddAnnonce = urlAddAnnonce;
    }

    public static String getUrlAnnonces() {
        return urlAnnonces;
    }

    public static void setUrlAnnonces(String urlAnnonces) {
        Server.urlAnnonces = urlAnnonces;
    }

    public static String getUrlAnnoncesUser() {
        return urlAnnoncesUser;
    }

    public static void setUrlAnnoncesUser(String urlAnnoncesUser) {
        Server.urlAnnoncesUser = urlAnnoncesUser;
    }

    public static String getUrlDelAnnonce() {
        return urlDelAnnonce;
    }

    public static void setUrlDelAnnonce(String urlDelAnnonce) {
        Server.urlDelAnnonce = urlDelAnnonce;
    }

    public static String getUrlUser() {
        return urlUser;
    }

    public static void setUrlUser(String urlUser) {
        Server.urlUser = urlUser;
    }

    public static String saveBitmap(Context context, String fileName, Bitmap bitmap) {
        File imageDir = new File(context.getFilesDir(), "Images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        File imageFile = new File(imageDir, fileName + ".jpeg");
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return fileName+".jpeg";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Bitmap getBitmap(Context context, String fileName) {
        File imageDir = new File(context.getFilesDir(), "Images");
        if (!imageDir.exists()) {
            return null;
        }
        File imageFile = new File(imageDir, fileName);
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public static String ImageToBase64(Bitmap img, Context ctx){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imgBytes = stream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }
    public static Bitmap ImageFromBase64(String imgB64, Context ctx){
        byte[] decodedString = Base64.decode(imgB64, Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return  image;
    }
    public static void sendImageToserver(JSONObject img, Context ctx){

        /*
        img object must have :
            imgB64 : img in base 64
            id     : id of image/user/annonce
            format : image format (exp : jpeg)
            type   : predifend type of image (exp : TYPE_IMAGE_AVATAR)
            index : index of image (if type is annnonce)
        */



        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlUploadImage(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(ctx, "image uploaded.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx, "error : "+error, Toast.LENGTH_SHORT).show();
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> Params = new HashMap<>();
                try {
                    Params.put("imgB64", img.getString("imgB64"));
                    Params.put("userID", img.getString("id")+"");
                    Params.put("format", img.getString("format"));
                    Params.put("type", img.getString("type"));
                    if(img.getString("type")=="ANNONCE"){
                        Params.put("index", img.getString("index"));
                    }
                } catch (JSONException e) {
                    Toast.makeText(ctx, "Error Sending Image", Toast.LENGTH_SHORT).show();
                }
                return Params;
            }
        };
        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }
    public static void getImage(String imgName, Context ctx, RequestFinished Req ){


        StringRequest Sreq = new StringRequest(Request.Method.POST, Server.getUrlDownloadImage(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if(res.has("error")){
                                //Toast.makeText(ctx, "Error: "+res.getString("error"), Toast.LENGTH_SHORT).show();
                                ArrayList l = new ArrayList();
                                l.add(res.get("error"));
                                Req.onError(l);
                            }
                            else{
                                ArrayList l = new ArrayList();
                                l.add(ImageFromBase64(res.getString("imgB64"), ctx));
                                Req.onFinish(l);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx, "Response Handling Error : "+e, Toast.LENGTH_SHORT).show();
                            ArrayList l = new ArrayList();
                            l.add(e);
                            Req.onError(l);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ArrayList l = new ArrayList();
                        l.add(error);
                        Req.onError(l);
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> Params = new HashMap<>();
                Params.put("imgName", imgName);
                return Params;
            }
        };
        RequestQueue reqQ = Volley.newRequestQueue(ctx);
        reqQ.add(Sreq);
    }

    public static String getUrlDelUser() {
        return urlDelUser;
    }

    public static void setUrlDelUser(String urlDelUser) {
        Server.urlDelUser = urlDelUser;
    }

    public static String getUrlUpdateUser() {
        return urlUpdateUser;
    }

    public static void setUrlUpdateUser(String urlUpdateUser) {
        Server.urlUpdateUser = urlUpdateUser;
    }

    public static String getUrlUpdateRendezVous() {
        return urlUpdateRendezVous;
    }

    public static void setUrlUpdateRendezVous(String urlUpdateRendezVous) {
        Server.urlUpdateRendezVous = urlUpdateRendezVous;
    }

    public static String getUrlUpdateReservation() {
        return urlUpdateReservation;
    }

    public static void setUrlUpdateReservation(String urlUpdateReservation) {
        Server.urlUpdateReservation = urlUpdateReservation;
    }

    public static String getUrlUpdateAnnonce() {
        return urlUpdateAnnonce;
    }

    public static void setUrlUpdateAnnonce(String urlUpdateAnnonce) {
        Server.urlUpdateAnnonce = urlUpdateAnnonce;
    }
}

