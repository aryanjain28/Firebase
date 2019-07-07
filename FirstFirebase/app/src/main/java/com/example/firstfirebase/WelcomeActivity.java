package com.example.firstfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class WelcomeActivity extends AppCompatActivity implements Serializable {

    private static int USER_IS_ONLINE = 0;
    private static final String TAG = "WelcomeActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStoreDatabase;
    private boolean doubleBackToExitPressedOnce = false;
    private Date currentTime;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private UserLocation mUserLocation;
    private ArrayList<UserLocation> userLocationArrayList;
    private ArrayList<String> userArrayList;
    private ArrayList<String> onlineDisplayArrayList;
    private ArrayList<GeoPoints> usersGeoPoints;
    private GeoPoints geoPoints;
    private ListenerRegistration  mUserListEventListener;
    private User user;
    private ProgressBar progressBar2;
    private TextView onlineMessage;
    private ListView usersOnline;
    private ProgressBar listprogressBar;
    private TextView gettingList;
    private TextView gettingLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        MobileAds.initialize(this, "ca-app-pub-5400968646818022~7963076388");
        currentTime = Calendar.getInstance().getTime();

        progressBar2 = findViewById(R.id.progressBar2);
        onlineMessage = findViewById(R.id.onlineMessage);
        listprogressBar = findViewById(R.id.listprogressBar);
        gettingList = findViewById(R.id.gettingList);
        gettingLocations = findViewById(R.id.gettingLocations);
        userArrayList = new ArrayList<>();
        usersGeoPoints = new ArrayList<>();
        mUserLocation = new UserLocation();
        mAuth = FirebaseAuth.getInstance();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(WelcomeActivity.this);
        mFireStoreDatabase = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Toast.makeText(this, "User signed in with email : " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            getCurrentUserDetails();
        }
        else {
            Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return;
        }

        displayListOfUsersOnline();
        if (USER_IS_ONLINE == 0)
            setUserIsOnline();
        Log.d(TAG, "Aryan User online");

        inflateFragment();
    }

    //FIXED
    @Override
    protected void onRestart() {
        super.onRestart();
        displayListOfUsersOnline();
        if (USER_IS_ONLINE == 0) {
            setUserIsOnline();
            Log.d(TAG, "Aryan User online");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (USER_IS_ONLINE == 1) {
            setUserIsOffline();
            Log.d(TAG, "Aryan User offline");
        }
    }

    //FIXED
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (USER_IS_ONLINE == 1) {
            setUserIsOffline();
            Log.d(TAG, "Aryan User offline");
        }
    }

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
                                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                                    String name = task.getResult().getDocuments().get(i).get("name").toString();
                                    String email = task.getResult().getDocuments().get(i).get("email").toString();
                                    if (name == null) name = "Name not provided.";
                                    if (email == null) name = "Email not provided.";
                                    onlineDisplayArrayList.add(name+"  "+"("+email+")");
                                    Log.d(TAG, "Aryans : success adding to list");
                                    listprogressBar.setVisibility(View.INVISIBLE);
                                    gettingList.setVisibility(View.INVISIBLE);

                                    getAllUsersCoordinates(task.getResult().getDocuments().get(i).getId().trim());
                                }
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

    private void getAllUsersCoordinates(String ID){
        gettingLocations.setVisibility(View.VISIBLE);
        final DocumentReference documentReference = mFireStoreDatabase.collection("UserLocations")
                .document(ID);

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            if (task != null){
                                geoPoints = new GeoPoints();
                                geoPoints.setGeoPoint(task.getResult().getGeoPoint("geoPoint"));
                                usersGeoPoints.add(geoPoints);
                                Log.d(TAG, "Aryan LAT : "+geoPoints.getGeoPoint().getLatitude());
                                Log.d(TAG, "Aryan LONG : "+geoPoints.getGeoPoint().getLongitude());
                                gettingLocations.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        gettingLocations.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void inflateFragment(){
        UserMaps userMapsFragment = UserMaps.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("OnlineUsersGeoPoints", usersGeoPoints);
        userMapsFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.commit();

    }

    private void setUserIsOffline() {
        final DocumentReference documentReference = mFireStoreDatabase
                .collection("Online")
                .document(mAuth.getCurrentUser().getUid());

        progressBar2.setVisibility(View.VISIBLE);
        onlineMessage.setVisibility(View.VISIBLE);
        documentReference.delete();
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
                        user = getCurrentUserDetails();
                        if (user != null) {
                            mUserLocation.setUser(user);
                            documentReference.set(user);
                            progressBar2.setVisibility(View.INVISIBLE);
                            onlineMessage.setVisibility(View.INVISIBLE);
                            Toast.makeText(WelcomeActivity.this, "Online", Toast.LENGTH_SHORT).show();
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
    private User getCurrentUserDetails(){
        DocumentReference userDetailRef = mFireStoreDatabase.collection("Users")
                .document(mAuth.getCurrentUser().getUid());

        userDetailRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: successfully got details");
                            user = task.getResult().toObject(User.class);
                            mUserLocation.setUser(task.getResult().toObject(User.class));
                            getCurrentUserLocation();
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
        MenuItem item = menu.getItem(2);
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
                Intent i = new Intent(WelcomeActivity.this, Profile.class);
                startActivity(i);
                USER_IS_ONLINE = 0;
                break;

            case R.id.signOut :
                setUserIsOffline();
                Toast.makeText(WelcomeActivity.this, u.getEmail()+" signed out",Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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
                        mAuth.getCurrentUser().delete();
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
}
