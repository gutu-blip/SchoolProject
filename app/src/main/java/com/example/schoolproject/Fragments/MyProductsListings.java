package com.example.schoolproject.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Activites.ProductListing;
import com.example.schoolproject.Activites.ProductStatsActivity;
import com.example.schoolproject.Adapters.ListingsAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;

import java.util.ArrayList;

public class MyProductsListings extends Fragment implements CallBackListener,ProductStatsActivity.DeleteListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    DatabaseReference mProductRef;
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser = mUser.getUid();
    ArrayList<Listing> mListings;
    ListingsAdapter listingsAdapter;
    ProgressBar progressCircle;
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

        fetchProducts();
        setListeners();

    }

    private void setListeners() {

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ProductListing.class);
                intent.putExtra("listingSource","individual");
                startActivity(intent);

            }
        });
    }

    private void fetchProducts() {


        mListings = new ArrayList<>();
        mProductRef = database.getReference("Listings").child("Products");
        mProductRef.keepSynced(true);

        mProductRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot collegeSnapshot :  snapshot.getChildren()){
                    for(DataSnapshot keySnapshot :  collegeSnapshot.getChildren()){

                        Listing listing = keySnapshot.child("Details").getValue(Listing.class);

                        if(listing.getUserID().equals(currentUser)){

                            ArrayList<String> album = new ArrayList<>();
                            for (DataSnapshot imageKey : keySnapshot.child("Album").getChildren()) {
                                String image = String.valueOf(imageKey.child("imageUrl").getValue());
                                album.add(image);
                            }
                            listing.setAlbum(album);

                            mListings.add(listing);
                        }
                    }
                }
                progressCircle.setVisibility(View.INVISIBLE);

                if(mListings.isEmpty()){
                    mPlaceHolder.setText("You have no product listings yet");
                }else {
                    listingsAdapter = new ListingsAdapter(getActivity(), mListings,
                            MyProductsListings.this);
                    recyclerView.setAdapter(listingsAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initWidgets(View view) {
        mPlaceHolder = view.findViewById(R.id.tv_placeholder);
        fabAdd = view.findViewById(R.id.fab_add);
        progressCircle = view.findViewById(R.id.progress_circle);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    }

    @Override
    public void callBack(int position) {

        Listing listing = mListings.get(position);

        Intent intent = new Intent(getActivity(), ProductStatsActivity.class);
        intent.putExtra("listing",listing);

        startActivity(intent);
    }

    @Override
    public String toString() {
        String title = "Products";
        return title;
    }

    @Override
    public void onDeleteListener() {
//      mListings.remove(position);
        listingsAdapter.notifyDataSetChanged();
    }
}