package com.example.expenses;

import java.util.List;

public class Transaction {
    public final long id;
    public final String description;
    public final double value;
    public final Currency currencyId;
    public final Deck deckId;
    public final Member who;
    public List<Member> forWhom;
    public final double realValue;



    public Transaction(long id, String description, double value, Currency currencyId, Deck deckId, Member who, double realValue) {
        this.id = id;
        this.description = description;
        this.value = value;
        this.currencyId = currencyId;
        this.deckId = deckId;
        this.who = who;
        this.realValue = realValue;
    }

    public long getId() {
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

    public double getRealValue() { return  this.realValue; }

    public void setForWhom(List<Member> members) { this.forWhom = members; }
}
