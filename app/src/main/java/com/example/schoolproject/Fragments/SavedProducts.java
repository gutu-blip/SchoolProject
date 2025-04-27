package com.example.schoolproject.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.schoolproject.Activites.SavedMerger;
import com.example.schoolproject.Adapters.InterestsAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;

public class SavedProducts extends Fragment implements CallBackListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser = mUser.getUid();
    DatabaseReference mSavedRef;
    ArrayList<Listing> mProducts;
    ProgressBar progressCircle;
    InterestsAdapter mAdapter;
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
    }

    private void fetchProducts() {
        mProducts = new ArrayList<>();
        mSavedRef = database.getReference("Listings").child("Products");
        mSavedRef.keepSynced(true);

        //SavedKeys
        ArrayList<String> productKeys = new ArrayList<>();
        database.getReference("Saved").child("Products").child(currentUser).addValueEventListener(new ValueEventListener() {
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
                            Listing listing = collegeSnapshot.child(postKey).child("Details").getValue(Listing.class);

                            if (listing != null) {
                                ArrayList<String> album = new ArrayList<>();
                                for (DataSnapshot imagekey : collegeSnapshot.child(postKey).child("Album").getChildren()) {
                                    String image = String.valueOf(imagekey.child("imageUrl").getValue());
                                    album.add(image);
                                }

                                listing.setAlbum(album);
                                mProducts.add(listing);

                            }
                        }
                    }
                }
                onLoadProduct(mProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onLoadProduct(ArrayList<Listing> mProducts) {
        progressCircle.setVisibility(View.INVISIBLE);
        if(mProducts.isEmpty()){
            mPlaceHolder.setText("You have no saved products yet");
        }else {
            mAdapter = new InterestsAdapter(getActivity(), mProducts, SavedProducts.this);

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
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

    }

    @Override
    public void callBack(int position) {


        Intent intent = new Intent(getActivity(), SavedMerger.class);

        intent.putExtra("product", mProducts.get(position));
        intent.putExtra("position", position);
        intent.putExtra("src", "product");
        intent.putParcelableArrayListExtra("products", mProducts);


        startActivity(intent);
    }

    @Override
    public String toString() {
        String title = "Products";
        return title;
    }
}