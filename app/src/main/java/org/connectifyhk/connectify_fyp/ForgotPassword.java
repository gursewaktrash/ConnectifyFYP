package org.connectifyhk.connectify_fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.connectifyhk.connectify_fyp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    EditText etPasswordEmail;
    Button btnPasswordreset;

    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //Views
        etPasswordEmail = findViewById(R.id.etPasswordEmail);
        btnPasswordreset = findViewById(R.id.btnPasswordreset);
        firebaseAuth = FirebaseAuth.getInstance();


        btnPasswordreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = etPasswordEmail.getText().toString().trim();

                if ((useremail.equals(""))){
                    Toast.makeText(ForgotPassword.this,"Please Enter Your Registered Email Address",Toast.LENGTH_SHORT).show();
                }
                else {
                    firebaseAuth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ForgotPassword.this,"Password Reset Email Sent!",Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(ForgotPassword.this,LoginActivity.class));
                            }
                            else {
                                Toast.makeText(ForgotPassword.this,"Error In Sending Password Reset Email!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}