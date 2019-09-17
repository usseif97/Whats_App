package com.example.usseif.whatsapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {


    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    private View privateChatsView;
    private RecyclerView chatsList;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = privateChatsView.findViewById(R.id.chat_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatsView;
    }


    @Override
    public void onStart(){

        super.onStart();

        /* FirebaseRecyclerOptions & FirebaseRecyclerAdapter both are from Firebase UI Library */
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) { // get Data From Database
                final String userID = getRef(position).getKey();                                                      // Then view it in layout
                final String[] image = {"default_image"};

                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.hasChild("image")){
                                image[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(image[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }

                            final String name = dataSnapshot.child("name").getValue().toString();
                            final String status = dataSnapshot.child("status").getValue().toString();
                            holder.userName.setText(name);

                            if (dataSnapshot.child("userState").hasChild("state")){
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time  = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")){
                                    holder.userStatus.setText("Online");
                                } else if (state.equals("offline")){
                                    holder.userStatus.setText("Last Seen: " + date + " " + time);
                                }

                            } else {
                                holder.userStatus.setText("Offline");
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", userID);
                                    chatIntent.putExtra("visit_user_name", name);
                                    chatIntent.putExtra("visit_user_image", image[0]);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {  // Join with the Layout
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                return viewHolder;
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView){
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }

}