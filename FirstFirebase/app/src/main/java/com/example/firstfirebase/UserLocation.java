package com.example.firstfirebase;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.auth.User;

import java.io.Serializable;
import java.util.Date;

public class UserLocation {
    private GeoPoint GeoPoint;
    private @ServerTimestamp Date TimeStamp;
    private User user;

    public UserLocation(GeoPoint GeoPoint, Date TimeStamp, User user) {
        this.GeoPoint = GeoPoint;
        this.TimeStamp = TimeStamp;
        this.user = user;
    }

    public UserLocation() {
    }

    public com.google.firebase.firestore.GeoPoint getGeoPoint() {
        return GeoPoint;
    }

    public void setGeoPoint(com.google.firebase.firestore.GeoPoint geoPoint) {
        GeoPoint = geoPoint;
    }

    public Date getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        TimeStamp = timeStamp;
    }

    public com.google.firebase.firestore.auth.User getUser() {
        return user;
    }

    public void setUser(com.google.firebase.firestore.auth.User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "GeoPoint=" + GeoPoint +
                ", TimeStamp=" + TimeStamp +
                ", User=" + user +
                '}';
    }
}
