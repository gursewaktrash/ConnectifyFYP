package org.connectifyhk.connectify_fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import org.connectifyhk.connectify_fyp.R;

import org.connectifyhk.connectify_fyp.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //ActionBar n title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Chats");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //bottom nav
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        //Set Chats Selected
        navigationView.setSelectedItemId(R.id.nav_chat);


        getSupportFragmentManager().beginTransaction().replace(R.id.content,new fragment_chatslist()).commit();

        checkUserStatus();

        //Update Token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        //perform Selection
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //handle item clicks
                switch (menuItem.getItemId()) {

                    case R.id.nav_chat:
                        //Chat fragment
                        actionBar.setTitle("Chats");
                        fragment_chatslist fragment1 = new fragment_chatslist();
                        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                        ft1.replace(R.id.content, fragment1,"");
                        ft1.commit();
                        return true;

                    case R.id.nav_profile:
                        //profile fragment
                        actionBar.setTitle("Profile");
                        fragment_profile fragment2 = new fragment_profile();
                        FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                        ft2.replace(R.id.content, fragment2,"");
                        ft2.commit();
                        return true;

                    case R.id.nav_users:
                        //users fragment
                        actionBar.setTitle("Users");
                        fragment_users fragment3 = new fragment_users();
                        FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                        ft3.replace(R.id.content, fragment3,"");
                        ft3.commit();
                        return true;

                }
                return false;
            }
        });

    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }


    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //user is signed in Stay Here
            //set email for logged in user
            mUID = user.getUid();

            //Save uid of signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();
        }
        else{
            //user is not signed in. Go to main actavity
            startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();
        }

    }

    @Override
    protected void onStart() {
        //Check on start of app
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }


}