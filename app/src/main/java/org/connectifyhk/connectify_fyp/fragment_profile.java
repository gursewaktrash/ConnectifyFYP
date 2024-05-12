package org.connectifyhk.connectify_fyp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.connectifyhk.connectify_fyp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_profile#newInstance} factory method to
 * create an instance of this fragment.
 */


public class fragment_profile extends Fragment {

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //Storage
    StorageReference storageReference;
    //Path where images of users will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    //Views
    ImageView avatarIv;
    TextView nameTv, emailTv, phoneTv , bioTv;
    ImageButton namebtn, biobtn, phonebtn;
    Button logoutbtn , deleteaccbtn;
    FloatingActionButton fabImage;

    //Progress Dialog
    ProgressDialog pd;

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

    //For checking profile photo
    String uploadProfilePhoto;


    //Set Chats Selected
    //navigationView.setSelectedItemId(R.id.nav_chat);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_profile.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_profile newInstance(String param1, String param2) {
        fragment_profile fragment = new fragment_profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("Users_Profile_Cover_Imgs/");




        //init arrays permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init view
        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        bioTv = view.findViewById(R.id.bioTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        namebtn = view.findViewById(R.id.namebtn);
        biobtn = view.findViewById(R.id.biobtn);
        phonebtn = view.findViewById(R.id.phonebtn);
        fabImage = view.findViewById(R.id.fabImage);
        logoutbtn = view.findViewById(R.id.logoutbtn);
        deleteaccbtn = view.findViewById(R.id.deleteaccbtn);



        //Init Progress Dialog
        pd = new ProgressDialog(getActivity());




        //Retrive user detials by email
        //by using orderByChild query
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
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

        //Name Button Click
        namebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editname();
            }
        });

        //Phone Button Click
        phonebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editphone();
            }
        });

        //About Button Click
        biobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editbio();
            }
        });

        //Image Button Click
        fabImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Updating Profile Picture");
                uploadProfilePhoto = "image";
                showImagePicDialog();
            }
        });

        //Logout Button Click
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    firebaseAuth.signOut();
                    checkUserStatus();
            }
        });



        //Delete Acc Click
        deleteaccbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Alert Dialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                //Set Title
                dialog.setTitle("Are you sure?");
                //Set message to dialog box
                dialog.setMessage("Deleting this account will result in completely removing your " +
                        "account from the system and you won't be able to access the app.");
                //Set buttons to dialog box
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference deleteuser = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child(user.getUid());
                        deleteuser.removeValue();
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(getActivity(),"Account Successfully Deleted",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getActivity(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(getActivity(),"Error Occurred While Deleting Account",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();

            }
        });

        return view;
    }



    private void showImagePicDialog(){
        //Show dialog containing options for camera and gallery for image

        String options [] = {"Camera","Gallery"};
        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private  void requestCameraPermission(){
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }



    private void editname(){
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update name"); //Ex Update name or phone
        //Set layout for dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //Add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter name");  //hint //Ex Update name or phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Add buttons to dialog
        //Update BTN
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edittext
                String value = editText.getText().toString().trim();
                //validate if user entered something or not
                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("name", value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Updated dismiss
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Failed dimiss, Show Error
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                } else {
                    Toast.makeText(getActivity(), "Enter name", Toast.LENGTH_SHORT).show();

                }

            }
        });
        //Cancel BTN
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        //create n show dialog
        builder.create().show();

    }

    private void editphone(){
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update phone"); //Ex Update name or phone
        //Set layout for dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //Add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter phone");  //hint //Ex Update name or phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Add buttons to dialog
        //Update BTN
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edittext
                String value = editText.getText().toString().trim();
                //validate if user entered something or not
                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("phone", value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Updated dismiss
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Failed dimiss, Show Error
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                } else {
                    Toast.makeText(getActivity(), "Enter phone", Toast.LENGTH_SHORT).show();

                }

            }
        });
        //Cancel BTN
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        //create n show dialog
        builder.create().show();

    }

    private void editbio(){
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update bio"); //Ex Update name or phone
        //Set layout for dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //Add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter bio");  //hint //Ex Update name or phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Add buttons to dialog
        //Update BTN
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edittext
                String value = editText.getText().toString().trim();
                //validate if user entered something or not
                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("bio", value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Updated dismiss
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Failed dimiss, Show Error
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                } else {
                    Toast.makeText(getActivity(), "Enter bio", Toast.LENGTH_SHORT).show();

                }

            }
        });
        //Cancel BTN
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        //create n show dialog
        builder.create().show();

    }




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
                        Toast.makeText(getActivity(),"Please enable camera & storage permission", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(getActivity(),"Please enable storage permission", Toast.LENGTH_SHORT).show();

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

                uploadProfilePhoto(image_uri);

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is picked from camera; get its uri

                uploadProfilePhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private void uploadProfilePhoto(Uri uri) {
        //show progress
        pd.show();
        //get image path
        String filePathAndName = storagePath+ ""+uploadProfilePhoto +"_"+user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //check if image saved on database
                        if (uriTask.isSuccessful()){
                            //image uploaded
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(uploadProfilePhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Saved image
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Image Updated",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Error Updating Image",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else{
                            //Error
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Error occured",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Error
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //user is signed in Stay Here
            //set email for logged in user
        }
        else{
            //user is not signed in. Go to main actavity
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }

    //Menu
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu in fragment
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    //Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Menu Top
        inflater.inflate(R.menu.menu_main,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Handle item click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id =item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }


}