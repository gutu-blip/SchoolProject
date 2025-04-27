package com.example.schoolproject.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
//import androidx.renderscript.Allocation;
//import androidx.renderscript.Element;
//import androidx.renderscript.RenderScript;
//import androidx.renderscript.ScriptIntrinsicBlur;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.schoolproject.Utils.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Activites.FragmentMerger;
import com.example.schoolproject.Adapters.ListingsAdapter;
import com.example.schoolproject.Adapters.ServiceAdapter;
import com.example.schoolproject.Adapters.VPSliderAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Interface.DeleteListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.Models.ServiceItem;
import com.example.schoolproject.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;
import me.relex.circleindicator.CircleIndicator3;

public class ProductDetailsFragment extends Fragment implements CallBackListener, DeleteListener{

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser = mUser.getUid();
    private ArrayList<String> album = new ArrayList<>();
    public List<Uri> uriList;
    Listing listing;
    String shopKey, shopUid, trade;
    ProgressBar progressCircle;
    CoordinatorLayout parentView;
    private TextView mPrice, mDesc, mName, shopName, mPosted, mSimilar, mCondition, mLikes,
            mConditionHeader, mLocation, mHostel, mRoom, mRating;
    RatingBar mRatingBar;
    RecyclerView recyclerView;
    Button btnChat, btnPatron;
    int likescount;
    CircleImageView shopLogo;
    com.getbase.floatingactionbutton.FloatingActionButton fabSlide;
    Dialog dialog;
    ShimmerFrameLayout shimmerProfilePhoto;
    Boolean bookmarkChecker = false;
    public Boolean followedChecker = false;
    String image = "";
    FrameLayout blurShare, blurBookmark, blurLikes, blurHeart;
    DatabaseReference mShopRef, followedRef, mSavedRef, mLikedRef, mStatsRef, mListingRef;
    private ArrayList<Listing> productList;
    private ArrayList<ServiceItem> serviceList;
    ImageButton btnBookmark, btnHeart, btnShare;
    Boolean likeChecker = false;
    String listingSource;
    View view;
    CardView cardView;
    ListingsAdapter listingAdapter;
    ServiceAdapter serviceAdapter;
    double average = 0.0;
    private static final int REQUEST_PERMISSIONS_CODE = 100;
    private Bitmap pendingResource;

    public ProductDetailsFragment(Listing listing, String shopKey, String shopUid, String trade) {
        this.listing = listing;
        this.shopUid = shopUid;
        this.shopKey = shopKey;
        this.trade = trade;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (view == null) {
            if (trade.equals("Products")) {
                view = inflater.inflate(R.layout.fragment_product_details, container, false);
                initWidgets(view);
                return view;
            } else if (trade.equals("Services")) {
                view = inflater.inflate(R.layout.fragment_service_details, container, false);
                initWidgets(view);
                return view;
            }
        }

        return null;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setItems();
//      checkPaymentStatus();
        setListeners();
        fetchSimilarProduct();
        bookmarkEvent();
        setViewPager();
        if (listingSource.equals("shop")) {
            shopInfo();
            followEvent();
        }
        if (trade.equals("Products")) {
            likeEvent();
        }
      tapTarget();
    }


