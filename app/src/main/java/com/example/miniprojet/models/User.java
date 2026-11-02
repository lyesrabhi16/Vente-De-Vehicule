package com.example.miniprojet.models;

import android.content.Context;
import android.content.SharedPreferences;

public final class User {
    private static User Instance;
    private Context Ctx;
    private static final String SHARED_PREF_NAME = "sharedPref";
    private static boolean Loggedin = false;
    private static final String KEY_ID = "idClient";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NUMTEL = "numTel";
    private static final String KEY_NOM = "nomClient";
    private static final String KEY_PRENOM = "prenomClient";
    private static final String KEY_AGE = "ageClient";
    private static final String KEY_IMAGE = "imageClient";
    private static final String KEY_ADMIN = "ADMIN";
    private static final String KEY_TOKEN = "token";


    private User(Context context){
        Ctx = context.getApplicationContext(); // jamais une activité : le singleton lui survivrait
    }

    public static synchronized User getInstance(Context context){
        if (Instance == null){
            Instance = new User(context);
        }
        return Instance;
    }

    public boolean userLogin(int id,String email, String numTel, String nom, String prenom, int age, Boolean admin){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedprefs.edit();

        editor.putInt(KEY_ID, id);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NUMTEL, numTel);
        editor.putString(KEY_NOM, nom);
        editor.putString(KEY_PRENOM, prenom);
        editor.putInt(KEY_AGE, age);
        editor.putBoolean(KEY_ADMIN, admin);

        editor.apply();
        return true;

    }

    /** Réenregistre le profil (après une modification) sans perdre la session. */
    public boolean refreshProfile(int id,String email, String numTel, String nom, String prenom, int age, Boolean admin){
        String token = getToken();
        logout();
        setToken(token);
        return userLogin(id, email, numTel, nom, prenom, age, admin);
    }

    /** Le jeton de connexion rendu par le serveur ; il part dans l'en-tête Authorization de chaque requête. */
    public void setToken(String token){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedprefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getString(KEY_TOKEN, null);
    }

    /** Connecté = un profil et un jeton (une session d'avant les jetons n'en a pas : il faut se reconnecter). */
    public boolean isLoggedin(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getInt(KEY_ID, -1) != -1 && sharedprefs.getString(KEY_TOKEN, null) != null;
    }

    public boolean logout(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedprefs.edit();
        editor.clear();
        editor.apply();
        return true;
    }

    public String getNom(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getString(KEY_NOM, null);
    }
    public String getPrenom(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getString(KEY_PRENOM, null);
    }
    public String getEmail(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getString(KEY_EMAIL, null);
    }
    public String getNumtel(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getString(KEY_NUMTEL, null);
    }
    public int getID(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getInt(KEY_ID, -1);
    }

    public int getAge(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getInt(KEY_AGE, -1);
    }
    public String getImageName(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getString(KEY_IMAGE, null);
    }
    public void setImageName(String name){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedprefs.edit();
        editor.putString(KEY_IMAGE, name);
        editor.apply();
    }
    public boolean isAdmin(){
        SharedPreferences sharedprefs = Ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedprefs.getBoolean(KEY_ADMIN, false);
    }

}
