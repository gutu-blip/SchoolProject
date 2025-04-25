package com.example.schoolproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.blurry.Blurry;

public class InterestsAdapter extends RecyclerView.Adapter<InterestsAdapter.NestedViewHolder> {

    public ArrayList<Listing> savedListings;
    Context context;
    public Boolean savedChecker = false;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    CallBackListener callBackListener;


    public InterestsAdapter(Context context, ArrayList<Listing> savedListings,
                            CallBackListener callBackListener) {
        this.savedListings = savedListings;
        this.context = context;
        this.callBackListener = callBackListener;
    }

    @NonNull
    @Override
    public NestedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_starred, parent, false);
            return new NestedViewHolder(view, callBackListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NestedViewHolder holder, int position) {

            Listing listing = savedListings.get(position);

            holder.getStarredListings(listing);

            DatabaseReference mStarredRef = database.getReference("Saved");

            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUser = mUser.getUid();

            final String postKey = listing.getPostKey();
            final String trade = listing.getTrade();

            holder.starredChecker(postKey,trade);
            holder.btnStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    savedChecker = true;

                    mStarredRef.child(trade).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (savedChecker.equals(true)) {
                                if (snapshot.child(currentUser).child(postKey).hasChild(currentUser)) {

                                    mStarredRef.child(trade).child(currentUser).child(postKey).removeValue();

                                    savedListings.remove(listing);
                                    notifyDataSetChanged();

                                    savedChecker = false;
                                } else {
                                    mStarredRef.child(trade).child(currentUser).child(postKey).child(currentUser).setValue(true);
                                    mStarredRef.child(trade).child(currentUser).child(postKey).child("postKey").setValue(postKey);

                                    savedChecker = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
    }

    @Override
    public int getItemCount() {
            return savedListings.size();
    }

    public class NestedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        DatabaseReference mStarredRef;
        ImageButton btnStar;
        CallBackListener mCallBackListener;


        public NestedViewHolder(@NonNull View itemView, CallBackListener callBackListener) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageview);
            mCallBackListener = callBackListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
                mCallBackListener.callBack(getAdapterPosition());
        }

        public void starredChecker(final String postkey,final String trade) {
            btnStar = itemView.findViewById(R.id.btn_star);

            mStarredRef = database.getReference("Starred").child("Mimics");
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            mStarredRef.child(trade).addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.child(uid).child(postkey).hasChild(uid)) {
                        btnStar.setImageResource(R.drawable.ic_star_filled);
                    } else {
                        btnStar.setImageResource(R.drawable.ic_star);
                    }
                }

                public void onCancelled(DatabaseError error) {
                }
            });
        }

        private void setBlurViews() {
            RelativeLayout root = itemView.findViewById(R.id.relativelayout);

            Blurry.with(context)
                    .radius(20)
                    .sampling(2)
                    .async()
                    .onto(root);

        }

        public void getStarredListings(Listing listing) {

            setBlurViews();
            Picasso.get()
                    .load(listing.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(imageView);
        }
    }
}