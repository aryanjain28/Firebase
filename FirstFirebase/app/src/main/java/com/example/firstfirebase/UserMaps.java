package com.example.firstfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.firstfirebase.Constant.MAPVIEW_BUNDLE_KEY;

public class UserMaps extends Fragment implements OnMapReadyCallback {

    private MapView user_map;
    ArrayList<LatitudeLongitude> latitudeLongitudeArrayList;
    ArrayList<String> usersList;
    LatitudeLongitude l;

    public static UserMaps newInstance(){
        return new UserMaps();
    }

    public UserMaps() {
        l=new LatitudeLongitude();
    }

    public UserMaps(ArrayList<LatitudeLongitude> latitudeLongitudeArrayList, ArrayList<String> usersList) {
        this.latitudeLongitudeArrayList = latitudeLongitudeArrayList;
        this.usersList = usersList;
        latitudeLongitudeArrayList.get(0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: "+l.getLatitude());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user_maps, container, false);
        user_map = view.findViewById(R.id.user_map);

        initGoogleMap(savedInstanceState);

        return view;
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        user_map.onCreate(mapViewBundle);
        user_map.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        user_map.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        user_map.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        user_map.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        user_map.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
       // map.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Marker"));
        if (ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&

                ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onPause() {
        user_map.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        user_map.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        user_map.onLowMemory();
    }
}
