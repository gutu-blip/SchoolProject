package com.example.schoolproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
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

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

public class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.ViewHolder> {

    Context context;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    public Boolean likeChecker = false;
    public Boolean starredChecker = false;
    ArrayList<Listing> mListings;
    CallBackListener callBackListener;

    public ListingsAdapter(Context context, ArrayList<Listing> mListings) {
        this.context = context;
        this.mListings = mListings;
    }

    public ListingsAdapter(Context context, ArrayList<Listing> mListings, CallBackListener callBackListener) {
        this.context = context;
        this.mListings = mListings;
        this.callBackListener = callBackListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (callBackListener == null) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ViewHolder(view, callBackListener);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_catalogue, parent, false);
            return new ViewHolder(view, callBackListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Listing listing = mListings.get(position);

        if (callBackListener != null) {

            holder.getListings(listing);

        } else {
            if (listing != null) {
                holder.getData(listing);
                save(holder, position);

                DatabaseReference likedRef = database.getReference("Liked");

                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                String currentUser = mUser.getUid();

                final String postKey = listing.getPostKey();
                holder.likesChecker(postKey);

                holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        likeChecker = true;

                        likedRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (likeChecker.equals(true)) {
                                    if (snapshot.child(postKey).hasChild(currentUser)) {

                                        likedRef.child(postKey).child(currentUser).removeValue();
                                        likeChecker = false;
                                    } else {
                                        likedRef.child(postKey).child(currentUser).child(currentUser).setValue(true);
                                        likedRef.child(postKey).child(currentUser).child("postKey").setValue(postKey);

                                        likeChecker = false;
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
        }
    }

    private void save(ViewHolder holder, int position) {

        Listing listing = mListings.get(position);

        final String trade = listing.getTrade();

        DatabaseReference mSavedRef = database.getReference("Saved").child(trade);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = mUser.getUid();

        final String postKey = listing.getPostKey();

        holder.bookmarkChecker(postKey, trade);

        holder.bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                starredChecker = true;

                mSavedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (starredChecker.equals(true)) {
                            if (snapshot.child(currentUser).child(postKey).hasChild(currentUser)) {

                                mSavedRef.child(currentUser).child(postKey).removeValue();

                                starredChecker = false;
                            } else {
                                mSavedRef.child(currentUser).child(postKey).child(currentUser).setValue(true);
                                mSavedRef.child(currentUser).child(postKey).child("postKey").setValue(postKey);

                                starredChecker = false;
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
        return mListings.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name, likes, price;
        ImageButton bookmarkBtn, likeBtn;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference savedRef, likedref;
        int likescount;
        CardView cardView;
        ImageView imageView;
        CallBackListener mCallBackListener;

        public ViewHolder(@NonNull View itemView, CallBackListener callBackListener) {
            super(itemView);

            this.mCallBackListener = callBackListener;
            name = itemView.findViewById(R.id.tv_name);
            price = itemView.findViewById(R.id.tv_price);
            likes = itemView.findViewById(R.id.tv_likes);
            cardView = itemView.findViewById(R.id.cv_imageview);
            imageView = itemView.findViewById(R.id.imageview);
            bookmarkBtn = itemView.findViewById(R.id.btn_bookmark);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (callBackListener != null) {
                mCallBackListener.callBack(getAdapterPosition());
            }
        }

        public void getData(final Listing listing) {
            name.setText(listing.getName());
            price.setText(String.valueOf(listing.getPrice()));

            ViewPager2 viewPager2 = itemView.findViewById(R.id.viewPager2);
            CircleIndicator3 indicator = (CircleIndicator3) itemView.findViewById(R.id.indicator);

            if (listing.getUploadType() != null) {
                if (listing.getUploadType().equals("singles")) {

                    indicator.setVisibility(View.INVISIBLE);
                    ArrayList<String> image = new ArrayList<>();
                    image.add(listing.getImageUrl());

                    viewPager2.setAdapter(new VPSliderAdapter(context, image, listing, "ServicesPosts"));

                } else {
                    indicator.setVisibility(View.VISIBLE);

                    VPSliderAdapter adapter = new VPSliderAdapter(context, listing.getAlbum(), listing, "ServicesPosts");
                    viewPager2.setAdapter(adapter);

                    indicator.setViewPager(viewPager2);
                    adapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
                }
            }
            viewPager2.setClipToPadding(false);
            viewPager2.setClipChildren(false);
            viewPager2.setOffscreenPageLimit(3);
            viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            compositePageTransformer.addTransformer(new MarginPageTransformer(40));
            compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                    float r = 1 - Math.abs(position);
                    page.setScaleY(0.85f + r * 0.15f);
                }
            });
            viewPager2.setPageTransformer(compositePageTransformer);

        }

        public void getListings(Listing listing) {
            Glide.with(context)
                    .load(listing.getImageUrl())
                    .centerCrop()
                    .fitCenter()
                    .placeholder(R.drawable.image_placeholder)
                    .into(imageView);

        }

        public void bookmarkChecker(final String postkey, final String trade) {

            savedRef = database.getReference("Saved");
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            savedRef.child(trade).addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.child(uid).child(postkey).hasChild(uid)) {
                        bookmarkBtn.setImageResource(R.drawable.ic_bookmark_on);
                    } else {
                        bookmarkBtn.setImageResource(R.drawable.ic_bookmark);
                    }
                }

                public void onCancelled(DatabaseError error) {
                }
            });
        }


        public void likesChecker(final String postKey) {
            likeBtn = itemView.findViewById(R.id.btn_like);

            likedref = database.getReference("Liked");

            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            likedref.addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {

                    if (snapshot.child(postKey).hasChild(uid)) {

                        likeBtn.setImageResource(R.drawable.ic_heart_filled);
                        likescount = (int) snapshot.child(postKey).getChildrenCount();
                        likes.setText(Utils.formatValue(likescount));
                    } else {
                        likeBtn.setImageResource(R.drawable.ic_heart_off);
                        likescount = (int) snapshot.child(postKey).getChildrenCount();
                        likes.setText(Utils.formatValue(likescount));
                    }
                }

                public void onCancelled(DatabaseError error) {
                }
            });
        }
    }
}
