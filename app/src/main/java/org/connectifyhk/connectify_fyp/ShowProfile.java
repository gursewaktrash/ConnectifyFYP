package org.connectifyhk.connectify_fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.TextView;

import org.connectifyhk.connectify_fyp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ShowProfile extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String UserID;

    //Views
    ImageView avatarIv;
    TextView nameTv, emailTv, phoneTv , bioTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        //Init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        //init view
        avatarIv = findViewById(R.id.avatarIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        bioTv = findViewById(R.id.bioTv);
        phoneTv = findViewById(R.id.phoneTv);

        //Get uid of cliked user
        Intent intent = getIntent();
        UserID = intent.getStringExtra("uid");

        //Retrive user detials by email
        //by using orderByChild query
        Query query = databaseReference.orderByChild("uid").equalTo(UserID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //check till required data
                for (DataSnapshot ds : snapshot.getChildren()){
                    //Get data
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String about = ""+ds.child("bio").getValue();

                    //Set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    bioTv.setText(about);


                    try {
                        //if image is recieved then set
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        // if no exception in getting image set Default
                        Picasso.get().load(R.drawable.male).into(avatarIv);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}