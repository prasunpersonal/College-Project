package com.jisce.collegeproject.Models;

import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

@Parcel
public class Product {
    public static final int SINGLE = 1;
    public static final int GRAIN_CROPS = 2;
    public static final int LIQUID = 3;
    public static final Map<String, Double> SINGLE_UNITS = new HashMap<>();
    public static final Map<String, Double> GRAIN_UNITS = new HashMap<>();
    public static final Map<String, Double> LIQUID_UNITS = new HashMap<>();

    static {
        SINGLE_UNITS.put("Piece", 1.0);
        SINGLE_UNITS.put("Dozen", 12.0);
    }
    static {
        GRAIN_UNITS.put("Kilogram", 1.0);
        GRAIN_UNITS.put("Gram", 0.001);
    }
    static {
        LIQUID_UNITS.put("Liter", 1.0);
        LIQUID_UNITS.put("Milliliter", 0.001);
    }

    String img, name;
    int type;
    double price;

    public Product() {}

    public Product(String name, int type, double price, @Nullable String img) {
        this.img = img;
        this.name = name;
        this.type = type;
        this.price = price;
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Map<String, Double> getUnitMap() {
        return (this.getType() == Product.SINGLE) ? Product.SINGLE_UNITS : (this.getType() == Product.GRAIN_CROPS) ? Product.GRAIN_UNITS : Product.LIQUID_UNITS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return this.hashCode() == product.hashCode();
    }

    @Override
    public int hashCode() {
        return String.format(Locale.getDefault(), "%s_%d_%f", this.name, this.type, this.price).hashCode();
    }
}
