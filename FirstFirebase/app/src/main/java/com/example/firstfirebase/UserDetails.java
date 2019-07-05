package com.example.firstfirebase;

public class UserDetails {
    private String name;
    private String number;

    public UserDetails(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public UserDetails() {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
