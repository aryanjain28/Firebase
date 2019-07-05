package com.example.firstfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.auth.User;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class WelcomeActivity extends AppCompatActivity implements Serializable {

    private static final String TAG = "WelcomeActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStoreDatabase;
    private boolean doubleBackToExitPressedOnce = false;
    private Date currentTime;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private UserLocation mUserLocation;
    private static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, "ca-app-pub-5400968646818022~7963076388");
        currentTime = Calendar.getInstance().getTime();
        setContentView(R.layout.activity_welcome);

        mUserLocation = new UserLocation();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(WelcomeActivity.this);
        mFireStoreDatabase = FirebaseFirestore.getInstance();

        if (mUser != null) {
            Toast.makeText(this, "User signed in with email : " + mUser.getEmail(), Toast.LENGTH_SHORT).show();
            getCurrentUserDetails();
        }
        else {
            Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    //STEP1
    //get current user details from firestore and add it to UserLocation Userdetails
    private void getCurrentUserDetails(){
        DocumentReference userDetailRef = mFireStoreDatabase.collection("Users")
                .document(mAuth.getCurrentUser().getUid());
        userDetailRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: successfully got details");
                            Log.d(TAG, "onComplete: "+task.getResult().toString());
                            Log.d(TAG, "onComplete: "+task.getResult().getData());

                            //This line is giving me error :
                            User user = task.getResult().toObject(User.class);
                            mUserLocation.setUser(user);
                            getCurrentUserLocation();
                        }
                    }
                });
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
                            Location location = task.getResult();
                            GeoPoint GeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            Log.d(TAG, "onComplete: Latitude : "+location.getLatitude() + " Longitude : "+location.getLongitude());
                            Log.d(TAG, "onComplete: Latitude : "+GeoPoint.getLatitude() + " Longitude : "+GeoPoint.getLongitude());

                            mUserLocation.setGeoPoint(GeoPoint);
                            mUserLocation.setTimeStamp(null);
                            setCurrentUserLocation();
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
                break;

            case R.id.signOut :
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
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference uidRef = database.getReference(mUser.getUid());
                        uidRef.removeValue();
                        mAuth.getCurrentUser().delete().addOnCompleteListener(WelcomeActivity.this, new OnCompleteListener<Void>() {
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
}
