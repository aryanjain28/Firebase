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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Random;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.firstfirebase.Constant.MAPVIEW_BUNDLE_KEY;

public class UserMaps extends Fragment implements OnMapReadyCallback {

    int x = 0;
    private MapView user_map;
    private GoogleMap googleMap;
    ArrayList<UserLocation> usersLocations  = new ArrayList<>();

    public static UserMaps newInstance(ArrayList<UserLocation> usersLocations){
        UserMaps userMaps = new UserMaps();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("onlineUsers", usersLocations);
        userMaps.setArguments(bundle);
        return userMaps;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null){
            usersLocations.clear();
            usersLocations = getArguments().getParcelableArrayList("onlineUsers");
            Log.d(TAG, "inflateFragment3: REACHED non-empty");
        }
        else {
            Log.d(TAG, "inflateFragment3: REACHED empty");
        }
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
    public void onMapReady(final GoogleMap map) {

        if (!usersLocations.isEmpty()) {
            googleMap = map;
            for (int i = 0; i < usersLocations.size(); i++) {
                final double x = usersLocations.get(i).getGeoPoint().getLatitude();
                final double y = usersLocations.get(i).getGeoPoint().getLongitude();

                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(new LatLng(x, y));
                markerOptions.title(usersLocations.get(i).getUser().getName());
                markerOptions.snippet(usersLocations.get(i).getUser().getEmail());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(selectRandomColor()));

                map.addMarker(markerOptions);
            }

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
    }

    private float selectRandomColor(){
        switch (new Random().nextInt(10)){
            case 0: return BitmapDescriptorFactory.HUE_AZURE;
            case 1: return BitmapDescriptorFactory.HUE_BLUE;
            case 2: return BitmapDescriptorFactory.HUE_CYAN;
            case 3: return BitmapDescriptorFactory.HUE_GREEN;
            case 4: return BitmapDescriptorFactory.HUE_MAGENTA;
            case 5: return BitmapDescriptorFactory.HUE_ROSE;
            case 6: return BitmapDescriptorFactory.HUE_ORANGE;
            case 7: return BitmapDescriptorFactory.HUE_VIOLET;
            case 8: return BitmapDescriptorFactory.HUE_RED;
            default: return BitmapDescriptorFactory.HUE_YELLOW;

        }
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
