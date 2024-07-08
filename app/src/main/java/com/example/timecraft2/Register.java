package com.example.timecraft2;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail,editTextPassword,editTextConfirmPassword;
    Button signUp;
    TextView signIn;
    FirebaseAuth  mAuth;
    ProgressBar progressBar;
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent i=new Intent(Register.this,Home.class);
            startActivity(i);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();
        editTextEmail=findViewById(R.id.editTextEmail);
        editTextPassword=findViewById(R.id.editTextPassword);
        editTextConfirmPassword=findViewById(R.id.editTextConfirmPassword);
        signUp=findViewById(R.id.buttonRegister);
        signIn=findViewById(R.id.linkLogin);
        progressBar=findViewById(R.id.progressBar);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email=String.valueOf(editTextEmail.getText());
                String password=String.valueOf(editTextPassword.getText());
                String confirmpassword=String.valueOf(editTextConfirmPassword.getText());
                if(email.isEmpty() || password.isEmpty() || confirmpassword.isEmpty()){
                    Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmpassword)) {
                    Toast.makeText(Register.this,"Passwords are not matching",Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "Account Created..",
                                                Toast.LENGTH_SHORT).show();
                                        Intent i=new Intent(Register.this, Login.class);
                                        startActivity(i);
                                        finish();

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
            }
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(Register.this, Login.class);
                startActivity(i);
                finish();
            }
        });
    }
}