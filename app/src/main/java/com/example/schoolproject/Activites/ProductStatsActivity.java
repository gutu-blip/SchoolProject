package com.example.schoolproject.Activites;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.schoolproject.Adapters.VPSliderAdapter;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.R;
import com.example.schoolproject.Utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

public class ProductStatsActivity extends AppCompatActivity {

    private FirebaseStorage mStorage;
    private DatabaseReference mListingsRef, mStatsRef, mLikedRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser;
    Listing listing;
    ViewPager2 viewPager2;
    ArrayList<String> album = new ArrayList<>();
    TextView mViews, mLikes, mPrice, mName, mCreated, mLeads, mDesc, mCondition,mConditionHeader,mPriceheader;
    int viewCount, likescount, leadsCount;
    ImageButton mDelete;
    Seller shop;
    private DeleteListener deleteListner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_stats);

        initWidgets();
        setItems();
        setViewPager();
        setListeners();
    }

    private void initWidgets() {
        mDesc = findViewById(R.id.tv_desc);
        mCondition = findViewById(R.id.tv_condition);
        mConditionHeader = findViewById(R.id.header_condition);
        mLeads = findViewById(R.id.tv_leads);
        mPriceheader = findViewById(R.id.header_price);
        mCreated = findViewById(R.id.tv_created);
        mLikes = findViewById(R.id.tv_likes);
        mPrice = findViewById(R.id.tv_price);
        mName = findViewById(R.id.tv_name);
        mViews = findViewById(R.id.tv_views);
        mDelete = findViewById(R.id.btn_delete);
        viewPager2 = findViewById(R.id.viewpager2);
    }

    public void onDeleteClick() {

        final String selectedKey = listing.getPostKey();

        if (listing.getUploadType().equals("singles")) {

            StorageReference imageRef = mStorage.getReferenceFromUrl(listing.getImageUrl());
            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mListingsRef.child(listing.getTrade()).child(listing.getLocation()).
                            child(selectedKey).removeValue();

                    Toast.makeText(ProductStatsActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();

                    if(shop!=null) {
                        Intent intent = new Intent(ProductStatsActivity.this, ProfileShopActivity.class);
                        intent.putExtra("shop", shop);
                        startActivity(intent);
                    }else{
                      if(deleteListner!=null){
                          deleteListner.onDeleteListener();
                      }
                    }
                    finish();
                }
            });
        } else {

            for (String image : album) {
                StorageReference imageRef = mStorage.getReferenceFromUrl(image);
                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mListingsRef.child(listing.getTrade()).child(listing.getLocation()).
                                child(selectedKey).removeValue();

                        Toast.makeText(ProductStatsActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProductStatsActivity.this, ProfileShopActivity.class);
                        intent.putExtra("shop", shop);
                        startActivity(intent);

                        finish();
                    }
                });
            }
        }
    }

    private void setItems() {

        currentUser = mUser.getUid();

        shop = getIntent().getParcelableExtra("shop");

        listing = getIntent().getParcelableExtra("listing");

        mListingsRef = database.getReference("Listings");
        mStorage = FirebaseStorage.getInstance();
        mStatsRef = database.getReference("Statistics");
        mLikedRef = database.getReference("Liked");

        if(listing.getTrade().equals("Products")){
            mPrice.setVisibility(View.VISIBLE);
            mPrice.setText(String.valueOf(listing.getPrice()));
            mConditionHeader.setText("Condition");
            mCondition.setText(listing.getCondition());

        }else{
            mPrice.setVisibility(View.INVISIBLE);
            mPriceheader.setVisibility(View.INVISIBLE);
            mConditionHeader.setText("Rates");
            mCondition.setText(listing.getRates());
        }

        mName.setText(listing.getName());
        mDesc.setText(listing.getDescription());
        mCreated.setText(calculateTimeAgo(listing.getTime()));

        mLikedRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {

                likescount = (int) snapshot.child(listing.getPostKey()).getChildrenCount();
                mLikes.setText(Utils.formatValue(likescount));

            }

            public void onCancelled(DatabaseError error) {
            }
        });

        mStatsRef.child("Leads").child(listing.getPostKey()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        leadsCount = (int) snapshot.getChildrenCount();
                        mLeads.setText(Utils.formatValue(leadsCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        mStatsRef.child("Views").child(listing.getPostKey()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        viewCount = (int) snapshot.getChildrenCount();
                        mViews.setText(Utils.formatValue(viewCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void setListeners() {

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(ProductStatsActivity.this);
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
                        onDeleteClick();
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private String calculateTimeAgo(String datePost) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
        try {
            long time = sdf.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    private void setViewPager() {
        CircleIndicator3 indicator = (CircleIndicator3) findViewById(R.id.indicator);

        if (listing.getUploadType().equals("album")) {
            indicator.setVisibility(View.VISIBLE);
            album = listing.getAlbum();

            VPSliderAdapter adapter = new VPSliderAdapter(ProductStatsActivity.this, album);
                viewPager2.setAdapter(adapter);

                indicator.setViewPager(viewPager2);
                adapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

        } else {
            indicator.setVisibility(View.INVISIBLE);
            ArrayList<String> image = new ArrayList<>();
            image.add(listing.getImageUrl());
            VPSliderAdapter adapter = new VPSliderAdapter(ProductStatsActivity.this,image);
            viewPager2.setAdapter(adapter);

            indicator.setViewPager(viewPager2);
            adapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
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

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof DeleteListener) {
            deleteListner = (DeleteListener) fragment;
        }
    }


    public interface DeleteListener {
        void onDeleteListener();
    }
}
