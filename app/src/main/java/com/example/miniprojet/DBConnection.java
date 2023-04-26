package com.example.miniprojet;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBConnection{
    private static String urlServer   = "http://192.168.43.147:5000";//chemin du serveur
    private static String urlLogin = urlServer + "/signin";
    private static  String urlRegister = urlServer + "/signup";
    private static  String urlSearch = urlServer + "/search";





    public static String getUrlLogin() {
        return urlLogin;
    }

    public static void setUrlLogin(String urlLogin) {
        DBConnection.urlLogin = urlLogin;
    }

    public static String getUrlServer() {
        return urlServer;
    }

    public static void setUrlServer(String urlServer) {
        DBConnection.urlServer = urlServer;
    }

    public DBConnection(){

    }


    public static String getUrlRegister() {
        return urlRegister;
    }

    public static void setUrlRegister(String urlRegister) {
        DBConnection.urlRegister = urlRegister;
    }

    public static String getUrlSearch() {
        return urlSearch;
    }

    public static void setUrlSearch(String urlSearch) {
        DBConnection.urlSearch = urlSearch;
    }
}

