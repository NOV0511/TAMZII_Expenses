package com.example.expenses;

import java.util.List;

public class Transaction {
    private int id;
    private String description;
    private double value;
    //TODO: predelat currency na tridu
    private String currency;
    private Deck deckId;
    private Member who;
    private List<Member> forWhom;

    public Transaction(int id, String description, double value, String currency, Member who) {
        this.id = id;
        this.description = description;
        this.value = value;
        this.currency = currency;
        this.who = who;
    }

    public Transaction(int id, String description, double value, String currency, Deck deckId, Member who, List<Member> forWhom) {
        this.id = id;
        this.description = description;
        this.value = value;
        this.currency = currency;
        this.deckId = deckId;
        this.who = who;
        this.forWhom = forWhom;
    }



    public void setDescription(String description) {
        this.description += description;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setWho(Member who) {
        this.who = who;
    }

    public void setForWhom(List<Member> forWhom) {
        this.forWhom = forWhom;
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

    public String getCurrency() {
        return this.currency;
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
}
