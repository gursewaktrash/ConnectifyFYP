package org.connectifyhk.connectify_fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.connectifyhk.connectify_fyp.R;

import org.connectifyhk.connectify_fyp.adapters.AdapterChat;
import org.connectifyhk.connectify_fyp.models.ModelChat;
import org.connectifyhk.connectify_fyp.models.ModelUser;
import org.connectifyhk.connectify_fyp.notifications.APIService;
import org.connectifyhk.connectify_fyp.notifications.Client;
import org.connectifyhk.connectify_fyp.notifications.Data;
import org.connectifyhk.connectify_fyp.notifications.Response;
import org.connectifyhk.connectify_fyp.notifications.Sender;
import org.connectifyhk.connectify_fyp.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    //Views
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTV;
    EditText messageEt;
    ImageButton sendBtn, attachBtn;


    //firebase auth
    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    //Checking if user has seen the message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    APIService apiService;
    boolean notify = false;

    //Permission constrants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //Arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    //Uri of Picked Images
    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Init Views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerview);
        profileIv = findViewById(R.id.proifleIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTV = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageET);
        sendBtn = findViewById(R.id.sendBtn);
        attachBtn = findViewById(R.id.attachBtn);

        //init arrays permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Layout for Rcycler View
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //Properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        //Create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        //Clicking a user pass the UID by intent so GET DP NAme and start chat
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //Search user to get User Info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
        //Get users Dp
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Check till data is recived
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //Get Data
                    String name =""+ds.child("name").getValue();
                    hisImage =""+ds.child("image").getValue();
                    String typingStatus =""+ds.child("typingTo").getValue();

                    //Check Typing status
                    if (typingStatus.equals(myUid)){
                        userStatusTV.setText("typing...");
                    }else{

                        //Get value of onlinestatus
                        String onlineStatus = ""+ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            userStatusTV.setText(onlineStatus);
                        }
                        else{
                            //Convet into proper time
                            //Convert time stamp to mm//yy and etc.
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            try {
                                cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                            userStatusTV.setText("Last seen at: "+ dateTime);
                        }
                    }


                    //Set Data
                    nameTv.setText(name);
                    try {
                        //image recieved and set it to toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_defaultface).into(profileIv);

                    }
                    catch (Exception e){
                        //image problem and set it to default image logo
                        Picasso.get().load(R.drawable.ic_defaultface).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Send BTN Clicked to send message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                //Get text which needs toi be send
                String message = messageEt.getText().toString().trim();
                //Check if its empty ot not
                if (TextUtils.isEmpty(message)){
                    //Text is empty
                    Toast.makeText(ChatActivity.this,"Empty Message!",Toast.LENGTH_SHORT).show();
                }
                else{
                    //Text is ready to send
                    sendMessage(message);
                }
                //Reset Text Area After Sending Message
                messageEt.setText("");
            }
        });

        //Click button to send image
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Image pick dialog
                showImagePicDialog();
            }
        });

        //Check Edit text to Listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() ==0){
                    checkTypingStatus("onOne");
                }
                else {
                    checkTypingStatus(hisUid); //Uid of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessages();

        seenMessage();
    }

    private void showImagePicDialog(){
        //Show dialog containing options for camera and gallery for image

        String options [] = {"Camera","Gallery"};
        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set Title
        builder.setTitle("Pick Image From");
        //Set items to dialog box
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Handle Dialog Item Clicks
                if (which == 0){

                    //Camera Clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1){

                    //Gallery Clicked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }

                    else {
                        pickFromGallery();
                    }

                }


            }
        });
        //Create N Show Dialog
        builder.create().show();

    }

    private void pickFromCamera(){
        //Intent picking image from device
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery(){
        //Pick from gallery
        Intent galleryintent = new Intent(Intent.ACTION_PICK);
        galleryintent.setType("image/");
        startActivityForResult(galleryintent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission(){
        //Check if enabled or no
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private  void requestStoragePermission(){
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //Check if enabled or no
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private  void requestCameraPermission(){
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }


    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }

                    //Adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //Set adapter to recycler view
                    recyclerView.setAdapter(adapterChat);
                    //So that wont have to scroll to see messages
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(final String message) {
        //Chat node created whever message sent to store messages
        //Saving the sender , reciever and the message

        String timestamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);



        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user = dataSnapshot.getValue(ModelUser.class);

                if (notify){
                    sendNotification(hisUid, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Create Chatlist  node in firebase database
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        notify = true;

        //Progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Image");
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/"+ timeStamp;

        //Get bitmap from image URI
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Image Uploaded
                        progressDialog.dismiss();
                        //Get Url of the image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            //Setup required Data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", hisUid);
                            hashMap.put("message", downloadUri);
                            hashMap.put("timeStamp", timeStamp);
                            hashMap.put("type", "image");
                            hashMap.put("isSeen", false);
                            //Put this into firebase
                            databaseReference.child("Chats").push().setValue(hashMap);

                            //Send Notifications
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                            database.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ModelUser user = dataSnapshot.getValue(ModelUser.class);

                                    if (notify){
                                        sendNotification(hisUid, user.getName(), "Sent You A Photo");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //Create Chatlist  node in firebase database
                            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(myUid)
                                    .child(hisUid);
                            chatRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatRef1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(hisUid)
                                    .child(myUid);
                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatRef2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });



                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Failed
                progressDialog.dismiss();
            }
        });
    }

    private void sendNotification(final String hisUid, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, name+":"+message, "New Message", hisUid, R.drawable.ic_face_purp);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this,""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in Stay Here
            //set email for logged in user
            myUid = user.getUid(); //Current signed in User's UID
        } else {
            //user is not signed in. Go to main actavity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        //Update status in database
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        //Update status in database
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //Set Online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Get Time
        String timeStamp = String.valueOf(System.currentTimeMillis());

        //Set offline with last seen with time
        checkOnlineStatus(timeStamp);
        checkTypingStatus("onOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //Set Online
        checkOnlineStatus("online");
        super.onResume();
    }

    //Handle Permisiion Results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Method called when user Allow or Deny

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //Picking from camera, first check if permissions allowed
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        //Permissions enabled
                        pickFromCamera();
                    }
                    else {
                        //Permissions denied
                        Toast.makeText(this,"Please enable camera & storage permission", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{


                //Picking from gallery, first check if permissions allowed
                if (grantResults.length >0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //Permissions enabled
                        pickFromGallery();
                    }
                    else {
                        //Permissions denied
                        Toast.makeText(this,"Please enable storage permission", Toast.LENGTH_SHORT).show();

                    }
                }
                break;
            }
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //After picking image
        if (resultCode == RESULT_OK){


            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery; get its uri
                image_uri = data.getData();

                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is picked from camera; get its uri

                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Hide searchview, since dont need it
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        int id =item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}