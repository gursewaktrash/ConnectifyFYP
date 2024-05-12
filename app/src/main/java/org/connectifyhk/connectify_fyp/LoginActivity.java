package org.connectifyhk.connectify_fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.connectifyhk.connectify_fyp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button btn_signup, btn_login;
    EditText metEmail, metPassword;
    TextView forgotpass;

    //Declare an instance of FirebaseAuth
     FirebaseAuth mAuth;

    //progress dialog
    ProgressDialog pd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //action bars and titltes
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        //enable back button
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setDisplayShowHomeEnabled(true);

        //init
        btn_signup = findViewById(R.id.btnSignup);
        btn_login = findViewById(R.id.btnLogin);
        metEmail = findViewById(R.id.etEmail);
        metPassword = findViewById(R.id.etPassword);
        forgotpass = findViewById(R.id.forgotpass);

        /// Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            finish();
            startActivity(new Intent(LoginActivity.this,DashboardActivity.class));
        }
        //init progress dialog
        pd = new ProgressDialog(this);

        //Sign UP BTN
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Forgot pass btn
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Goes to Forgot password page
                startActivity(new Intent(LoginActivity.this,ForgotPassword.class));
            }
        });


        //login btn
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String email = metEmail.getText().toString();
                String passw = metPassword.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //invaid email pattern set error
                    metEmail.setError("Invalid Email");
                    metEmail.setFocusable(true);
                }
                else if (metPassword.length()<1) {
                    //set error
                    metPassword.setError("Field Can't Be Empty");
                    metPassword.setFocusable(true);
                }
                else {
                    //vaid email pattern
                    loginUser(email,passw);
                }

            }
        });

    }

    //Login BTN usercheck
    private void loginUser(String email, String passw) {
        //Show progress dialog
        pd.setMessage("Logging In");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismiss dialog
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //user is logged in , start Login Activity
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            //dismiss dialog
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {//dismiss dialog
                pd.dismiss();

                //error, get and show message
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }




}