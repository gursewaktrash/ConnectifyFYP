package org.connectifyhk.connectify_fyp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.connectifyhk.connectify_fyp.R;

import org.connectifyhk.connectify_fyp.adapters.AdapterUsers;
import org.connectifyhk.connectify_fyp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_users#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_users extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;


    //firebase auth
    FirebaseAuth firebaseAuth;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_users() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_users.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_users newInstance(String param1, String param2) {
        fragment_users fragment = new fragment_users();
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
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //init Recycler View
        recyclerView = view.findViewById(R.id.users_recyclerView);
        //Set properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Init userlist
        userList = new ArrayList<>();

        //GET ALL USERS
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        //Get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //Get path to Users containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //Get all data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //Get all users excpet logged in one
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //Set adapter
                    recyclerView.setAdapter(adapterUsers);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Search Users
    private void searchUsers(final String query) {

        //Get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //Get path to Users containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //Get all data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //Get all searched users excpet logged in one
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }

                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //Refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //Set adapter
                    recyclerView.setAdapter(adapterUsers);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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


    //Inflate options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating enu
        inflater.inflate(R.menu.menu_main, menu);

        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //Search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search btn
                //if not empty
                if (!s.equals("")){
                    //Search contains text, search it
                    searchUsers(s);
                }
                else {
                    //search is empty show all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // called whenever press a any letter
                if (!s.equals("")){
                    //Search contains text, search it
                    searchUsers(s);
                }
                else {
                    //search is empty show all users
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    //handle menu item click
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