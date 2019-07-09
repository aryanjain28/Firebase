package com.example.firstfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class WelcomeActivity extends AppCompatActivity implements Serializable {

    private static int USER_IS_ONLINE;
    private static final String TAG = "WelcomeActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStoreDatabase;
    private boolean doubleBackToExitPressedOnce = false;
    private Date currentTime;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private UserLocation mUserLocation;
    private ArrayList<String> userArrayList;
    private ArrayList<String> onlineDisplayArrayList;
    private ArrayList<UserLocation> usersLocations;
    private User user;
    private ProgressBar progressBar2;
    private TextView onlineMessage;
    private ListView usersOnline;
    private ProgressBar listprogressBar;
    private TextView gettingList;
    private TextView gettingLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        USER_IS_ONLINE = 0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getSupportActionBar().setTitle(R.string.app_name);

        MobileAds.initialize(this, "ca-app-pub-5400968646818022~7963076388");
        currentTime = Calendar.getInstance().getTime();

        progressBar2 = findViewById(R.id.progressBar2);
        onlineMessage = findViewById(R.id.onlineMessage);
        listprogressBar = findViewById(R.id.listprogressBar);
        gettingList = findViewById(R.id.gettingList);
        gettingLocations = findViewById(R.id.gettingLocations);
        userArrayList = new ArrayList<>();
        usersLocations = new ArrayList<>();
        mUserLocation = new UserLocation();
        mAuth = FirebaseAuth.getInstance();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(WelcomeActivity.this);
        mFireStoreDatabase = FirebaseFirestore.getInstance();

        Log.d(TAG, "Aryan onCreate: CALLED");
        if (checkLocationPermissionGranted()) {
            if (checkLocationAndNetwork()) {
                if (mAuth.getCurrentUser() != null) {
                    getCurrentUserDetails(0);
                    afterTwoSecond();
                } else {
                    Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    return;
                }
            }
        }
    }

    private void afterTwoSecond(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setUserIsOnline();
            }
        }, 2000);
    }

    //FIXED
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Aryan onRestart: Called");
        if (checkLocationPermissionGranted()) {
            if (checkLocationAndNetwork()) {
                getCurrentUserDetails(0);
                if (USER_IS_ONLINE == 0) {
                    afterTwoSecond();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Aryan onPause: Called");
        if (USER_IS_ONLINE == 1) {
            setUserIsOffline();
            Log.d(TAG, "User offline");
        }
    }

    //FIXED
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Aryan onDestroy: Called");
        if (USER_IS_ONLINE == 1) {
            setUserIsOffline();
            Log.d(TAG, "User offline");
        }
    }

    //Step 1 : loop for getting a online list
    private void displayListOfUsersOnline(){
        listprogressBar.setVisibility(View.VISIBLE);
        gettingList.setVisibility(View.VISIBLE);
        CollectionReference collectionReference = mFireStoreDatabase.collection("Online");
        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task != null) {
                                onlineDisplayArrayList = new ArrayList<>();
                                onlineDisplayArrayList.clear();
                                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                                    String name = task.getResult().getDocuments().get(i).get("name").toString();
                                    String email = task.getResult().getDocuments().get(i).get("email").toString();
                                    onlineDisplayArrayList.add(name+"  "+"("+email+")");
                                    Log.d(TAG, " success adding to list");
                                    listprogressBar.setVisibility(View.INVISIBLE);
                                    gettingList.setVisibility(View.INVISIBLE);

                                    getUserClass(task.getResult().getDocuments().get(i).getId().trim());
                                }
                                findViewById(R.id.listEmptyMessage).setVisibility(View.INVISIBLE);
                                usersOnline = findViewById(R.id.usersOnline);
                                ListAdapter adapter = new ArrayAdapter<>(WelcomeActivity.this, android.R.layout.simple_list_item_1, onlineDisplayArrayList);
                                usersOnline.setAdapter(adapter);
                            }
                            Log.d(TAG, "onComplete: Success");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listprogressBar.setVisibility(View.INVISIBLE);
                        gettingList.setVisibility(View.INVISIBLE);
                    }
                });
        //return onlineDisplayArrayList;
    }

    //Step 2 : getting user class from USERS
    private void getUserClass(final String ID){
        DocumentReference userDetailRef = mFireStoreDatabase.collection("Users")
                .document(ID);
        userDetailRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            user = task.getResult().toObject(User.class);
                            getAllUsersCoordinates(user, ID);
                        }
                    }
                });
    }

    //Step 3 : setting UsersLocation ArrayList
    private void getAllUsersCoordinates(final User user, String ID){
        gettingLocations.setVisibility(View.VISIBLE);
        final DocumentReference documentReference = mFireStoreDatabase.collection("UserLocations")
                .document(ID);

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task != null) {

                                UserLocation userLocation = new UserLocation();
                                userLocation.setUser(user);
                                userLocation.setGeoPoint(task.getResult().getGeoPoint("geoPoint"));
                                usersLocations.add(userLocation);

                                Log.d(TAG, " LAT : " + userLocation.getGeoPoint().getLatitude());
                                Log.d(TAG, " LONG : " + userLocation.getGeoPoint().getLongitude());
                                gettingLocations.setVisibility(View.INVISIBLE);

                                if (!usersLocations.isEmpty()) {
                                    Log.d(TAG, "inflateFragment1: \n" +
                                            usersLocations.get(usersLocations.size() - 1).getGeoPoint().getLatitude() + "\n" +
                                            usersLocations.get(usersLocations.size() - 1).getGeoPoint().getLongitude());
                                } else
                                    Log.d(TAG, "inflateFragment1: EMPTY");

                            }

                        }
                        inflateFragment();
                    }
                });
    }

    //Steps 4 : inflation
    private void inflateFragment(){
        Log.d(TAG, "inflateFragment2: REACHED");
        if (!usersLocations.isEmpty()) {
            Log.d(TAG, "inflateFragment2: \n" +
                    usersLocations.get(usersLocations.size()-1).getGeoPoint().getLatitude() + "\n" +
                    usersLocations.get(usersLocations.size()-1).getGeoPoint().getLongitude());
        }
        else
            Log.d(TAG, "inflateFragment2: EMPTY");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        UserMaps userMaps = UserMaps.newInstance(usersLocations);
        transaction.replace(R.id.fragment, userMaps);
        transaction.commit();
    }

    private void setUserIsOffline() {
        final DocumentReference documentReference = mFireStoreDatabase
                .collection("Online")
                .document(mAuth.getCurrentUser().getUid());

        progressBar2.setVisibility(View.VISIBLE);
        onlineMessage.setVisibility(View.VISIBLE);
        documentReference.delete();
        Log.d(TAG, "\nsetUserIsOffline: I am OFFFLLLIIINNNNEEEE......\n\n\n");
        Toast.makeText(WelcomeActivity.this, "Offline", Toast.LENGTH_SHORT).show();
        progressBar2.setVisibility(View.INVISIBLE);
        onlineMessage.setVisibility(View.INVISIBLE);
        USER_IS_ONLINE = 0;
    }

    //getting all the userLocations and add it to a arraylist
    private void setUserIsOnline(){
        progressBar2.setVisibility(View.VISIBLE);
        onlineMessage.setVisibility(View.VISIBLE);
        final DocumentReference documentReference = mFireStoreDatabase.collection("Online")
                .document(mAuth.getCurrentUser().getUid());
        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        user = getCurrentUserDetails(1);
                        if (user != null) {
                            mUserLocation.setUser(user);
                            documentReference.set(user);
                            progressBar2.setVisibility(View.INVISIBLE);
                            onlineMessage.setVisibility(View.INVISIBLE);
                            Toast.makeText(WelcomeActivity.this, "User signed in with email : " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                            displayListOfUsersOnline();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar2.setVisibility(View.INVISIBLE);
                        onlineMessage.setVisibility(View.INVISIBLE);
                        Toast.makeText(WelcomeActivity.this, "Failed to go online", Toast.LENGTH_SHORT).show();
                    }
                });
        USER_IS_ONLINE = 1;
    }

    //STEP1
    //get current user details from firestore and add it to UserLocation Userdetails
    private User getCurrentUserDetails(final int x){
        DocumentReference userDetailRef = mFireStoreDatabase.collection("Users")
                .document(mAuth.getCurrentUser().getUid());

        userDetailRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: successfully got details");
                            user = task.getResult().toObject(User.class);
                            if (x == 0) {
                                mUserLocation.setUser(task.getResult().toObject(User.class));
                                getCurrentUserLocation();
                            }
                        }
                    }
                });
        return user;
    }

    //STEP2
    //get current user location
    private void getCurrentUserLocation() {
        Log.d(TAG, "getUsersLocation: Called");
        if (ActivityCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()){
                            if (task != null) {
                                Location location = task.getResult();
                                GeoPoint GeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                                Log.d(TAG, "onComplete: Latitude : " + location.getLatitude() + " Longitude : " + location.getLongitude());
                                Log.d(TAG, "onComplete: Latitude : " + GeoPoint.getLatitude() + " Longitude : " + GeoPoint.getLongitude());

                                mUserLocation.setGeoPoint(GeoPoint);
                                mUserLocation.setTimeStamp(new Date());
                                setCurrentUserLocation();
                            }
                        }
                    }
                });
    }

    //STEP3
    //set current user location in firestore
    private void setCurrentUserLocation(){
        if (mUserLocation != null){
            DocumentReference locationRef = mFireStoreDatabase.collection("UserLocations")
                    .document(mAuth.getCurrentUser().getUid());

            locationRef.set(mUserLocation)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d(TAG, "onComplete: Inserted use location into database"+
                                        "\n latitude : "+mUserLocation.getGeoPoint().getLatitude()+
                                        "\n longitude : "+mUserLocation.getGeoPoint().getLongitude());
                            }
                        }
                    });
        }
    }

 @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signout, menu);
        MenuItem item = menu.getItem(3);
        SpannableString s = new SpannableString("Delete Account");
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        item.setTitle(s);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FirebaseUser u = mAuth.getCurrentUser();
        switch(item.getItemId()){

            case R.id.profile:
                USER_IS_ONLINE = 0;
                Intent i = new Intent(WelcomeActivity.this, Profile.class);
                startActivity(i);
                break;

            case R.id.refresh:
                displayListOfUsersOnline();
                break;

            case R.id.signOut :
                setUserIsOffline();
                findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
                Toast.makeText(WelcomeActivity.this, u.getEmail()+" signed out",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAuth.signOut();
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);
                    }
                }, 1000);
                break;

            case R.id.delete :
                AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
                alertBox.setTitle("Are you sure?");
                alertBox.setMessage("Deleting this account will result in completely removing your account "+
                        " from the system and you won't be able to access the app.");
                SpannableString s1 = new SpannableString("Delete");
                s1.setSpan(new ForegroundColorSpan(Color.RED), 0, s1.length(), 0);
                alertBox.setPositiveButton(s1, new DialogInterface.OnClickListener() {
                    @Override
                    public  void onClick(DialogInterface dialog, int which) {
                        DocumentReference documentReference1 = mFireStoreDatabase.collection("UserLocations")
                                .document(mAuth.getCurrentUser().getUid());
                        setUserIsOffline();
                        documentReference1.delete()
                                .addOnCompleteListener(WelcomeActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isComplete()) {
                                        mAuth.getCurrentUser().delete();
                                        Toast.makeText(WelcomeActivity.this, " ACCOUNT DELETED.", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                    else {
                                        Toast.makeText(WelcomeActivity.this, "Account not deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        DocumentReference documentReference2 = mFireStoreDatabase.collection("Users")
                                .document(mAuth.getCurrentUser().getUid());
                        documentReference2.delete()
                                .addOnCompleteListener(WelcomeActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isComplete()) {
                                            Toast.makeText(WelcomeActivity.this, " ACCOUNT DELETED.", Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(i);
                                        }
                                        else {
                                            Toast.makeText(WelcomeActivity.this, "Account not deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        }
                    });
                    SpannableString s2 = new SpannableString("Dismiss");
                    s2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s2.length(), 0);
                    alertBox.setNegativeButton(s2, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertBox.create();
                alertDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkLocationPermissionGranted() {
        boolean x = true;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            handlePermissions(0);
            Log.d(TAG, "checkLocationPermissionGranted: Called");
            x = false;
        }
        return x;
    }

    private boolean checkLocationAndNetwork(){

        boolean x = true;
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            handlePermissions(1);
            x = false;
        }
    return x;
    }

    private void handlePermissions(int x){

        SpannableString spannableString = new SpannableString("OK");
        spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString.length(), 0);
        switch (x){
            case 0:
                new AlertDialog.Builder(WelcomeActivity.this)
                        .setCancelable(false)
                        .setTitle("Location permission not provide")
                        .setMessage("Location permission not provided, please tap OK below and grant location permission.")
                        .setPositiveButton(spannableString, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setData(Uri.fromParts("package",getPackageName(),null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                               // intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                              //  intent.addFlags(intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                WelcomeActivity.this.startActivity(intent);
                            }
                        })
                        .show();
                break;

            case 1:
                new AlertDialog.Builder(WelcomeActivity.this)
                        .setCancelable(false)
                        .setTitle("App needs location & network services.")
                        .setMessage("Please turn on your location & internet and press OK below.")
                        .setPositiveButton(spannableString, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
                break;
        }

    }

}
