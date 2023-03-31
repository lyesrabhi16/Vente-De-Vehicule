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
    private static String urlBDD   = "http://10.2.60.180/miniprojet";//chemin de la base de donnees
    private static String urlLogin = urlBDD + "/login.php";
    private static  String urlRegister = urlBDD + "/register.php";




    public static String getUrlLogin() {
        return urlLogin;
    }

    public static void setUrlLogin(String urlLogin) {
        DBConnection.urlLogin = urlLogin;
    }

    public static String getUrlBDD() {
        return urlBDD;
    }

    public static void setUrlBDD(String urlBDD) {
        DBConnection.urlBDD = urlBDD;
    }

    public DBConnection(){

    }


    public static String getUrlRegister() {
        return urlRegister;
    }

    public static void setUrlRegister(String urlRegister) {
        DBConnection.urlRegister = urlRegister;
    }
}

