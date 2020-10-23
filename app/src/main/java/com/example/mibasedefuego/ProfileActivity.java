package com.example.mibasedefuego;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    //UI Elements
    private CircleImageView ivProfile;
    private TextView tvProfileEmail;
    private TextView tvProfileUsername;
    private TextView tvProfilePhone;
    private Button btnProfileGuardar;

    //Request code for take a picture from gallery
    public static final int TAKE_FROM_GALLERY = 1;

    //Uri for the image
    private Uri imageUri;

    //Firebase
    //Storage
    private StorageReference mStorageRef;
    //Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Tarea de subida de archivo
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setUp();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    /**
     * Bind views
     */
    private void setUp(){
        this.setTitle("Mi perfil");
        this.ivProfile = findViewById(R.id.profile);
        this.tvProfileEmail = findViewById(R.id.tvProfileMail);
        this.tvProfilePhone = findViewById(R.id.tvProfilePhone);
        this.tvProfileUsername = findViewById(R.id.tvProfileUser);
        this.btnProfileGuardar = findViewById(R.id.btnProfileGuardar);
        this.btnProfileGuardar.setEnabled(false);
        getIntentExtras();
        setUpListeners();
    }

    private void setUpListeners(){
        this.ivProfile.setOnClickListener(onClickEventHandler);
        this.btnProfileGuardar.setOnClickListener(onClickEventHandler);
    }

    /**
     * Get intent extra that we send from the previous activity
     */
    private void getIntentExtras(){
        Intent intent = getIntent();
        this.tvProfileEmail.setText(intent.getStringExtra("mail"));
        this.tvProfileUsername.setText(intent.getStringExtra("username"));
        this.tvProfilePhone.setText(intent.getStringExtra("phone"));
        if(intent.getStringExtra("img") != null){
            String url = intent.getStringExtra("img");
            Picasso.with(getApplicationContext()).load(url).into(ivProfile);
        }
    }

    private View.OnClickListener onClickEventHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnProfileGuardar:
                    uploadFileToFirebase();
                    break;
                case R.id.profile:
                    openFileChooser();
                    break;
                default:
                    break;
            }
        }
    };

    private void openFileChooser(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, TAKE_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_FROM_GALLERY && resultCode == RESULT_OK) {
            if(data != null){
                imageUri = data.getData();
                try{
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            imageUri);
                    this.ivProfile.setImageBitmap(bitmap);
                    this.btnProfileGuardar.setEnabled(true);
                }catch (IOException e){
                    showMessage("Error escogiendo foto de la galería");
                }
            }
        }
    }

    private void uploadFileToFirebase(){
        StorageReference riversRef = mStorageRef.child("images/"+tvProfileUsername+"si.jpg");
        uploadTask = riversRef.putFile(imageUri);
        Task<Uri>  urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    String downloadUrl = task.getResult().toString();
                    addImgUrlToUserDocument(downloadUrl);
                }
            }
        });
    }

    //Update a existing document in a collection of Firestore
    private void addImgUrlToUserDocument(String imgUrl){
        Map<String,Object> user = new HashMap<>();
        user.put("profileimg",imgUrl);
        db.collection("users").document(tvProfileEmail.getText().toString())
                .update(user);
        showMessage("Foto de perfil añadida al usuario");
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
            .show();
    }
}