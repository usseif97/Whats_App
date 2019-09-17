package com.example.usseif.whatsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {

    private DatabaseReference chatRequestsRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    private View requestsView;
    private RecyclerView requestsList;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        requestsList = requestsView.findViewById(R.id.requests_list);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestsRef.child(currentUserID), Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {
                holder.itemView.findViewById(R.id.user_profile_name).setVisibility(View.INVISIBLE);
                holder.itemView.findViewById(R.id.user_status).setVisibility(View.INVISIBLE);
                holder.itemView.findViewById(R.id.user_profile_image).setVisibility(View.INVISIBLE);


                final String userID = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String requestTpe = dataSnapshot.getValue().toString();
                            if (requestTpe.equals("received")){   // RECEIVED

                                holder.itemView.findViewById(R.id.user_profile_name).setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.user_status).setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.user_profile_image).setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.accept_request_button).setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.decline_request_button).setVisibility(View.VISIBLE);

                                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")){
                                            final String profileimage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(profileimage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }
                                        final String username = dataSnapshot.child("name").getValue().toString();
                                        final String userstatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(username);
                                        holder.userStatus.setText("Wants To Connect With You");

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Accept","Decline"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(username + " Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {

                                                        if (i == 0){ // Accept Case
                                                            contactsRef.child(currentUserID).child(userID).child("Contact")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        chatRequestsRef.child(currentUserID).child(userID)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    chatRequestsRef.child(userID).child(currentUserID)
                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                            contactsRef.child(userID).child(currentUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Toast.makeText(getContext(), "The Other Contact Added You", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        } else if (i == 1){ // Decline Case
                                                            chatRequestsRef.child(currentUserID).child(userID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        chatRequestsRef.child(userID).child(currentUserID)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(getContext(), "Contact Removed", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            else if (requestTpe.equals("sent")){  // SENT

                                holder.itemView.findViewById(R.id.user_profile_name).setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.user_status).setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.user_profile_image).setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.accept_request_button).setVisibility(View.VISIBLE);

                                Button requestSentButton = holder.itemView.findViewById(R.id.accept_request_button);
                                requestSentButton.setText("Request Sent");
                                //holder.itemView.findViewById(R.id.decline_request_button).setVisibility(View.INVISIBLE);

                                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")){
                                            final String profileimage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(profileimage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }
                                        final String username = dataSnapshot.child("name").getValue().toString();
                                        final String userstatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(username);
                                        holder.userStatus.setText("You Have Sent a Request To " + username);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Cancel Chat Request"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already Sent Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {

                                                        if (i == 0){ // Cancel Case
                                                            chatRequestsRef.child(currentUserID).child(userID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        chatRequestsRef.child(userID).child(currentUserID)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(getContext(), "You Have Cancelled Chat Request", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                return viewHolder;
            }
        };
        requestsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, declineButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            acceptButton = itemView.findViewById(R.id.accept_request_button);
            declineButton = itemView.findViewById(R.id.decline_request_button);

        }
    }


}
