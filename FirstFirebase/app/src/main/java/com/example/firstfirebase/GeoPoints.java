package com.example.firstfirebase;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

public class GeoPoints implements Parcelable {
    GeoPoint geoPoint;

    public GeoPoints() {
    }

    public GeoPoints(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    protected GeoPoints(Parcel in) {
    }

    public static final Creator<GeoPoints> CREATOR = new Creator<GeoPoints>() {
        @Override
        public GeoPoints createFromParcel(Parcel in) {
            return new GeoPoints(in);
        }

        @Override
        public GeoPoints[] newArray(int size) {
            return new GeoPoints[size];
        }
    };

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
