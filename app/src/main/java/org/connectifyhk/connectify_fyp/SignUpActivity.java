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
import android.widget.Toast;

import org.connectifyhk.connectify_fyp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[a-z])" +
                    "(?=.*[A-Z])" +
                    "(?=\\S+$)" +
                    ".{6,12}" +
                    "$");



    EditText musername, memail, mpassword, mconpassowrd;
    Button btn_signup, btn_login;


    //progressbar to display while registering user
    ProgressDialog progressDialog;
    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //action bars and titltes
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        //enable back button
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setDisplayShowHomeEnabled(true);

        //INIT
        musername = findViewById(R.id.etName);
        memail = findViewById(R.id.etEmail);
        mpassword = findViewById(R.id.etPassword);
        mconpassowrd = findViewById(R.id.etConPassword);
        btn_signup = findViewById(R.id.btnSignup);
        btn_login = findViewById(R.id.btnLogin);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User");


        //Signup Btn click
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input
                String email = memail.getText().toString().trim();
                String password = mpassword.getText().toString().trim();
                String conpassword = mconpassowrd.getText().toString().trim();
                String username = musername.getText().toString().trim();

                //validate
                if (username.length()<1){
                    //set error
                    musername.setError("Field Can't Be Empty");
                    musername.setFocusable(true);

                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error
                    memail.setError("Invalid Email");
                    memail.setFocusable(true);
                }
                 else if (!PASSWORD_PATTERN.matcher(password).matches()){
                    //set error
                    mpassword.setError("Password Must Be 6-12 Characters, And Include At Least One Lowercase Letter, And One Uppercase Letter, And A Number!");
                    mpassword.setFocusable(true);

                }
                 else if (password.equals(conpassword)) {
                    //set error
                    registerUser(username, email, password); //register user
                }
                 else {
                    mconpassowrd.setError("Password Does Not Match");
                    mconpassowrd.setFocusable(true);
                }
            }
        });


        //Login BTN CLICK
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }



    private void registerUser(String username, String email, String password) {
        //All are valid email password username
        progressDialog.show();


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            //Get User email and UID from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            String bio = "Hi There Im Using Chat App";
                            String phone = "Phone";
                            String username = musername.getText().toString().trim();

                            //Store user data with hashmap
                            // using hashmap
                            HashMap<Object,String> hashMap = new HashMap<>();
                            //put info into hashmap
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name",username);
                            hashMap.put("onlineStatus","online"); // will put later (E.G Edit Profile)
                            hashMap.put("typingTo","noOne"); // will put later (E.G Edit Profile)
                            hashMap.put("image",""); // will put later (E.G Edit Profile)
                            hashMap.put("bio",bio);
                            hashMap.put("phone",phone);

                            //Firebase database isntance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //Path to store named data "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //Put data within hashmap
                            reference.child(uid).setValue(hashMap);



                            Toast.makeText(SignUpActivity.this,"Registered...\n"+user.getEmail(),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                            finish();
                        }
                        else{
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Error Dismiss Dialog
                progressDialog.dismiss();
                Toast.makeText(SignUpActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }



}