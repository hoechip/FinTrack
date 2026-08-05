package com.example.themgd_tracuu;

public class TransactionHistory {
    public long id;
    public String title;
    public double amount;
    public String date;
    public String categoryName;
    public String categoryIcon;

    public TransactionHistory(long id, String title, double amount, String date, String categoryName, String categoryIcon) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
    }
}
