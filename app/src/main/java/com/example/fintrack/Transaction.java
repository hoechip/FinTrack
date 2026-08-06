package com.example.fintrack;

public class Transaction {
    private String title;
    private String date;
    private String amount;
    private int iconRes = -1;
    private String iconEmoji = null;

    public Transaction(String title, String date, String amount, int iconRes) {
        this.title = title;
        this.date = date;
        this.amount = amount;
        this.iconRes = iconRes;
    }

    public Transaction(String title, String date, String amount, String iconEmoji) {
        this.title = title;
        this.date = date;
        this.amount = amount;
        this.iconEmoji = iconEmoji;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getAmount() { return amount; }
    public int getIconRes() { return iconRes; }
    public String getIconEmoji() { return iconEmoji; }
}
