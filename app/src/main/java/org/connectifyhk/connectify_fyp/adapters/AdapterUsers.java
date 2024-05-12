package org.connectifyhk.connectify_fyp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.connectifyhk.connectify_fyp.ChatActivity;
import org.connectifyhk.connectify_fyp.ShowProfile;
import org.connectifyhk.connectify_fyp.models.ModelUser;
import org.connectifyhk.connectify_fyp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;

    //Constructor


    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get Data
        String hisUID = userList.get(i).getUid();
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        final String userEmail = userList.get(i).getEmail();

        //Set Data
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_face_purp).into(myHolder.mAvatarIv);
        }
        catch (Exception e){

        }
        //Handle item click
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show dialog box
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //Profile Clicked
                            Intent intent = new Intent(context, ShowProfile.class);
                            intent.putExtra("uid", hisUID);
                            context.startActivity(intent);

                        }
                        if (which==1){
                            //Click user from user list to start chatting
                            //Activity by User UID
                            //Using the UID to find the user to text with
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid", hisUID);
                            context.startActivity(intent);
                        }

                    }
                });
                builder.create().show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv;
        TextView mNameTv, mEmailTv;



        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //Init Views
            mAvatarIv  = itemView.findViewById(R.id.avatarIv);
            mNameTv  = itemView.findViewById(R.id.nameTv);
            mEmailTv  = itemView.findViewById(R.id.emailTv);

        }
    }
}
