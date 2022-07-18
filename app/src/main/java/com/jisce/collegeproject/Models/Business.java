package com.jisce.collegeproject.Models;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

import javax.annotation.Nullable;

public class Business {
    String id, name, email, phone, ownerName, pinCode, building, area, city, country, state, shopImg;

    public Business() {}

    public Business(String id, String name, String ownerName, String email, String phone, String pinCode, @Nullable String building, String area, String city, String state, String country) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
        this.email = email;
        this.phone = phone;
        this.pinCode = pinCode;
        this.building = building;
        this.area = area;
        this.city = city;
        this.country = country;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getShopImg() {
        return shopImg;
    }

    public void setShopImg(String shopImg) {
        this.shopImg = shopImg;
    }

    @Exclude
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Business)) return false;
        Business business = (Business) o;
        return getId().equals(business.getId());
    }

    @Exclude
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
