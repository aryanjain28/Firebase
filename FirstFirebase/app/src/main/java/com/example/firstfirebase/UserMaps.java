package com.example.firstfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.ByteArrayOutputStream;
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

        Bitmap image = null;

        if (!usersLocations.isEmpty()) {
            googleMap = map;
            for (int i = 0; i < usersLocations.size(); i++) {
                final double x = usersLocations.get(i).getGeoPoint().getLatitude();
                final double y = usersLocations.get(i).getGeoPoint().getLongitude();

                MarkerOptions markerOptions = new MarkerOptions();
                /*image = Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(getResources(), selectImage()),
                        120,
                        120,
                        true
                );*/
                //addPaddingTopForBitmap(image, 10);
                //image.compress(Bitmap.CompressFormat.JPEG, 0, new ByteArrayOutputStream());

                markerOptions.position(new LatLng(x, y));
                //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(padBitmap(image)));
                markerOptions.title(usersLocations.get(i).getUser().getName());
                markerOptions.snippet(usersLocations.get(i).getUser().getEmail());

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

    public static Bitmap padBitmap(Bitmap bitmap)
    {
        int paddingX = 10;
        int paddingY = 10;

        Bitmap paddedBitmap = Bitmap.createBitmap(
                bitmap.getWidth() + paddingX,
                bitmap.getHeight() + paddingY,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(paddedBitmap);
        canvas.drawARGB(0xFF, 0xF0, 0xFF, 0xF0); // this represents white color
        canvas.drawBitmap(
                bitmap,
                paddingX / 2,
                paddingY / 2,
                new Paint(Paint.FILTER_BITMAP_FLAG));

        return paddedBitmap;
    }

    public int selectImage(){
        switch (new Random().nextInt(5)){
            case 1 : return R.drawable.arrow;
            case 2: return R.drawable.bird;
            case 3: return R.drawable.emoji;
            case 4: return R.drawable.employee;
            default:return R.drawable.doraemon;
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
