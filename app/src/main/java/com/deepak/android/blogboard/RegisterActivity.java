package com.deepak.android.blogboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passField;
    private EditText confirmPass;
    private Button returnToLogin;
    private Button createAccount;
    private ProgressBar regProgress;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        //initializing the registry layout objects.
        emailField = findViewById(R.id.registry_email);
        passField = findViewById(R.id.registry_password);
        confirmPass = findViewById(R.id.confirm_password);
        createAccount = findViewById(R.id.create_account);
        returnToLogin = findViewById(R.id.login_to_account);
        regProgress = findViewById(R.id.registration_progress);

        returnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //registration process
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String pass = passField.getText().toString();
                String pass2 = confirmPass.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(pass2)){
                    if (pass.equals(pass2)){
                        regProgress.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Intent setupIntent = new Intent(RegisterActivity.this, AccountSetup.class);
                                    startActivity(setupIntent);
                                    finish();
                                }else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                                regProgress.setVisibility(View.INVISIBLE);
                            }
                        });


                    }else{
                        String error = "Email or Password Field is Empty or password does not matches. Try Again";
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Fire base initialization if the user logged in.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            returnToMain();
        }
    }
    //main activity intent.
    private void returnToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this , MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
