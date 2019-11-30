package com.example.expenses;

public class Member {
    public final int id;
    public final String name;
    public final Deck deckId;


    public Member(int id, String name, Deck deckId) {
        this.id = id;
        this.name = name;
        this.deckId = deckId;
    }


    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Deck getDeck() {
        return this.deckId;
    }
}
