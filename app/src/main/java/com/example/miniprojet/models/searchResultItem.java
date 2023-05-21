package com.example.miniprojet.models;

import com.example.miniprojet.databinding.AnnonceBinding;
import com.example.miniprojet.databinding.PersonItemBinding;

public class searchResultItem {

    public static final int RESULT_PERSON = 0, RESULT_ANNONCE = 1;
    private PersonItem person;
    private Annonce annonce;
    private PersonItemBinding containerPerson;
    private AnnonceBinding containerAnnonce;
    private int type;

    public PersonItem getPerson() {
        return person;
    }

    public void setPerson(PersonItem person) {
        this.person = person;
    }

    public Annonce getAnnonce() {
        return annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public PersonItemBinding getContainerPerson() {
        return containerPerson;
    }

    public void setContainerPerson(PersonItemBinding containerPerson) {
        this.containerPerson = containerPerson;
    }

    public AnnonceBinding getContainerAnnonce() {
        return containerAnnonce;
    }

    public void setContainerAnnonce(AnnonceBinding containerAnnonce) {
        this.containerAnnonce = containerAnnonce;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
