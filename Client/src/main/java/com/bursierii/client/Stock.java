package com.bursierii.client;

public class Stock {
    private String Ticker;
    private String Company;
    private int Amount;
    private int Price;
    private String Date;

    public Stock(String ticker, String company, int amount, int price, String date) {
        Ticker = ticker;
        Company = company;
        Amount = amount;
        Price = price;
        Date = date;
    }

    public String getTicker() {
        return Ticker;
    }

    public String getCompany() {
        return Company;
    }

    public int getAmount() {
        return Amount;
    }

    public int getPrice() {
        return Price;
    }

    public String getDate() {
        return Date;
    }
}
