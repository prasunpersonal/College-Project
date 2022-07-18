package com.jisce.collegeproject.Models;

public class SelectionArgs {
    private double quantity;
    private String unit;

    public SelectionArgs() {}


    public SelectionArgs(double quantity, String unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
