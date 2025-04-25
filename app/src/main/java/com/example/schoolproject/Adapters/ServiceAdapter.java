package com.example.schoolproject.Adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Interface.DeleteListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.ServiceItem;
import com.example.schoolproject.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int TYPE_IMAGE = 1;
    private static int TYPE_IMAGELESS = 0;
    public ArrayList<ServiceItem> mItems;
    DatabaseReference mSavedRef;
    Context context;
    public Boolean saveChecker = false;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    CallBackListener callBackListener;
    public String calling;
    private ArrayList<Listing> mImageServices;
    DeleteListener deleteListener;

    public ServiceAdapter(Context context, ArrayList<ServiceItem> mItems, CallBackListener callBackListener,
                          DeleteListener deleteListener) {
        this.mItems = mItems;
        this.callBackListener = callBackListener;
        this.context = context;
        this.deleteListener = deleteListener;
    }

    public ServiceAdapter(Context context, ArrayList<ServiceItem> mItems, ArrayList<Listing> mImageServices,
                          CallBackListener callBackListener) {

        this.context = context;
        this.mItems = mItems;
        this.mImageServices = mImageServices;
        this.callBackListener = callBackListener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if (viewType == TYPE_IMAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_service, parent, false);
            return new ImageViewHolder(view, callBackListener);
        } else if (viewType == TYPE_IMAGELESS) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_imageless_service, parent, false);
            return new ImagelessViewHolder(view, callBackListener,deleteListener);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (getItemViewType(position) == TYPE_IMAGE) {

            ArrayList<Listing> listings = (ArrayList<Listing>) mItems.get(position).getImageServices();
            Listing listing = (Listing) mItems.get(position).getObject();
            ((ImageViewHolder) holder).setImageService(listing,position);

            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUser = mUser.getUid();

            final String postKey = listing.getPostKey();
            final String trade = listing.getTrade();

            bookmarkChecker(listing.getPostKey(), trade, ((ImageViewHolder) holder).btnSave);

            ((ImageViewHolder) holder).btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (calling != null) {
                        if (calling.equals("SavedServices")) {
                            mItems.remove(position);
                            notifyDataSetChanged();
                        }
                    }
                    saveEvent(currentUser, postKey, listings, listing);
                }
            });

        } else if (getItemViewType(position) == TYPE_IMAGELESS) {

            ArrayList<Listing> listings = (ArrayList<Listing>) mItems.get(position).getImagelessServices();
            Listing listing = (Listing) mItems.get(position).getObject();
            ((ImagelessViewHolder) holder).setImagelessService(listing);

            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUser = mUser.getUid();

            final String postKey = listing.getPostKey();
            final String trade = listing.getTrade();

            bookmarkChecker(listing.getPostKey(), trade, ((ImagelessViewHolder) holder).btnSave);

            ((ImagelessViewHolder) holder).btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (calling != null) {
                        if (calling.equals("SavedServices")) {
                            mItems.remove(position);
                            notifyDataSetChanged();
                        }
                    }
                    saveEvent(currentUser, postKey, listings, listing);
                }
            });
        }
    }

    private void saveEvent(String currentUser, String postKey, ArrayList<Listing> savedListings, Listing listing) {
        saveChecker = true;

        mSavedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (saveChecker.equals(true)) {
                    if (snapshot.child(currentUser).child(postKey).hasChild(currentUser)) {
                        mSavedRef.child(currentUser).child(postKey).removeValue();
                        saveChecker = false;
                    } else {
                        mSavedRef.child(currentUser).child(postKey).child(currentUser).setValue(true);
                        mSavedRef.child(currentUser).child(postKey).child("postKey").setValue(postKey);

                        saveChecker = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size();
        } else {
            return 0;
        }
    }

    public class ImagelessViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mCategory, mDescription, mPrice, mCollege, mDate,mTitle;
        ImageButton btnSave;
        CallBackListener mCallBackListener;
        DeleteListener mDeleteListener;
        Listing mListing;

        public ImagelessViewHolder(@NonNull View itemView, CallBackListener callBackListener, DeleteListener deleteListener) {
            super(itemView);

            mCategory = itemView.findViewById(R.id.tv_category);
            mTitle = itemView.findViewById(R.id.tv_title);
            mDescription = itemView.findViewById(R.id.tv_desc);
            mPrice = itemView.findViewById(R.id.tv_amount);
            mDate = itemView.findViewById(R.id.tv_time);
            mCollege = itemView.findViewById(R.id.tv_college);
            btnSave = itemView.findViewById(R.id.btn_bookmark);
            mDeleteListener = deleteListener;
            mCallBackListener = callBackListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListing != null && calling != null) {
                mCallBackListener.callBack(getAdapterPosition());
            }
        }

        private void setImagelessService(Listing listing) {
            mListing = listing;

            ImageButton btnDelete = itemView.findViewById(R.id.btn_delete);

            if(calling.equals("MyServiceListings")){
                btnDelete.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.INVISIBLE);
            }else{
                btnDelete.setVisibility(View.INVISIBLE);
                btnSave.setVisibility(View.VISIBLE);
            }
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDeleteListener.onDelete(getAdapterPosition());
                }
            });

            mCategory.setText(listing.getCategory());
            mPrice.setText("Rates "+listing.getAmount());
            mDescription.setText(listing.getDescription());
            mDate.setText(calculateTimeAgo(listing.getTime()));
            mCollege.setText(listing.getLocation());
            mTitle.setText(listing.getName());
        }
    }

    private String calculateTimeAgo(String datePost) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
        try {
            long time = sdf.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTitle, mDescription, mPrice, mCollege, mDate,mCategory;
        ImageButton btnSave;
        CallBackListener mCallBackListener;
        Listing mListing;
        VPSliderAdapter adapter;

        public ImageViewHolder(@NonNull View itemView, CallBackListener callBackListener) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.tv_title);
            mCategory = itemView.findViewById(R.id.tv_category);
            mDescription = itemView.findViewById(R.id.tv_desc);
            mPrice = itemView.findViewById(R.id.tv_amount);
            mDate = itemView.findViewById(R.id.tv_time);
            mCollege = itemView.findViewById(R.id.tv_college);
            btnSave = itemView.findViewById(R.id.btn_bookmark);
            mCallBackListener = callBackListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListing != null && calling != null) {
                mCallBackListener.callBack(getAdapterPosition());
            }
        }

        private void setImageService(Listing listing,int savedPosition) {

            mListing = listing;
            mCategory.setText(listing.getCategory());
            mTitle.setText(listing.getName());
            mPrice.setText("Rates: "+listing.getRates());
            mDescription.setText(listing.getDescription());
            mDate.setText(calculateTimeAgo(listing.getTime()));
            mCollege.setText(listing.getLocation());
            ViewPager2 viewPager2 = itemView.findViewById(R.id.viewpager);
            CircleIndicator3 indicator = (CircleIndicator3) itemView.findViewById(R.id.indicator);

            if(calling.equals("MyServiceListings")){
               btnSave.setVisibility(View.INVISIBLE);
            }else{
                btnSave.setVisibility(View.VISIBLE);
            }

            if (listing.getUploadType() != null) {
                if (listing.getUploadType().equals("singles")) {

                    indicator.setVisibility(View.INVISIBLE);
                    ArrayList<String> image = new ArrayList<>();
                    image.add(listing.getImageUrl());

                    if (calling.equals("ServicesPosts") ) {
                        adapter = new VPSliderAdapter(context, image, listing,"ServicesPosts");
                        viewPager2.setAdapter(adapter);
                    } else if (calling.equals("SavedServices")) {
                        adapter = new VPSliderAdapter(context, image, savedPosition, mImageServices,listing);
                        viewPager2.setAdapter(adapter);
                    }else if(calling.equals("MyServiceListings")){
                        SharedPreferences.Editor editor = context.getSharedPreferences("POS_PREFS", MODE_PRIVATE).edit();
                        editor.putInt("pos",savedPosition );
                        editor.apply();

                        adapter = new VPSliderAdapter(context, image, listing,savedPosition,"MyServiceListings");
                        viewPager2.setAdapter(adapter);
                    }
                } else {
                    indicator.setVisibility(View.VISIBLE);

                    if (calling.equals("ServicesPosts")) {
                        adapter = new VPSliderAdapter(context, listing.getAlbum(), listing,"ServicesPosts");
                        viewPager2.setAdapter(adapter);
                    } else if (calling.equals("SavedServices")) {
                        adapter = new VPSliderAdapter(context, listing.getAlbum(), savedPosition, mImageServices,listing);
                        viewPager2.setAdapter(adapter);
                    }else if(calling.equals("MyServiceListings")){
                        SharedPreferences.Editor editor = context.getSharedPreferences("POS_PREFS", MODE_PRIVATE).edit();
                        editor.putInt("pos",savedPosition );
                        editor.apply();

                        adapter = new VPSliderAdapter(context, listing.getAlbum(), listing,savedPosition,"MyServiceListings");
                        viewPager2.setAdapter(adapter);
                    }
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
    }

    public void bookmarkChecker(final String postkey, final String trade, ImageButton btnSave) {

        mSavedRef = database.getReference("Saved").child(trade);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mSavedRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(uid).child(postkey).hasChild(uid)) {
                    btnSave.setImageResource(R.drawable.ic_bookmark_on);
                } else {
                    btnSave.setImageResource(R.drawable.ic_bookmark);
                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }
}

