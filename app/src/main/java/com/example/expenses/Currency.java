package com.example.expenses;

public class Currency {

    private final long id;
    private final String code;
    private final String name;
    private final double amount;
    private final double rate;
    private final String country;


    Currency(long id, String code, String name, double amount, double rate, String country) {

        this.id = id;
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.rate = rate;
        this.country = country;
    }

    public long getId() { return this.id; };
    public String getCode() { return this.code; };
    public String getName() { return this.name; };
    public double getAmount() { return this.amount; };
    public double getRate() { return this.rate; };
    public String getCountry() { return this.country; };

}
