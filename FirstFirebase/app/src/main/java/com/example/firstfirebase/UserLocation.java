package com.example.firstfirebase;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation {
    private GeoPoint GeoPoint;
    private @ServerTimestamp Date TimeStamp;
    private User user;

    public UserLocation(com.google.firebase.firestore.GeoPoint geoPoint, Date timeStamp, User user) {
        GeoPoint = geoPoint;
        TimeStamp = timeStamp;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "GeoPoint=" + GeoPoint +
                ", TimeStamp=" + TimeStamp +
                ", user=" + user +
                '}';
    }
}