    private void tapTarget() {
        SharedPreferences.Editor write_prefs = getActivity().getSharedPreferences("SLIDE_PREFS", MODE_PRIVATE).edit();
        SharedPreferences read_prefs = getActivity().getSharedPreferences("SLIDE_PREFS", MODE_PRIVATE);

        Boolean firstStart = read_prefs.getBoolean("firstStart", true);
        if (firstStart && listing.getListingSource().equals("shop")) {
            if (!shopUid.equals(currentUser)) {

                fabSlide.setVisibility(View.VISIBLE);
                TapTargetView.showFor(getActivity(),
                        TapTarget.forView(fabSlide, "Slide to the Left", "Slide the screen to view the shop and its catalogue")
                                .outerCircleColor(R.color.teal_200)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(25)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(16)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.black)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(60),
                        new TapTargetView.Listener() {

                            @Override
                            public void onTargetClick(TapTargetView view) {
                                super.onTargetClick(view);

                                write_prefs.putBoolean("firstStart", false);
                                write_prefs.apply();

                                fabSlide.setVisibility(View.GONE);

                            }
                        });
            }
        }
    }

    public void getRating(Listing listing) {
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
            Toast.makeText(getActivity(), "" + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void followEvent() {

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = mUser.getUid();

        final String shopKey = listing.getSellerKey();

        followChecker(shopKey);

        if (shopKey != null) {

           btnPatron.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
                    String name = signInAccount.getDisplayName();
                    String photoUrl = signInAccount.getPhotoUrl().toString();
                    followedChecker = true;

                    followedRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (followedChecker.equals(true)) {
                                if (snapshot.child(shopKey).hasChild(currentUser)) {

                                    followedRef.child(shopKey).child(currentUser).removeValue();
                                    followedChecker = false;
                                } else {

                                    followedRef.child(shopKey).child(currentUser).child("follower").setValue(currentUser);
                                    followedRef.child(shopKey).child(currentUser).child("followed").setValue(shopKey);
                                    followedRef.child(shopKey).child(currentUser).child("profilephoto").setValue(photoUrl);
                                    followedRef.child(shopKey).child(currentUser).child("name").setValue(name);

                                    followedChecker = false;
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

    public void followChecker(final String shopKey) {

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        followedRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.child(shopKey).hasChild(uid)) {
                    btnPatron.setText("Patron");
                } else {
                    btnPatron.setText("Become a Patron");
                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void shopInfo() {

        mShopRef = database.getReference("Profiles").child(listing.getUserID()).child("Shops")
                .child(listing.getSellerKey());
        mShopRef.keepSynced(true);

        mShopRef.child("Profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Seller seller = snapshot.getValue(Seller.class);

                if (seller != null) {
                    shopName.setText(seller.getName());
                    mLocation.setText(seller.getCollege());

                    Picasso.get()
                            .load(seller.getProfilePhoto())
                            .into(shopLogo);

                    shimmerProfilePhoto.setVisibility(View.INVISIBLE);

                    if (!TextUtils.isEmpty(seller.getRoom())) {
                        getActivity().findViewById(R.id.header_room).setVisibility(View.VISIBLE);
                        mRoom.setVisibility(View.VISIBLE);
                        mRoom.setText(seller.getRoom());
                    }
                    if (!TextUtils.isEmpty(seller.getHostel())) {
                        getActivity().findViewById(R.id.header_hostel).setVisibility(View.VISIBLE);
                        mHostel.setVisibility(View.VISIBLE);
                        mHostel.setText(seller.getHostel());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void likeEvent() {

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = mUser.getUid();

        final String postKey = listing.getPostKey();

        likesChecker(postKey);
        btnHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                likeChecker = true;

                mLikedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (likeChecker.equals(true)) {
                            if (snapshot.child(postKey).hasChild(currentUser)) {

                                mLikedRef.child(postKey).child(currentUser).removeValue();
                                likeChecker = false;
                            } else {
                                mLikedRef.child(postKey).child(currentUser).child(currentUser).setValue(true);
                                mLikedRef.child(postKey).child(currentUser).child("postKey").setValue(postKey);

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

    public void likesChecker(final String postKey) {
        btnHeart = getActivity().findViewById(R.id.btn_heart);

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mLikedRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.child(postKey).hasChild(uid)) {

                    btnHeart.setImageResource(R.drawable.ic_heart_filled);
                    likescount = (int) snapshot.child(postKey).getChildrenCount();
                    mLikes.setText(Utils.formatValue(likescount));

                } else {
                    btnHeart.setImageResource(R.drawable.ic_heart_off_detail);
                    likescount = (int) snapshot.child(postKey).getChildrenCount();
                    mLikes.setText(Utils.formatValue(likescount));

                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void bookmarkEvent() {

        String currentUser = mUser.getUid();

        final String postKey = listing.getPostKey();
        final String trade = listing.getTrade();

        bookmarkChecker(postKey, trade);

        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEvent(postKey, currentUser);
            }
        });
    }

    private void saveEvent(String postKey, String currentUser) {
        bookmarkChecker = true;

        mSavedRef.child(trade).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (bookmarkChecker.equals(true)) {
                    if (snapshot.child(currentUser).child(postKey).hasChild(currentUser)) {

                        mSavedRef.child(trade).child(currentUser).child(postKey).removeValue();

                        bookmarkChecker = false;
                    } else {
                        mSavedRef.child(trade).child(currentUser).child(postKey).child(currentUser).setValue(true);
                        mSavedRef.child(trade).child(currentUser).child(postKey).child("postKey").setValue(postKey);

                        bookmarkChecker = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void bookmarkChecker(final String postkey, final String trade) {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnBookmark = getActivity().findViewById(R.id.btn_bookmark);
        mSavedRef.child(trade).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(uid).child(postkey).hasChild(uid)) {
                    btnBookmark.setImageResource(R.drawable.ic_bookmark_on);
                } else {
                    btnBookmark.setImageResource(R.drawable.ic_bookmark);
                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });

    }

    private void fetchSimilarProduct() {

        // MarketPlace -> Trade -> College -> postKey

        mListingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                serviceList.clear();

                for (DataSnapshot tradeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot collegeSnapshot : tradeSnapshot.getChildren()) {
                        for (DataSnapshot keySnapshot : collegeSnapshot.getChildren()) {

                            Listing mListing = keySnapshot.child("Details").getValue(Listing.class);

                            if (mListing != null) {
                                if (mListing.getName().contains(listing.getName()) && !mListing.getPostKey()
                                        .equals(listing.getPostKey())) {

                                    ArrayList<String> album = new ArrayList<>();
                                    for (DataSnapshot imageKey : keySnapshot.child("Album").getChildren()) {
                                        String image = imageKey.child("imageUrl").getValue(String.class);
                                        album.add(image);
                                    }

                                    ArrayList<String> specs = new ArrayList<>();

                                    specs.add(listing.getCategory());
                                    specs.add(listing.getLocation());

                                    mListing.setSpecs(specs);

                                    if (trade.equals("Products")) {
                                        mListing.setAlbum(album);
                                        productList.add(mListing);
                                    } else {

                                        if (mListing.getImageUrl().equals("")) {
                                            serviceList.add(new ServiceItem(mListing, 0));
                                        } else if (!TextUtils.isEmpty(mListing.getImageUrl())) {
                                            mListing.setAlbum(album);
                                            serviceList.add(new ServiceItem(mListing, 1));

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                onLoadCatalogue(productList, serviceList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onLoadCatalogue(ArrayList<Listing> productList, ArrayList<ServiceItem> serviceList) {

        if (trade.equals("Products")) {
            if (productList.isEmpty()) {
                getActivity().findViewById(R.id.tv_placeholder).setVisibility(View.VISIBLE);
            } else {
                getActivity().findViewById(R.id.tv_placeholder).setVisibility(View.INVISIBLE);

                listingAdapter = new ListingsAdapter(getActivity(), productList);
                SlideInRightAnimationAdapter alphaInAnimationAdapter = new SlideInRightAnimationAdapter(listingAdapter);
                alphaInAnimationAdapter.setDuration(550);
                alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
                alphaInAnimationAdapter.setFirstOnly(false);

                recyclerView.setAdapter(alphaInAnimationAdapter);
                listingAdapter.notifyDataSetChanged();
            }
        } else {
            if (serviceList.isEmpty()) {
                getActivity().findViewById(R.id.tv_placeholder).setVisibility(View.VISIBLE);
            } else {

                serviceAdapter = new ServiceAdapter(getActivity(), serviceList, ProductDetailsFragment.this,ProductDetailsFragment.this);
                serviceAdapter.calling = "ServicesPosts";
                getActivity().findViewById(R.id.tv_placeholder).setVisibility(View.INVISIBLE);

                SlideInRightAnimationAdapter alphaInAnimationAdapter = new SlideInRightAnimationAdapter(serviceAdapter);
                alphaInAnimationAdapter.setDuration(550);
                alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
                alphaInAnimationAdapter.setFirstOnly(false);

                recyclerView.setAdapter(alphaInAnimationAdapter);
                serviceAdapter.notifyDataSetChanged();
            }
        }
        progressCircle.setVisibility(View.INVISIBLE);

    }

    private void setViewPager() {
        ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewpager2);
        CircleIndicator3 indicator = (CircleIndicator3) getActivity().findViewById(R.id.indicator);
        album = listing.getAlbum();

        if (listing.getUploadType().equals("album")) {

            VPSliderAdapter adapter = new VPSliderAdapter(getActivity(), album);
            viewPager2.setAdapter(adapter);

            indicator.setViewPager(viewPager2);
            adapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
        } else {
            ArrayList<String> image = new ArrayList<>();
            image.add(listing.getImageUrl());

            viewPager2.setAdapter(new VPSliderAdapter(getActivity(), image));
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

    private void initWidgets(View view) {

        fabSlide = view.findViewById(R.id.fab_slide);
        cardView = view.findViewById(R.id.cv_details);
        parentView = view.findViewById(R.id.coordinator);
        shimmerProfilePhoto = view.findViewById(R.id.shimmer_profile_photo);

        mRating = view.findViewById(R.id.tv_rating);
        mRatingBar = view.findViewById(R.id.rating_bar);
        mHostel = view.findViewById(R.id.tv_hostel);
        mRoom = view.findViewById(R.id.tv_room);
        mLocation = view.findViewById(R.id.tv_location);
        mLikes = view.findViewById(R.id.tv_likes);
        mCondition = view.findViewById(R.id.tv_condition);
        mConditionHeader = view.findViewById(R.id.header_condition);
        mSimilar = view.findViewById(R.id.header_similar);
        mPosted = view.findViewById(R.id.tv_posted);
        btnPatron = view.findViewById(R.id.btn_patron);
        shopLogo = view.findViewById(R.id.shop_logo);
        shopName = view.findViewById(R.id.shop_name);
        mPrice = view.findViewById(R.id.tv_price);
        mDesc = view.findViewById(R.id.tv_desc);
        btnChat = view.findViewById(R.id.btn_chat);
        mName = view.findViewById(R.id.tv_name);
        btnShare = view.findViewById(R.id.btn_share);
//        Picasso.get().load(Uri.parse(listing.getImageUrl())).into(mImageView);

        dialog = new Dialog(getActivity());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.dialog_list);

        dialog.setCanceledOnTouchOutside(false);
        progressCircle = dialog.findViewById(R.id.progress_circle);

        recyclerView = view.findViewById(R.id.recyclerview);
        if (listing.getTrade().equals("Products")) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        }
    }


    private void setItems() {

        if (currentUser.equals(shopUid)) {
            btnPatron.setVisibility(View.GONE);
            btnChat.setVisibility(View.GONE);
        } else {
            btnPatron.setVisibility(View.VISIBLE);
            btnChat.setVisibility(View.VISIBLE);
        }

        uriList = new ArrayList<>();
        productList = new ArrayList<>();
        serviceList = new ArrayList<>();

        listingSource = listing.getListingSource();

        image = listing.getImageUrl();

        mListingRef = database.getReference("Listings");
        mListingRef.keepSynced(true);
        followedRef = database.getReference("Followed").child("Shops");
        followedRef.keepSynced(true);

        mSavedRef = database.getReference("Saved");
        mSavedRef.keepSynced(true);
        mStatsRef = database.getReference("Statistics");
        mStatsRef.keepSynced(true);
        mLikedRef = database.getReference("Liked");
        mLikedRef.keepSynced(true);
        String name = listing.getName();
        int price = listing.getPrice();
        String desc = listing.getDescription();

        mSimilar.setText("Similar");
        mPosted.setText(calculateTimeAgo(listing.getTime()));
        mName.setText(name);
        mDesc.setText(desc);

        if (listing.getTrade().equals("Services")) {
            mPrice.setText("Rates: "+listing.getRates());
        } else if (listing.getTrade().equals("Products")) {
            mConditionHeader.setText("Condition");
            mCondition.setText(listing.getCondition());
            mPrice.setText(Integer.toString(price));
        }

        if (listing.getListingSource().equals("individual")) {

            mRating.setVisibility(View.INVISIBLE);
            mRatingBar.setVisibility(View.INVISIBLE);
            shopName.setText(listing.getSellerName());
            mLocation.setText(listing.getLocation());

            if (!TextUtils.isEmpty(listing.getRoom())) {
                getActivity().findViewById(R.id.header_room).setVisibility(View.VISIBLE);
                mRoom.setVisibility(View.VISIBLE);
                mRoom.setText(listing.getRoom());
            }
            if (!TextUtils.isEmpty(listing.getHostel())) {
                getActivity().findViewById(R.id.header_hostel).setVisibility(View.VISIBLE);
                mHostel.setVisibility(View.VISIBLE);
                mHostel.setText(listing.getHostel());
            }
            Picasso.get()
                    .load(listing.getSellerPhoto())
                    .into(shopLogo);

            shimmerProfilePhoto.setVisibility(View.INVISIBLE);
            btnPatron.setText("Contact");
            btnChat.setVisibility(View.GONE);

        } else if (listing.getListingSource().equals("shop")) {
            getRating(listing);
            btnChat.setVisibility(View.VISIBLE);
            btnPatron.setText("Become a Patron");
        }

        String key = mStatsRef.push().getKey();
        mStatsRef.child("Views").child(listing.getPostKey()).child(key).child("viewed")
                .setValue(currentUser);
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

    private void setListeners() {

        Glide.with(getActivity()).asBitmap().load(listing.getImageUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        btnShare.setVisibility(View.VISIBLE);
                        btnShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareImage(resource);
                            }
                        });

                        btnChat.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {

                                String message = "Say something";
                                String cleanNumber = cleanPhoneNumber(listing.getSellerPhone());
                                downloadAndSendWhatsAppImage(getActivity(), cleanNumber, message, listing.getImageUrl());

                            }
                        });

                        if (listing.getListingSource().equals("individual")) {
                            btnPatron.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    String message = "Say something";
                                    String cleanNumber = cleanPhoneNumber(listing.getSellerPhone());
                                    downloadAndSendWhatsAppImage(getActivity(), cleanNumber, message, listing.getImageUrl());

                                }
                            });
                        } else {

                            btnChat.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    String message = "Say something";
                                    String cleanNumber = cleanPhoneNumber(listing.getSellerPhone());
                                    downloadAndSendWhatsAppImage(getActivity(), cleanNumber, message, listing.getImageUrl());

                                }
                            });
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

    }
    private String cleanPhoneNumber(String phoneNumber) {
        return phoneNumber.replaceFirst("^\\+", "");
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted && pendingResource != null) {
                proceedWithChatLogic(pendingResource);
            } else {
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted && pendingResource != null) {
                    proceedWithChatLogic(pendingResource);
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            });


    private void proceedWithChatLogic(Bitmap resource) {
        SharedPreferences.Editor write_prefs = getActivity().getSharedPreferences("CHAT_PREFS", MODE_PRIVATE).edit();
        SharedPreferences read_prefs = getActivity().getSharedPreferences("CHAT_PREFS", MODE_PRIVATE);

        boolean firstStart = read_prefs.getBoolean("firstStart", true);

        if (firstStart) {
            chatPrompt(resource, write_prefs);
        } else {
            chat(resource);
        }
    }

    private void requestPermission(Bitmap resource) {
        this.pendingResource = resource;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    },
                    REQUEST_PERMISSIONS_CODE
            );
        } else {
            proceedWithChatLogic(resource);
        }
    }

    private void openWhatsApp(String number) {










        try {
            number = number.replace(" ", "").replace("+", "");

            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(number) + "@s.whatsapp.net");
            getActivity().startActivity(sendIntent);

        } catch (Exception e) {
        }
    }

    private void chat(Bitmap resource) {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());

        mStatsRef.child("Leads").child(listing.getPostKey()).child(currentUser).child("name")
                .setValue(signInAccount.getDisplayName());
        mStatsRef.child("Leads").child(listing.getPostKey()).child(currentUser).child("userId").
                setValue(currentUser);

        if (isAppInstalled(((FragmentMerger) getActivity()).context, "com.whatsapp.w4b")) {

            try {
                saveImageToGallery(resource, listing.getName());
                openWhatsApp(listing.getSellerPhone());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isAppInstalled(((FragmentMerger) getActivity()).context, "com.whatsapp")) {

            try {
                saveImageToGallery(resource, listing.getName());
                openWhatsApp(listing.getSellerPhone());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), "Whatsapp not installed on your device", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveImageToGallery(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getActivity().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/" + "CladeInterests");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).toString() + File.separator + "CladeInterests";

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);

            scanner(image.getAbsolutePath());

        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
    }

    private void scanner(String path) {

        MediaScannerConnection.scanFile(getActivity(),
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        openWhatsApp(listing.getSellerPhone());
                    }
                });
    }

    private boolean isAppInstalled(Context ctx, String packageName) {
        PackageManager pm = ctx.getApplicationContext().getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void chatPrompt(Bitmap resource, SharedPreferences.Editor write_prefs) {

        TapTargetView.showFor(getActivity(),
                TapTarget.forView(btnChat, "You'll be redirected to Whatsapp", "Tap the camera icon on whatsapp to view and comment on this product")
                        .outerCircleColor(R.color.teal_200)
                        .outerCircleAlpha(0.96f)
                        .targetCircleColor(R.color.white)
                        .titleTextSize(25)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(16)
                        .descriptionTextColor(R.color.black)
                        .textColor(R.color.black)
                        .textTypeface(Typeface.SANS_SERIF)
                        .dimColor(R.color.black)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .targetRadius(60),
                new TapTargetView.Listener() {

                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);

                        chat(resource);

                        write_prefs.putBoolean("firstStart", false);
                        write_prefs.apply();

                    }
                });
    }

    private void shareImage(Bitmap bitmap) {

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        Uri uri;

        String textToShare = "Say Something";
        uri = saveImage(bitmap, getActivity().getApplicationContext());


        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, "New App");
        share.putExtra(Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(share, "Share Content"));
    }

    private static Uri saveImage(Bitmap image, Context context) {

        File imagefolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imagefolder.mkdirs();
            File file = new File(imagefolder, "shared_images.jpg");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.flush();
            stream.close();

            uri = FileProvider.getUriForFile(Objects.requireNonNull(context.getApplicationContext()),
                    context.getPackageName() + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    @Override
    public void callBack(int position) {
        Listing listing = (Listing) serviceList.get(position).getObject();
        if (listing.getImageUrl().isEmpty()) {

            SavedServices.saved.trade = "Services";
            SavedServices.saved.dialogEvent(listing, getActivity());
        }
    }

    @Override
    public void onDelete(int position) {

    }
}

