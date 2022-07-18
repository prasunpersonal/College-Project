package com.jisce.collegeproject.Models;

import com.google.firebase.firestore.Exclude;

import org.parceler.Parcel;

@Parcel
public class Invoice {
    private String name, date, url, customerPhone, customerName, customerEmail, customerAddress, customerPin;
    private double total;

    public Invoice() {}

    public Invoice(String name, String date, String url, String customerPhone, String customerName, String customerEmail, String customerAddress, String customerPin, double total) {
        this.name = name;
        this.date = date;
        this.url = url;
        this.customerPhone = customerPhone;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.customerPin = customerPin;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerPin() {
        return customerPin;
    }

    public void setCustomerPin(String customerPin) {
        this.customerPin = customerPin;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Exclude
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        Invoice invoice = (Invoice) o;
        return getName().equals(invoice.getName());
    }

    @Exclude
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
