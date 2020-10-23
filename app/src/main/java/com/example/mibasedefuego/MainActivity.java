package com.example.mibasedefuego;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth myAuth;

    //UI elements
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvEmail;
    private TextView tvProvider;
    private Button btnRegistro;
    private Button btnLogin;
    private Button btnSignOut;
    //Elements for Google signIn
    private SignInButton signInButton;



    //Para el start activity por result
    private final static int GOOGLE_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myAuth = FirebaseAuth.getInstance();
        createRequest();
        setUp();
    }

    @Override
    public void onStart() {
        super.onStart();
        //Chequeamos si existe alguien logueado con una cuenta de google en el dispositivo
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!= null){
            updateUi(account);
        }
    }

    private void createRequest(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    private void googleSignIn(){
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(googleSignInIntent, GOOGLE_SIGN_IN);
    }


    //Bind ui elements
    private void setUp(){
        this.setTitle("Autenticación");
        this.etEmail = findViewById(R.id.etEmail);
        this.etPassword = findViewById(R.id.etPassword);
        this.tvEmail = findViewById(R.id.tvEmail);
        this.tvProvider = findViewById(R.id.tvProvider);
        this.btnRegistro = findViewById(R.id.btnRegistro);
        this.btnLogin = findViewById(R.id.btnLogin);
        this.btnSignOut = findViewById(R.id.btnSignOut);
        this.signInButton = findViewById(R.id.btnGoogle);
        this.signInButton.setSize(SignInButton.SIZE_WIDE);
        this.btnRegistro.setOnClickListener(onClickListenerEvt);
        this.btnLogin.setOnClickListener(onClickListenerEvt);
        this.btnSignOut.setOnClickListener(onClickListenerEvt);
        this.signInButton.setOnClickListener(onClickListenerEvt);
        this.btnSignOut.setEnabled(false);
    }

    private View.OnClickListener onClickListenerEvt = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnRegistro:
                    if(checkFields()){
                        createUserinFirebase(etEmail.getText().toString(),
                                etPassword.getText().toString());
                    }else{
                        showAlert("Por favor rellena los campos de email y contraseña");
                    }
                    break;
                case R.id.btnLogin:
                    if(checkFields()){
                        loginUserinFirebase();
                    }else{
                        showAlert("Rellena los campos de email contraseña");
                    }
                    break;
                case R.id.btnSignOut:
                    signOutOfFirebase();
                    cleanFields();
                    break;
                case R.id.btnGoogle:
                    googleSignIn();
                    break;
                default:
                    break;
            }
        }
    };

    private void createUserinFirebase(String email, String password){
        //Creamos un usuario con correo y contraseña en firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword
                (email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = myAuth.getCurrentUser();
                            updateUi(user);
                        }else{
                            showAlert(task.getException().toString());
                        }
                    }
                });

    }

    private void loginUserinFirebase(){
        //Login con un usuario y contraseña
        FirebaseAuth.getInstance().signInWithEmailAndPassword
                (etEmail.getText().toString(),etPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = myAuth.getCurrentUser();
                            updateUi(user);
                            showAlert("Correctamente logeado");
                            enableSignOut();
                            goRegister();
                        }else{
                            showAlert("Ha ocurrido un error con el registro");
                        }
                    }
                });

    }

    private void signOutOfFirebase(){
        FirebaseAuth.getInstance().signOut();
    }


    //Método on activity result
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN){
            //Lanzamos la tarea de autenticación con google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                updateUi(account);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                showAlert("Problemas con el registro con Google "+e.getMessage());
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        myAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = myAuth.getCurrentUser();
                            updateUi(user);
                        }else{
                            showAlert("Problemas con la autenticación de google con Firebase");
                        }
                    }
                });
    }

    private boolean checkFields(){
        return !this.etEmail.getText().toString().isEmpty() &&
                !this.etPassword.getText().toString().isEmpty();
    }

    private void showAlert(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle("Error")
                .setPositiveButton("Sí", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateUi(FirebaseUser user){
        if(user != null){
            this.tvEmail.setText(user.getEmail());
            this.tvProvider.setText(user.getProviderId());
        }else{
            showAlert("El usuario llegó null");
        }
    }

    private void updateUi(GoogleSignInAccount account){
        if(account != null){
            this.tvEmail.setText(account.getEmail());
            this.tvProvider.setText(account.getDisplayName());
        }else{
            showAlert("El usuario llegó null");
        }
    }

    private void enableSignOut(){
        this.btnSignOut.setEnabled(true);
    }

    private void cleanFields(){
        etEmail.setText("");
        etPassword.setText("");
        tvEmail.setText("");
        tvProvider.setText("");
    }

    private void goRegister(){
        Intent intent = new Intent(this, RegisterActivity.class);
        FirebaseUser user = myAuth.getCurrentUser();
        intent.putExtra("mail", user.getEmail());
        startActivity(intent);
    }
}