package com.example.miniprojet;

public class Server {
    private static String urlServer   = "http://192.168.43.147:5000";//chemin du serveur
    private static String urlLogin = urlServer + "/signin";
    private static  String urlRegister = urlServer + "/signup";
    private static  String urlSearch = urlServer + "/search";





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
}

