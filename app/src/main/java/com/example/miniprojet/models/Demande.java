package com.example.miniprojet.models;

public class Demande {
    private Client person;
    private Annonce annonce;
    private String title;
    private String etat;

    public Demande() {
    }

    public Demande(Client person, Annonce annonce, String title, String etat) {
        this.person = person;
        this.annonce = annonce;
        this.title = title;
        this.etat = etat;
    }

    public Client getPerson() {
        return person;
    }

    public void setPerson(Client person) {
        this.person = person;
    }

    public Annonce getAnnonce() {
        return annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
}
