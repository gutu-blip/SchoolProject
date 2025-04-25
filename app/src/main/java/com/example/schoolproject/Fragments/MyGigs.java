package com.example.schoolproject.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.schoolproject.Activites.GigListing;
import com.example.schoolproject.Adapters.ListAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Interface.DeleteListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;

import java.util.ArrayList;

public class MyGigs extends Fragment implements CallBackListener, DeleteListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    DatabaseReference mGigsRef;
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser = mUser.getUid();
    ArrayList<Listing> mListings;
    ListAdapter listAdapter;
    ProgressBar progressCircle;
    ConstraintLayout parentView;
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
        setListeners();

    }

    private void setListeners() {

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(getActivity(), GigListing.class);
                startActivity(intent);
            }
        });
    }

    private void fetchGigs() {

        mListings = new ArrayList<>();
        mGigsRef = database.getReference("Listings").child("Gigs");
        mGigsRef.keepSynced(true);
        mGigsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot collegeSnapshot :  snapshot.getChildren()){
                    for(DataSnapshot keySnapshot :  collegeSnapshot.getChildren()){

                        Listing listing = keySnapshot.getValue(Listing.class);

                        if(listing.getUserID().equals(currentUser)){
                            mListings.add(listing);
                        }
                    }
                }

                progressCircle.setVisibility(View.INVISIBLE);
                if(mListings.isEmpty()){
                    mPlaceHolder.setText("You have no gig listings yet");
                }else {
                    listAdapter = new ListAdapter(mListings,getActivity(),MyGigs.this,MyGigs.this);
                    recyclerView.setAdapter(listAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initWidgets(View view) {

        mPlaceHolder = view.findViewById(R.id.tv_placeholder);
        fabAdd = view.findViewById(R.id.fab_add);
        progressCircle = view.findViewById(R.id.progress_circle);
        parentView = view.findViewById(R.id.parent);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

    }

    @Override
    public void callBack(int position) {
        Listing listing = mListings.get(position);
        SavedServices.saved.trade = "Gigs";
        SavedServices.saved.dialogEvent(listing, getActivity());
    }

    public void onDeleteClick(int position) {
        Listing listing = mListings.get(position);

        final String selectedKey = listing.getPostKey();

        mGigsRef.child(listing.getLocation()).child(selectedKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(parentView, "Gig successfuly deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                mListings.remove(position);
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public String toString() {
        String title = "Gigs";
        return title;
    }

    @Override
    public void onDelete(int position) {

        Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.dialog_delete);
        dialog.show();

        TextView tvMessage = dialog.findViewById(R.id.tv_confirm);
        Button btnProceed = dialog.findViewById(R.id.btn_proceed);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        tvMessage.setText("Are you sure you want to delete this item?");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeleteClick(position);
                dialog.dismiss();
            }
        });
    }
}