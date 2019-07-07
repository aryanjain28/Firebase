package com.example.firstfirebase;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String Name;
    private String Number;
    private String Email;
    private String ID;

    public User() {
    }

    public User(String name, String number, String email, String ID) {
        Name = name;
        Number = number;
        Email = email;
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    protected User(Parcel in) {
        Name = in.readString();
        Number = in.readString();
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String Number) {
        this.Number = Number;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public String toString() {
        return "User{" +
                "Name='" + Name + '\'' +
                ", Number='" + Number + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Name);
        dest.writeString(Number);
    }
}
