package com.example.miniprojet.models;

import android.media.Image;

public class Annonce {
    Image img, userAvatar;
    int idAnnonce, idUser;
    String title;
    String type;
    String qte;
    String date;
    String desc;
    String userTitle;

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

    public Annonce(Image img, Image userAvatar, int idAnnonce, int idUser, String title, String type, String qte, String date, String desc, String userTitle, String userSubTitle) {
        this.img = img;
        this.userAvatar = userAvatar;
        this.idAnnonce = idAnnonce;
        this.idUser = idUser;
        this.title = title;
        this.type = type;
        this.qte = qte;
        this.date = date;
        this.desc = desc;
        this.userTitle = userTitle;
        this.userSubTitle = userSubTitle;
    }

    public Annonce(Image img, int idAnnonce, int idUser, String title, String type, String qte, String date, String desc) {
        this.img = img;
        this.idAnnonce = idAnnonce;
        this.idUser = idUser;
        this.title = title;
        this.type = type;
        this.qte = qte;
        this.date = date;
        this.desc = desc;
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
}
