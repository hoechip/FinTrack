package com.example.fintrack;

public class Envelope {
    private String id;
    private String name;
    private int iconRes;

    // No-argument constructor required for Firebase
    public Envelope() {}

    public Envelope(String id, String name, int iconRes) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
}
