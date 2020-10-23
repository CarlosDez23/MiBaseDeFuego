package com.example.mibasedefuego;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    //Ui elements
    private EditText etRegisterMail;
    private EditText etRegisterUsername;
    private EditText etRegisterPhone;
    private Button btnGuardar;
    private Button btnMostrar;
    private Button btnBorrar;
    private Button btnVolver;
    private Button btnIrPerfil;
    private CircleImageView ivRegisterProfile;

    //Firebase Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Url de la foto
    private String imgUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setUp();

    }

    //Set up ui elements
    private void setUp(){
        this.setTitle("Registro");
        this.etRegisterMail = findViewById(R.id.etRegisterMail);
        this.etRegisterUsername = findViewById(R.id.etRegisterUsername);
        this.etRegisterPhone = findViewById(R.id.etRegisterPhone);
        this.btnGuardar = findViewById(R.id.btnGuardar);
        this.btnMostrar = findViewById(R.id.btnMostrar);
        this.btnBorrar = findViewById(R.id.btnBorrar);
        this.btnVolver = findViewById(R.id.btnVolver);
        this.ivRegisterProfile = findViewById(R.id.ivRegisterProfile);
        this.btnIrPerfil = findViewById(R.id.btnIrPerfil);
        setUpListeners();
        getExtrasFromIntent();
    }

    //Set up listeners for ui elements
    private void setUpListeners(){
        this.btnGuardar.setOnClickListener(onClickEvtHandler);
        this.btnMostrar.setOnClickListener(onClickEvtHandler);
        this.btnBorrar.setOnClickListener(onClickEvtHandler);
        this.btnVolver.setOnClickListener(onClickEvtHandler);
        this.btnIrPerfil.setOnClickListener(onClickEvtHandler);
    }

    private void getExtrasFromIntent(){
        //Añadimos el contenido del intent
        Intent intent = getIntent();
        this.etRegisterMail.setText(intent.getStringExtra("mail"));
        this.etRegisterMail.setEnabled(false);
    }

    private View.OnClickListener onClickEvtHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnGuardar:
                    insertItemFirebase();
                    break;
                case R.id.btnMostrar:
                    queryItemFromFirebase();
                    break;
                case R.id.btnBorrar:
                    deleteItemFromFirebase();
                    break;
                case R.id.btnVolver:
                    goBackLogin();
                    break;
                case R.id.btnIrPerfil:
                    goProfile();
                    break;
                default:
                    break;
            }
        }
    };

    //Sirve para guardar o actualizar porque pillamos la "id" del email
    private void insertItemFirebase() {
        Map<String,String> user = new HashMap<>();
        user.put("username", etRegisterUsername.getText().toString());
        user.put("phone", etRegisterPhone.getText().toString());
        db.collection("users").document(etRegisterMail.getText().toString())
                .set(user);
        showMessage("Usuario añadido");
    }

    //Consultamos una colección
    private void queryItemFromFirebase(){
        db.collection("users").document(etRegisterMail.getText().toString())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                refreshUI(documentSnapshot);
                showMessage("Usuario encontrado");
                btnIrPerfil.setVisibility(View.VISIBLE);
            }
        });
    }

    //Borramos de una colección
    private void deleteItemFromFirebase(){
        db.collection("users").document(etRegisterMail.getText().toString())
                .delete();
        showMessage("Usuario borrado");
        refreshUI();
    }

    private void refreshUI(DocumentSnapshot documentSnapshot){
        etRegisterUsername.setText(documentSnapshot.get("username").toString());
        etRegisterPhone.setText(documentSnapshot.get("phone").toString());
        if(documentSnapshot.get("profileimg") != null){
            imgUrl = documentSnapshot.get("profileimg").toString();
            System.out.println(imgUrl);
            Picasso.with(getApplicationContext()).load(imgUrl).into(ivRegisterProfile);
        }
    }

    private void refreshUI(){
        etRegisterUsername.setText("");
        etRegisterPhone.setText("");
    }
    private void showMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    private void goBackLogin(){
        finish();
    }

    private void goProfile(){
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("mail", etRegisterMail.getText().toString());
        intent.putExtra("username", etRegisterUsername.getText().toString());
        intent.putExtra("phone", etRegisterPhone.getText().toString());
        intent.putExtra("img", imgUrl);
        startActivity(intent);
    }
}