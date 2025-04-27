package com.example.schoolproject.Fragments;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Activites.SavedMerger;
import com.example.schoolproject.Adapters.ServiceAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.Models.ServiceItem;
import com.example.schoolproject.R;
import com.example.schoolproject.Utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;


public class SavedServices extends Fragment implements CallBackListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser = mUser.getUid();
    DatabaseReference mSavedRef;
    ArrayList<Listing> mServices, mImageServices;
    double average = 0.0;
    ProgressBar progressCircle;
    ServiceAdapter mAdapter;
    ArrayList<ServiceItem> items;
    public ConstraintLayout parentView;
    TextView mPlaceHolder;
    public static SavedServices saved = new SavedServices();
    public String trade = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mylistings, container, false);

        initWidgets(view);
        return view;
    }

    private void initWidgets(View view) {

        mPlaceHolder = view.findViewById(R.id.tv_placeholder);
        parentView = view.findViewById(R.id.parent);
        fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setVisibility(View.GONE);
        progressCircle = view.findViewById(R.id.progress_circle);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fetchServices();

    }

    private void fetchServices() {
        items = new ArrayList<>();
        mServices = new ArrayList<>();
        mImageServices = new ArrayList<>();
        mSavedRef = database.getReference("Listings").child("Services");
        mSavedRef.keepSynced(true);

        //SavedKeys
        ArrayList<String> productKeys = new ArrayList<>();
        database.getReference("Saved").child("Services").child(currentUser).addValueEventListener(new ValueEventListener() {
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
                ServiceItem serviceItem = new ServiceItem();

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

                                if (listing.getImageUrl().equals("")) {
                                    items.add(new ServiceItem(listing, 0));
                                    mServices.add(listing);
                                } else if (!TextUtils.isEmpty(listing.getImageUrl())) {
                                    listing.setAlbum(album);
                                    items.add(new ServiceItem(listing, 1));
                                    mServices.add(listing);
                                    mImageServices.add(listing);
                                }
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

    private void onLoadService(ArrayList<ServiceItem> list) {

        progressCircle.setVisibility(View.INVISIBLE);
        if(list.isEmpty()){
            mPlaceHolder.setText("You have no saved services yet");
        }else {

            mAdapter = new ServiceAdapter(getActivity(), list, mImageServices,SavedServices.this);
            mAdapter.calling = "SavedServices";

            SlideInRightAnimationAdapter alphaInAnimationAdapter = new SlideInRightAnimationAdapter(mAdapter);
            alphaInAnimationAdapter.setDuration(700);
            alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
            alphaInAnimationAdapter.setFirstOnly(false);

            recyclerView.setAdapter(alphaInAnimationAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void getRating(Listing listing, RatingBar mRatingBar,TextView mRating) {

        try {
            final DatabaseReference dbRef = database.getReference("Profiles").child(listing.getUserID())
                    .child("Shops").child(listing.getSellerKey()).child("Ratings");

            final DatabaseReference ratingRef = database.getReference("Profiles").child(listing.getUserID())
                    .child("Shops").child(listing.getSellerKey()).child("Profile").child("rating");

            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double total = 0.0;
                    double count = 0.0;

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        double rating = Double.parseDouble(ds.child("rating").getValue().toString());
                        total = total + rating;
                        count = count + 1;
                        average = total / count;
                    }

                    mRatingBar.setRating((float) average);
                    DecimalFormat percentageFormat = new DecimalFormat("0.0");
                    String finalAverage = percentageFormat.format(average);
                    ratingRef.setValue(finalAverage);
                    mRating.setText(finalAverage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });
        } catch (Exception e) {
        }
    }


    public void dialogEvent(Listing listing,Context context){

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);
        DatabaseReference mLeadsRef = database.getReference().child("Statistics").child("Leads");

        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.dialog_imageless_service);

        TextView mCollege = dialog.findViewById(R.id.tv_college);
        TextView mShopName = dialog.findViewById(R.id.shop_name);
        TextView mTitle = dialog.findViewById(R.id.tv_name);
        TextView mCategory = dialog.findViewById(R.id.tv_category);
        TextView mRating = dialog.findViewById(R.id.tv_rating);
        RatingBar mRatingBar = dialog.findViewById(R.id.rating_bar);
        TextView mTime = dialog.findViewById(R.id.tv_posted);
        TextView mPrice = dialog.findViewById(R.id.tv_price);
        TextView mDesc = dialog.findViewById(R.id.tv_desc);
        TextView mHostel = dialog.findViewById(R.id.tv_hostel);
        TextView mHostelHeader = dialog.findViewById(R.id.header_hostel);
        TextView mRoom = dialog.findViewById(R.id.tv_room);
        TextView mRoomHeader = dialog.findViewById(R.id.header_room);

        Button btnChat = dialog.findViewById(R.id.btn_chat);
        ShimmerFrameLayout shimmerLogo = dialog.findViewById(R.id.shimmer_profile_photo);
        CircleImageView shopLogo = dialog.findViewById(R.id.shop_logo);

        dialog.show();

        String listingName = listing.getName();
        String offer = listing.getAmount();
        String desc = listing.getDescription();
        String category = listing.getCategory();
        String room = listing.getRoom();
        String hostel = listing.getHostel();
        String listingSource = listing.getListingSource();

        mTitle.setText(listingName);
        mDesc.setText(desc);
        if(trade.equals("Gigs")){
            mPrice.setText("Compensation: "+offer);
        }else{
            mPrice.setText("Rates: "+offer);
        }
        mTime.setText(Utils.utils.calculateTimeAgo(listing.getTime()));
        mCategory.setText(category);

       if(!TextUtils.isEmpty(room)){
           mRoomHeader.setVisibility(View.VISIBLE);
           mRoom.setVisibility(View.VISIBLE);
           mRoom.setText(listing.getRoom());
       }
       if(!TextUtils.isEmpty(hostel)){
           mHostelHeader.setVisibility(View.VISIBLE);
           mHostel.setVisibility(View.VISIBLE);
           mHostel.setText(listing.getHostel());
        }

       //listingSource,room,hostel
        if(listingSource!=null){
            if (listingSource.equals("individual")) {
                mRating.setVisibility(View.INVISIBLE);
                mRatingBar.setVisibility(View.INVISIBLE);
            } else {
                getRating(listing, mRatingBar, mRating);
            }

            if (listingSource.equals("shop")) {
                database.getReference("Profiles").child(listing.getUserID()).child("Shops").child(listing.getSellerKey())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                Seller seller = snapshot.child("Profile").getValue(Seller.class);

                                if (seller != null) {
                                    mCollege.setText(seller.getCollege());
                                    mShopName.setText(seller.getName());

                                    shimmerLogo.setVisibility(View.INVISIBLE);
                                    Picasso.get()
                                            .load(seller.getProfilePhoto())
                                            .into(shopLogo);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } else {
                mCollege.setText(listing.getLocation());
                mShopName.setText(listing.getSellerName());
                shimmerLogo.setVisibility(View.INVISIBLE);

                Picasso.get()
                        .load(listing.getSellerPhoto())
                        .into(shopLogo);
            }
        }else{
            mRating.setVisibility(View.INVISIBLE);
            mRatingBar.setVisibility(View.INVISIBLE);

            mCollege.setText(listing.getLocation());
            mShopName.setText(listing.getSellerName());
            shimmerLogo.setVisibility(View.INVISIBLE);

            Picasso.get()
                    .load(listing.getSellerPhoto())
                    .into(shopLogo);
        }

        btnChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mLeadsRef.child(listing.getPostKey()).child(currentUser).child("name")
                        .setValue(signInAccount.getDisplayName());
                mLeadsRef.child(listing.getPostKey()).child(currentUser).child("userId").setValue(currentUser);

                String message = "Say something";
                String cleanNumber = cleanPhoneNumber(listing.getSellerPhone());

                sendWhatsAppMessage(context, cleanNumber, message);
            }
        });
    }
    private String cleanPhoneNumber(String phoneNumber) {
        return phoneNumber.replaceFirst("^\\+", "");
    }
    private void sendWhatsAppMessage(Context context, String phoneNumber, String message) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain"); // <-- No image, only text now

        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra("jid", phoneNumber + "@s.whatsapp.net"); // WhatsApp JID format

        try {
            // Try WhatsApp Business first
            intent.setPackage("com.whatsapp.w4b");
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent);
            } else {
                // Fallback to normal WhatsApp
                intent.setPackage("com.whatsapp");
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Neither WhatsApp nor WhatsApp Business is installed.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error opening WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadAndSendWhatsAppImage(Context context, String phoneNumber, String message, String imageUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                InputStream inputStream = url.openStream();
                File file = new File(context.getCacheDir(), "shared_image.jpg");

                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

                // Post to main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    sendWhatsAppMessageWithImage(context, phoneNumber, message, uri);
                });

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void sendWhatsAppMessageWithImage(Context context, String phoneNumber, String message, Uri imageUri) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra("jid", phoneNumber + "@s.whatsapp.net");

        try {
            // Try WhatsApp Business first
            intent.setPackage("com.whatsapp.w4b");
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent);
            } else {
                intent.setPackage("com.whatsapp");
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Neither WhatsApp nor WhatsApp Business is installed.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error opening WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callBack(int position) {
        Listing listing = (Listing) items.get(position).getObject();

        if (!listing.getImageUrl().isEmpty()) {
            Toast.makeText(getActivity(), "SavedServices", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), SavedMerger.class);

            intent.putExtra("service", listing);
            intent.putExtra("src", "service");
            intent.putExtra("position", position);
            intent.putParcelableArrayListExtra("services", mServices);
            startActivity(intent);
        } else {

            trade = "Service";
            dialogEvent(listing,getActivity());
        }
    }

    @Override
    public String toString() {
        String title = "Services";
        return title;
    }
}