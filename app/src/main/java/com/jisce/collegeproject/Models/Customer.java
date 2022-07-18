package com.jisce.collegeproject.Models;

import com.google.firebase.firestore.Exclude;

import java.util.Locale;
import java.util.Objects;

public class Customer {
    private String customerPhone, customerName, customerEmail, customerAddress, customerPin;

    public Customer() {}

    public Customer(String customerPhone, String customerName, String customerEmail, String customerAddress, String customerPin) {
        this.customerPhone = customerPhone;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.customerPin = customerPin;
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

    @Exclude
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return getCustomerPhone().equals(customer.getCustomerPhone()) && getCustomerName().equals(customer.getCustomerName()) && getCustomerEmail().equals(customer.getCustomerEmail()) && getCustomerAddress().equals(customer.getCustomerAddress()) && getCustomerPin().equals(customer.getCustomerPin());
    }

    @Exclude
    @Override
    public int hashCode() {
        String str = String.format(Locale.getDefault(), "%s_%s_%s_%s_%s", this.customerPhone, this.customerName, this.customerEmail, this.customerAddress, this.customerPin);
        return str.hashCode();
    }
}
