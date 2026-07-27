package com.example.fintrack;

public class Envelope {
    private String id;
    private String name;
    private double spentAmount;
    private double totalAmount; // For progress calculation
    private String iconName; // e.g., "ic_food", "ic_transport"
    private String colorHex;

    public Envelope() {}

    public Envelope(String name, double spentAmount, double totalAmount, String iconName, String colorHex) {
        this.name = name;
        this.spentAmount = spentAmount;
        this.totalAmount = totalAmount;
        this.iconName = iconName;
        this.colorHex = colorHex;
    }

    public String getName() { return name; }
    public double getSpentAmount() { return spentAmount; }
    public double getTotalAmount() { return totalAmount; }
    public String getIconName() { return iconName; }
    public String getColorHex() { return colorHex; }
    public int getPercent() {
        if (totalAmount <= 0) return 0;
        return (int) ((spentAmount / totalAmount) * 100);
    }
}
