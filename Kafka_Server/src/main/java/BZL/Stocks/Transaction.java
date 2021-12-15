package BZL.Stocks;

public class Transaction {
    private String user;
    private String user_id;
    private String ticker;
    private String action; // Buy or sell
    private double price;
    private int amount;

    public Transaction(String user, String user_id, String ticker,double price,int amount,String action){
        this.user = user;
        this.user_id = user_id;
        this.ticker = ticker;
        this.price = price;
        this.amount = amount;
        this.action = action;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
