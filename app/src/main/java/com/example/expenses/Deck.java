package com.example.expenses;

public class Deck {

    public final int id;
    public final String name;



    public Deck(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }


    public String getName() {
        return this.name;
    }
}
