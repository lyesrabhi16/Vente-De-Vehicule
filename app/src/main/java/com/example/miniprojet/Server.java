package com.example.miniprojet;

public class Server {
    private static String IP = "192.168.137.1";
    private static String urlServer   = "http://"+IP+":5000";//chemin du serveur
    private static String urlLogin = urlServer + "/signin";
    private static  String urlRegister = urlServer + "/signup";
    private static String urlUser = urlServer + "/user";
    private static  String urlSearch = urlServer + "/search";
    private static String urlChats = urlServer +"/chats";
    private static String urlMessages = urlServer +"/messages";
    private static String urlAnnonce = urlServer +"/annonce";
    private static String urlAnnonces = urlServer +"/annonces";
    private static String urlAnnoncesUser = urlAnnonces +"/user";

    private static String urlAddAnnonce = urlAnnonce +"/add";
    private static String urlDelAnnonce = urlAnnonce +"/remove";
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
}

