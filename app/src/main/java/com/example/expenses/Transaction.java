package com.example.expenses;

import java.util.List;

public class Transaction {
    public final int id;
    public final String description;
    public final double value;
    public final Currency currencyId;
    public final Deck deckId;
    public final Member who;
    public List<Member> forWhom;



    public Transaction(int id, String description, double value, Currency currencyId, Deck deckId, Member who) {
        this.id = id;
        this.description = description;
        this.value = value;
        this.currencyId = currencyId;
        this.deckId = deckId;
        this.who = who;
    }

    public int getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public double getValue() {
        return this.value;
    }

    public Currency getCurrency() {
        return this.currencyId;
    }

    public Deck getDeckId() {
        return this.deckId;
    }

    public Member getWho() {
        return this.who;
    }

    public List<Member> getForWhom() {
        return this.forWhom;
    }

    public void setForWhom(List<Member> members) { this.forWhom = members; }
}
