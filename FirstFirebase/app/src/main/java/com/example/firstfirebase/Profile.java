package com.example.firstfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;


public class Profile extends AppCompatActivity {

    private static final String TAG = "Profile";
    public static final int PICK_IMAGE_REQUEST = 1;
    public static int PIC_PRESENT_OR_NOT = 0;
    private ImageView profilePic;
    private EditText name;
    private EditText number;
    private TextView message;
    private Button uploadButton;
    private ProgressBar progressBar;
    private Uri mImageUri;
    private String x;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    private FirebaseFirestore mFirestoreDatabase;
    private FirebaseAuth mAuth;
    private UserDetails mUserDetails;
    private Map<String, Object> mUserDetailsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        message = findViewById(R.id.message);
        profilePic = findViewById(R.id.profilePic);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        uploadButton = findViewById(R.id.uploadButton);
        progressBar = findViewById(R.id.progressBar);
        mUserDetails = new UserDetails();

        mStorageRef = FirebaseStorage.getInstance().getReference("Uploads/");
        mFirestoreDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        message.setVisibility(View.VISIBLE);
        chooseImage();
        registerForContextMenu(profilePic);
        upload();
        getDetails();
    }

    //1.Used to create floating menu.
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (PIC_PRESENT_OR_NOT == 1) {
            getMenuInflater().inflate(R.menu.delete_pic, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (PIC_PRESENT_OR_NOT == 1) {

            FirebaseAuth  mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();

            switch (item.getItemId()){
                case R.id.removepic :
                    profilePic.setImageResource(R.color.common_google_signin_btn_text_dark_disabled);
                    message.setVisibility(View.VISIBLE);
                    StorageReference mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fir-setup-711db.appspot.com");
                    StorageReference profileRef = mStorageRef.child("Uploads/ProfilePicUploads/" + user.getUid().trim() + ".jpg");

                    profileRef.delete();
                    Toast.makeText(this, "Profile pic removed", Toast.LENGTH_LONG).show();
                    PIC_PRESENT_OR_NOT = 0;
                    break;

                case R.id.save : download();
                    break;

                default: return super.onContextItemSelected(item);
            }
        }
        return false;
    }


    //2. Used to select image.
    public void chooseImage() {
        profilePic.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileChooser();
                    }
                }
        );
    }

    private void fileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            mImageUri = data.getData();
            message.setVisibility(View.INVISIBLE);
            PIC_PRESENT_OR_NOT = 1;
        }
        profilePic.setImageURI(mImageUri);

    }

    //3. Upload button.
    public void upload(){
            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();


                            //to upload name and number
                            if (!name.getText().toString().trim().equals("") && !number.getText().toString().trim().equals("")) {

                                progressBar.setVisibility(View.VISIBLE);

                                mUserDetailsMap = new HashMap<>();
                                mUserDetailsMap.put("Name", name.getText().toString().trim());
                                mUserDetailsMap.put("Number", number.getText().toString().trim());

                                //This is FireStore
                                setCurrentUserDetails();
                                progressBar.setVisibility(View.INVISIBLE);

                            }

                            //to upload image
                            if (PIC_PRESENT_OR_NOT == 1) {

                                progressBar.setVisibility(View.VISIBLE);
                                Uri profile = mImageUri;

                                x = mAuth.getCurrentUser().getUid();

                                StorageReference profileRef = mStorageRef.child("ProfilePicUploads/" + x + "." + getExtension(profile));
                                profileRef.putFile(profile)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(Profile.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnCanceledListener(new OnCanceledListener() {
                                            @Override
                                            public void onCanceled() {
                                                Toast.makeText(Profile.this, "Failure", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
            );
    }

    //4. To display profilepic, name and number on start up
    public void getDetails() {

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        //to download profile pic from storage
        //This is from Firebase Storage
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fir-setup-711db.appspot.com");
        StorageReference profileRef = mStorageRef.child("Uploads/ProfilePicUploads/" + user.getUid().trim() + ".jpg");

        progressBar.setVisibility(View.VISIBLE);
        profileRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Glide
                                .with(Profile.this)
                                .asBitmap()
                                .load(uri)
                                .into(profilePic);

                        progressBar.setVisibility(View.INVISIBLE);
                        message.setVisibility(View.INVISIBLE);
                        PIC_PRESENT_OR_NOT = 1;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        if (e.getMessage().equals("Object does not exist at location."))
                            Toast.makeText(Profile.this, "No profile pic.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(Profile.this, "Failed to download.", Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

        //to get name and number from database
        //this is from FireStore
        progressBar.setVisibility(View.VISIBLE);
        mFirestoreDatabase.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().exists()){
                                name.setText(task.getResult().getData().get("Name").toString());
                                number.setText(task.getResult().getData().get("Number").toString());
                                Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData().get("Number"));
                                Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData().get("Name"));
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                            else {
                                name.setText("");
                                name.setText("");
                                Toast.makeText(Profile.this, "No details", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, "Downloading failure", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private String getExtension(Uri mImageUri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(mImageUri));
    }

    private void download(){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fir-setup-711db.appspot.com");
        StorageReference profileRef = storageReference.child("/Uploads/ProfilePicUploads/"+user.getUid()+".jpg");

        profileRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(uri);

                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
                        request.setDestinationInExternalFilesDir(Profile.this, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), "ProfilePic.jpg");

                        downloadManager.enqueue(request);
                        Toast.makeText(Profile.this, "Image saved.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, "Failed to save image.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setCurrentUserDetails(){
            mFirestoreDatabase.collection("Users")
                    .document(mAuth.getCurrentUser().getUid())
                    .set(mUserDetailsMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Profile.this, "Details uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Profile.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
    }
}
