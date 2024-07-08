package com.example.timecraft2;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class Login extends AppCompatActivity {
    EditText loginEmail,loginPassword;
    Button signIn;
    TextView signUp;
    ProgressBar progressBar;
    FirebaseAuth  mAuth;
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent i=new Intent(Login.this,Home.class);
            startActivity(i);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progressBar);
        loginEmail=findViewById(R.id.editTextEmail);
        loginPassword=findViewById(R.id.editTextPassword);
        signIn=findViewById(R.id.buttonLogin);
        signUp=findViewById(R.id.linkRegister);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email=String.valueOf(loginEmail.getText());
                String password=String.valueOf(loginPassword.getText());
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(Login.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Login Successful",
                                            Toast.LENGTH_SHORT).show();
                                    Intent i=new Intent(Login.this,Home.class);
                                    startActivity(i);
                                    finish();
                                } else {

                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        signUp.setOnClickListener(v -> {
            Intent i =new Intent(Login.this, Register.class);
            startActivity(i);
            finish();
        });
    }
    }
