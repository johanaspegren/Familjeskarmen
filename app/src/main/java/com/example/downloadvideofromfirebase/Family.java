package com.example.downloadvideofromfirebase;

import java.util.ArrayList;

public class Family {

    String name;                    // name of the focusperson Arja Nilsson
    ArrayList<String> categories;   // greeting is default
    ArrayList<String> authors;      // the kids

    public Family() {
    }

    public Family(String name, ArrayList<String> categories, ArrayList<String> authors) {
        this.name = name;
        this.categories = categories;
        this.authors = authors;
    }

    @Override
    public String toString() {
        return "Family{" +
                "name='" + name + '\'' +
                ", categories=" + categories +
                ", authors=" + authors +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public void setAuthors(ArrayList<String> authors) {
        this.authors = authors;
    }
}
