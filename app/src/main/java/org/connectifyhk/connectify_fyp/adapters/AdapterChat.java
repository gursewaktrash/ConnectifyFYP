package org.connectifyhk.connectify_fyp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import org.connectifyhk.connectify_fyp.R;
import org.connectifyhk.connectify_fyp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;


    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Layouts for LEFT Rigtht
        if (i == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {
        //Get data
        String message = chatList.get(i).getMessage();
        String timeStamp = chatList.get(i).getTimestamp();
        String type = chatList.get(i).getType();

        //Convert time stamp to mm//yy and etc.
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        try {
            cal.setTimeInMillis(Long.parseLong(timeStamp));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();


            if(type.equals("text")){
                //Text Message
                myHolder.messageTv.setVisibility(View.VISIBLE);
                myHolder.messageIv.setVisibility(View.GONE);

                myHolder.messageTv.setText(message);
            }
            else {
            //Image Message
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(myHolder.messageIv);
        }


        //Set Data
        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);
        try{
            Picasso.get().load(imageUrl).into(myHolder.profileIv);
        }
        catch (Exception e){

        }

        //Click to show delete dialog
        myHolder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show delete confirm message
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this message");
                //Button to delete
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(i);
                    }
                });
                //Button to cancel
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });
                //Create and show dialog
                builder.create().show();
            }
        });

        //Set seen/delieved status
        if (i ==chatList.size()-1){
            if (chatList.get(i).isSeen()){
                myHolder.isSeenTv.setText("Seen");
            }
            else {
                myHolder.isSeenTv.setText("Delivered");
            }
        }
        else{
            myHolder.isSeenTv.setVisibility(View.GONE);
        }

    }

    private void deleteMessage(int position) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        //Get timestamp of the clicked messages - compare with database in CHATS timestamp message
        //When matched delete that message
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //If sender is deleting message try to match sender value of the message with current user
                    if (ds.child("sender").getValue().equals(myUID)){

                        //Remove the message
                        //Set value of message "This Message was deleted"

                        // 1) Remove the message from database
                        // THIS IS COMMENT --> CODE 4 DELETE FROM DATABASE IS Down
                        // s.getRef().removeValue();

                        // 2) Set value of message "This Message was deleted"
                        //Will show message that it was deleted
                        HashMap<String , Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context,"Message Deleted",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context,"You can delete your own messages only",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //Get the signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }

    //View Holder Class
    class MyHolder extends RecyclerView.ViewHolder{

        //Views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout; // For click listener to show delete

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //Init Views
            profileIv  = itemView.findViewById(R.id.proifleIv);
            messageIv  = itemView.findViewById(R.id.messageIv);
            messageTv  = itemView.findViewById(R.id.messageTv);
            timeTv  = itemView.findViewById(R.id.timeTv);
            isSeenTv  = itemView.findViewById(R.id.isSeenTv);
            messageLayout  = itemView.findViewById(R.id.messageLayout);
        }
    }
}
