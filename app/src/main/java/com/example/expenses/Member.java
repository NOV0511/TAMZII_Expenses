package com.example.expenses;

public class Member {
    private int id;
    private String name;
    private Deck deckId;

    public Member(int id) {
        this.id = id;
    }

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
    }

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
