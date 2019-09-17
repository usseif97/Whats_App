package com.example.usseif.whatsapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private DatabaseReference rootRef;

    private View groupFragmentView;
    private ListView listView;

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> groupsList = new ArrayList<>();


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView =  inflater.inflate(R.layout.fragment_groups, container, false);

        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        RetrieveUserGroups();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = parent.getItemAtPosition(position).toString();

                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);
            }
        });

        return groupFragmentView;
    }


    private void InitializeFields(){
        listView = groupFragmentView.findViewById(R.id.groups_list_view);
        arrayAdapter = new ArrayAdapter<String >(getContext(), android.R.layout.simple_expandable_list_item_1, groupsList);
        listView.setAdapter(arrayAdapter);
    }

    private void RetrieveUserGroups(){
        rootRef.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> groupsSet = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    groupsSet.add(((DataSnapshot)iterator.next()).getKey());
                }
                groupsList.clear();
                groupsList.addAll(groupsSet);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
