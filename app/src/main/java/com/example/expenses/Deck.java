package com.example.expenses;

public class Deck {

    public final long id;
    public final String name;



    public Deck(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return this.id;
    }


    public String getName() {
        return this.name;
    }
}
