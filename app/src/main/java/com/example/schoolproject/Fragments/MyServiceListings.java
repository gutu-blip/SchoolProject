package com.example.schoolproject.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Activites.ProductStatsActivity;
import com.example.schoolproject.Activites.ServiceListing;
import com.example.schoolproject.Adapters.ServiceAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Interface.DeleteListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.ServiceItem;
import com.example.schoolproject.R;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;

public class MyServiceListings extends Fragment implements ProductStatsActivity.DeleteListener, CallBackListener, DeleteListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser = mUser.getUid();
    ArrayList<Listing> mListings;
    ProgressBar progressCircle;
    ArrayList<ServiceItem> items = new ArrayList<>();
    ServiceAdapter mAdapter;
    TextView mPlaceHolder;
    ConstraintLayout parentView;
    DatabaseReference mServiceRef;

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

        fetchServices();
        setListeners();
    }

/*
    private void fetchServices() {

        DatabaseReference mServicesRef = database.getReference("Listings").child("Services");
        mServicesRef.keepSynced(true);

        mServicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot collegeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot keySnapshot : collegeSnapshot.getChildren()) {

                        Listing listing = keySnapshot.child("Details").getValue(Listing.class);

                        if (listing != null) {
                            ArrayList<String> album = new ArrayList<>();
                            for (DataSnapshot imageKey : keySnapshot.child("Album").getChildren()) {
                                String image = String.valueOf(imageKey.child("imageUrl").getValue());
                                album.add(image);
                            }
                            ArrayList<String> specs = new ArrayList<>();

                            specs.add(listing.getCategory());
                            specs.add(listing.getLocation());

                            listing.setSpecs(specs);
                            if (listing.getImageUrl().equals("")) {

                                items.add(new ServiceItem(listing, 0));
                            } else if (!TextUtils.isEmpty(listing.getImageUrl())) {
                                listing.setAlbum(album);
                                items.add(new ServiceItem(listing, 1));

                            }
                        }
                    }
                }
                onLoadListings(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
*/

    //Original
    private void fetchServices() {

        mListings = new ArrayList<>();
        mServiceRef = database.getReference("Listings").child("Services");
        mServiceRef.keepSynced(true);

        mServiceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot collegeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot keySnapshot : collegeSnapshot.getChildren()) {

                        Listing listing = keySnapshot.child("Details").getValue(Listing.class);

                        if (listing.getUserID().equals(currentUser)) {

                            ArrayList<String> album = new ArrayList<>();
                            for (DataSnapshot imageKey : keySnapshot.child("Album").getChildren()) {
                                String image = String.valueOf(imageKey.child("imageUrl").getValue());
                                album.add(image);
                            }

                            if (listing.getImageUrl().equals("")) {
                                items.add(new ServiceItem(listing, 0));
                            } else if (!TextUtils.isEmpty(listing.getImageUrl())) {
                                listing.setAlbum(album);
                                items.add(new ServiceItem(listing, 1));
                            }
                        }
                    }
                }
                onLoadService(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setListeners() {

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ServiceListing.class);
                intent.putExtra("listingSource", "individual");
                startActivity(intent);
            }
        });
    }

    private void onLoadService(ArrayList<ServiceItem> list) {

        progressCircle.setVisibility(View.GONE);
        if (items.isEmpty()) {
            mPlaceHolder.setText("You have no service listings yet");
        } else {

            mAdapter = new ServiceAdapter(getActivity(), list, MyServiceListings.this,MyServiceListings.this);
            mAdapter.calling = "MyServiceListings";
            SlideInRightAnimationAdapter alphaInAnimationAdapter = new SlideInRightAnimationAdapter(mAdapter);
            alphaInAnimationAdapter.setDuration(700);
            alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
            alphaInAnimationAdapter.setFirstOnly(false);

            recyclerView.setAdapter(alphaInAnimationAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initWidgets(View view) {

        parentView = view.findViewById(R.id.parent);
        mPlaceHolder = view.findViewById(R.id.tv_placeholder);
        fabAdd = view.findViewById(R.id.fab_add);
        progressCircle = view.findViewById(R.id.progress_circle);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public String toString() {
        String title = "Services";
        return title;
    }

    @Override
    public void onDeleteListener() {
        SharedPreferences prefs = getActivity().getSharedPreferences("POS_PREFS", MODE_PRIVATE);
        int position = prefs.getInt("pos", 0);

        items.remove(position);
        mAdapter.notifyDataSetChanged();
    }
    public void onDeleteClick(int position) {

        Listing listing = (Listing) items.get(position).getObject();

        final String selectedKey = listing.getPostKey();

        mServiceRef.child(listing.getLocation()).child(selectedKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(parentView, "Gig successfuly deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                items.remove(position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void callBack(int position) {
        Listing listing = (Listing) items.get(position).getObject();

        if (!listing.getImageUrl().isEmpty()) {
        } else {
            SavedServices.saved.trade = "Service";
            SavedServices.saved.dialogEvent(listing, getActivity());
        }
    }

    @Override
    public void onDelete(int position) {
        onDeleteClick(position);
    }
}