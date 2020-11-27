package com.example.clase11;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.clase11.dto.TipoUsuario;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        validacionUsuario();
    }

    public void login(View view) {

        List<AuthUI.IdpConfig> proveedores = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        AuthMethodPickerLayout authMethodPickerLayout =
                new AuthMethodPickerLayout.Builder(R.layout.loginlayout)
                        .setEmailButtonId(R.id.btnEmailPassword)
                        .setGoogleButtonId(R.id.btnGoogle)
                        .build();

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(proveedores)
                .build();

        startActivityForResult(intent, 1);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            validacionUsuario();
        }
    }

    public void validacionUsuario() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                goToMainScreen(currentUser);
            } else {
                currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (currentUser.isEmailVerified()) {
                            goToMainScreen(currentUser);
                        } else {
                            Toast.makeText(MainActivity.this, "Se le ha enviado un correo para verificar su cuenta", Toast.LENGTH_SHORT).show();
                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("infoApp", "correo enviado");
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public void goToMainScreen(FirebaseUser currentUser) {

        String uid = currentUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        TipoUsuario tipoUsuario = new TipoUsuario();

        if(currentUser.getEmail().equals("stuardo.lucho@gmail.com")){
            tipoUsuario.setRol("admin");
        }

        reference.child("users").child(uid).setValue(tipoUsuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("infoApp", "info guardada exitosamente");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("infoApp", "Error al guardar");
                        e.printStackTrace();
                    }
                });

        if(tipoUsuario.getRol().equals("admin")){
            startActivity(new Intent(MainActivity.this, AdminActivity.class));
        }else{
            startActivity(new Intent(MainActivity.this, MainActivity2.class));
        }

        finish();
    }

}