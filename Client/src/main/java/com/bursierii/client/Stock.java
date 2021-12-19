package com.bursierii.client;

public class Stock {
    private String Ticker;
    private String Company;
    private int Amount;
    private double Price;
    private String Date;
    private String Action;

    public Stock(String ticker, String company, int amount, double price, String date,String action) {
        Ticker = ticker;
        Company = company;
        Amount = amount;
        Price = price;
        Date = date;
        Action = action;
    }


    public String getTicker() {
        return Ticker;
    }

    public String getAction() {return Action;}

    public String getCompany() {
        return Company;
    }

    public int getAmount() {
        return Amount;
    }

    public double getPrice() {
        return Price;
    }

    public String getDate() {
        return Date;
    }
}
