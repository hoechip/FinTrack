package com.example.fintrack;

public class Envelope {
    private String id;
    private String name;
    private double spentAmount;
    private double totalAmount; // For progress calculation
    private String iconName; // e.g., "ic_food", "ic_transport"
    private String colorHex;
    private int iconRes;

    // No-argument constructor required for Firebase
    public Envelope() {}

    public Envelope(String id, String name, double spentAmount, double totalAmount, String iconName, String colorHex, int iconRes) {
        this.id = id;
        this.name = name;
        this.spentAmount = spentAmount;
        this.totalAmount = totalAmount;
        this.iconName = iconName;
        this.colorHex = colorHex;
        this.iconRes = iconRes;
    }

    public Envelope(String name, double spentAmount, double totalAmount, String iconName, String colorHex) {
        this(null, name, spentAmount, totalAmount, iconName, colorHex, 0);
    }

    public Envelope(String id, String name, int iconRes) {
        this(id, name, 0, 0, null, null, iconRes);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public double getSpentAmount() { return spentAmount; }
    public double getTotalAmount() { return totalAmount; }
    public String getIconName() { return iconName; }
    public String getColorHex() { return colorHex; }
    public int getPercent() {
        if (totalAmount <= 0) return 0;
        return (int) ((spentAmount / totalAmount) * 100);
    }
    public void setName(String name) { this.name = name; }
    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
}
