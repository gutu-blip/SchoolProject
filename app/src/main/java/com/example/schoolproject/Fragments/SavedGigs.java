package com.example.schoolproject.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Adapters.ListAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;

public class SavedGigs extends Fragment implements CallBackListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser = mUser.getUid();
    DatabaseReference mSavedRef;
    ArrayList<Listing> mGigs;
    ProgressBar progressCircle;
    ListAdapter mAdapter;
    TextView mPlaceHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mylistings, container, false);

        initWidgets(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchGigs();
    }

    private void fetchGigs() {
        mGigs = new ArrayList<>();
        mSavedRef = database.getReference("Listings").child("Gigs");
        mSavedRef.keepSynced(true);

        //SavedKeys
        ArrayList<String> productKeys = new ArrayList<>();
        database.getReference("Saved").child("Gigs").child(currentUser).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot keySnapshot : snapshot.getChildren()) {
                    productKeys.add(String.valueOf(keySnapshot.child("postKey").getValue()));
                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });

        mSavedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot collegeSnapshot : snapshot.getChildren()) {
                    for (String postKey : productKeys) {
                        if (collegeSnapshot.hasChild(postKey)) {
                            Listing listing = collegeSnapshot.child(postKey).getValue(Listing.class);

                            if (listing != null) {

                                mGigs.add(listing);
                            }
                        }
                    }
                }
                onLoadGig(mGigs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onLoadGig(ArrayList<Listing> mGigs) {
        progressCircle.setVisibility(View.INVISIBLE);

        if(mGigs.isEmpty()){
            mPlaceHolder.setText("You have no saved gigs yet");
        }else {
            mAdapter = new ListAdapter(mGigs, getActivity(), "SavedGigs", SavedGigs.this);

            SlideInRightAnimationAdapter alphaInAnimationAdapter = new SlideInRightAnimationAdapter(mAdapter);
            alphaInAnimationAdapter.setDuration(700);
            alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
            alphaInAnimationAdapter.setFirstOnly(false);

            recyclerView.setAdapter(alphaInAnimationAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initWidgets(View view) {

        mPlaceHolder = view.findViewById(R.id.tv_placeholder);
        fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setVisibility(View.GONE);
        progressCircle = view.findViewById(R.id.progress_circle);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
    }

    @Override
    public String toString() {
        String title = "Gigs";
        return title;
    }

    @Override
    public void callBack(int position) {
        Listing listing = mGigs.get(position);
        SavedServices.saved.trade = "Gigs";
        SavedServices.saved.dialogEvent(listing, getActivity());
    }
}