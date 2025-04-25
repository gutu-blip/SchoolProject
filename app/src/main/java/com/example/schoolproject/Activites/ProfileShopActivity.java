package com.example.schoolproject.Activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Adapters.ListingsAdapter;
import com.example.schoolproject.Adapters.ProfilesAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.Models.UserProfile;
import com.example.schoolproject.R;
import com.example.schoolproject.Utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;

public class ProfileShopActivity extends AppCompatActivity implements CallBackListener {

    public static String CONSUMER_KEY = "";
    public static String CONSUMER_SECRET = "";

    public static String MODE = "";
    public static String BUSINESS_SHORT_CODE = "";
    public static String PASSKEY = "";
    public static String PARTYB = "";
    public static String CALLBACKURL = "";

    TextView mPatrons, mName, mMantra, mRecos, mRating, mRecosAlert, mPatronsAlert;
    RecyclerView recyclerView, btmRecyclerview;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    ListingsAdapter mAdapter;
    Intent intent;
    private FloatingActionButton fabEdit;
    BottomSheetBehavior bottomSheetBehavior;
    FloatingActionButton mAdd;
    ProfilesAdapter profilesAdapter;
    String shopName, patrons, profilephoto, mantra;
    private DatabaseReference mListingsRef, mPatronsRef;
    public ImageView mImageview;
    public static String currentUser, sellerKey, shopPhone;
    public ArrayList<Listing> mListings;
    private Seller shopProfile;
    RatingBar ratingBar;
    double average = 0.0;
    ProgressBar mProgressCircleProfiles;
    AutoCompleteTextView mSearch;
    ArrayList<UserProfile> patronsList;
    String loggedUser_Photo, loggedUser_Name, loggedUser_UserName;
    ArrayList<UserProfile> filteredProfiles;
    ArrayList<String> patronNames = new ArrayList<>();
    CoordinatorLayout parentView;
    ProgressBar progressCircle;
    SharedPreferences.Editor write_prefs;
    SharedPreferences read_prefs;
    Context context = ProfileShopActivity.this;
    private SweetAlertDialog sweetAlertDialog;
    int recosCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_shop);
        getWindow().setSoftInputMode(3);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        read_prefs = getSharedPreferences("SHOP_PREFS", MODE_PRIVATE);
        write_prefs = getSharedPreferences("SHOP_PREFS", MODE_PRIVATE).edit();

        sellerKey = read_prefs.getString("sellerKey", "default");
        shopPhone = read_prefs.getString("shopPhone", "default");
        shopName = read_prefs.getString("shopName", "default");

        patronsList = new ArrayList<>();

        initWidgets();
        init();
//      checkPaymentStatus();
        setListeners();
        setShopProfile();
        fetchPatrons();
        initBottomSheet();
        fetchCatalogue();
        getRating();
//      tapTarget();

    }

    private void getConfigs() {
        DatabaseReference mPaymentsRef = database.getReference("Payments");
        mPaymentsRef.keepSynced(true);

        DatabaseReference mConfigRef = mPaymentsRef.child("Configs");

        mConfigRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                CONSUMER_SECRET = snapshot.child("consumerSecret").getValue(String.class);
                MODE = snapshot.child("mode").getValue(String.class);
                CONSUMER_KEY = snapshot.child("consumerKey").getValue(String.class);
                BUSINESS_SHORT_CODE = snapshot.child("businessShortCode").getValue(String.class);
                CALLBACKURL = snapshot.child("callBackUrl").getValue(String.class);
                PARTYB = snapshot.child("partyB").getValue(String.class);
                PASSKEY = snapshot.child("passKey").getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        if (bottomSheetBehavior.isHideable()) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetBehavior.setPeekHeight(0);

        } else {
            super.onBackPressed();
        }
    }

    private void setListeners() {

        findViewById(R.id.fab_shop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileShopActivity.this, ShopInfoActivity.class);
                intent.putExtra("shop", shopProfile);
                intent.putExtra("operation", "OpenShop");

                startActivity(intent);
            }
        });

        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ProfileShopActivity.this, ShopInfoActivity.class);
                intent.putExtra("shop", shopProfile);
                intent.putExtra("operation", "UpdateProfile");

                startActivity(intent);
            }
        });

        findViewById(R.id.ll_patrons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (patronsList.size() > 0) {

                    fabEdit.setVisibility(View.INVISIBLE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    mProgressCircleProfiles.setVisibility(View.VISIBLE);

                    profilesAdapter = new ProfilesAdapter(patronsList,ProfileShopActivity.this);

                    SlideInRightAnimationAdapter alphaInAnimationAdapter = new SlideInRightAnimationAdapter(profilesAdapter);
                    alphaInAnimationAdapter.setDuration(400);
                    alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
                    alphaInAnimationAdapter.setFirstOnly(false);

                    btmRecyclerview.setAdapter(alphaInAnimationAdapter);
                    profilesAdapter.notifyDataSetChanged();
                    mProgressCircleProfiles.setVisibility(View.INVISIBLE);
                } else {
                    Snackbar.make(parentView, "Your shop doesn't have any patrons yet.", Snackbar.LENGTH_LONG)
                            .setAction("", null).show();
                }
            }
        });

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(shopProfile.getTrade().equals("Products")){
                    Intent intent = new Intent(ProfileShopActivity.this, ProductListing.class);
                    intent.putExtra("listingSource","shop");
                    intent.putExtra("shop",shopProfile);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(ProfileShopActivity.this, ServiceListing.class);
                    intent.putExtra("listingSource","shop");
                    intent.putExtra("shop",shopProfile);
                    startActivity(intent);

                }
            }
        });
    }

    private void init() {

        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText("Connecting to Safaricom");
        sweetAlertDialog.setContentText("Please wait...");
        sweetAlertDialog.setCancelable(false);

        SharedPreferences prefs_profile = getSharedPreferences("HOME_PROFILE_PREFS", MODE_PRIVATE);
        loggedUser_Photo = prefs_profile.getString("profilephoto", "none");
        loggedUser_UserName = prefs_profile.getString("username", "none");
        loggedUser_Name = prefs_profile.getString("name", loggedUser_UserName);

        mListingsRef = database.getReference("Listings");
        mListingsRef.keepSynced(true);

        currentUser = mUser.getUid();
        mPatronsRef = database.getReference("Followed").child("Shops");
        mPatronsRef.keepSynced(true);

        shopProfile = getIntent().getParcelableExtra("shop");
        if(shopProfile.getTrade().equals("Products")) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }else{
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false));
        }

        sellerKey = shopProfile.getSellerKey();
        final DatabaseReference recoRef = database.getReference("Followed").child("Shop Recos");
        recoRef.keepSynced(true);

        recoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                recosCount = (int) snapshot.child(sellerKey).getChildrenCount();
                if (recosCount == 1) {
                    mRecos.setText(Utils.formatValue(recosCount));
                } else {
                    mRecos.setText(Utils.formatValue(recosCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setShopProfile() {

        shopName = shopProfile.getName();
        patrons = shopProfile.getPatrons();
        profilephoto = shopProfile.getProfilePhoto();
        mantra = shopProfile.getMantra();

        mName.setText(shopName);
        mMantra.setText(mantra);
        mPatrons.setText(patrons);

        Picasso.get()
                .load(profilephoto)
                .placeholder(R.drawable.image_placeholder)
                .into(mImageview);

    }

    private void fetchPatrons() {

        mPatronsRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {

                patronsList.clear();
                //fetch patrons
                for (DataSnapshot followerSnapshot : snapshot.child(sellerKey).getChildren()) {
                    UserProfile profile = new UserProfile();

                    if (profile != null) {

                        String profilePhoto = followerSnapshot.child("profilephoto").getValue(String.class);
                        String name = followerSnapshot.child("name").getValue(String.class);
                        String followerUid = followerSnapshot.child("follower").getValue(String.class);

                        patronNames.add(name);
                        profile.setProfilePhoto(profilePhoto);
                        profile.setDisplayName(name);
                        profile.setUserId(followerUid);

                        patronsList.add(profile);
                    }
                }
                mPatrons.setText(Utils.formatValue(patronsList.size()));
            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void initBottomSheet() {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initBottomSheetWidgets();
        bottomSheetBehavior.setPeekHeight(0);

        //   set your sheet hideable or not
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    fabEdit.setVisibility(View.VISIBLE);
                    bottomSheetBehavior.setPeekHeight(0);
                    bottomSheetBehavior.setHideable(false);

                    if (profilesAdapter != null) {
                        profilesAdapter = null;
                    }
                } else {
                    setAutoComplete(patronNames);
                    bottomSheetBehavior.setHideable(true);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                searchItems(editable.toString());
            }
        });
    }

    private void setAutoComplete(ArrayList<String> subCategories) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfileShopActivity.this,
                android.R.layout.simple_dropdown_item_1line, subCategories);
        mSearch.setThreshold(1);
        mSearch.setAdapter(adapter);

        mSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                searchItems(adapter.getItem(i));
            }
        });

    }

    private void searchItems(String param) {
        filteredProfiles = new ArrayList<>();

        if (profilesAdapter != null) {

            for (UserProfile profile : patronsList) {
                if (profile.getDisplayName().toLowerCase().contains(param.toLowerCase())) {
                    filteredProfiles.add(profile);
                }
            }
            profilesAdapter.filteredList(filteredProfiles);
        }
    }

    private void initBottomSheetWidgets() {
        RelativeLayout bottomsheetLayout = findViewById(R.id.bottom_sheet_recycler);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        btmRecyclerview = bottomsheetLayout.findViewById(R.id.recyclerview);
        btmRecyclerview.setLayoutManager(new LinearLayoutManager(ProfileShopActivity.this, LinearLayoutManager.VERTICAL, false));
        ImageButton btnAdd = bottomsheetLayout.findViewById(R.id.btn_add);
        btnAdd.setVisibility(View.GONE);

        FloatingActionButton fabCancel = bottomsheetLayout.findViewById(R.id.fab_cancel);
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        mSearch = bottomsheetLayout.findViewById(R.id.et_search);
        mSearch.setHint("Search for names");
        mProgressCircleProfiles = bottomsheetLayout.findViewById(R.id.progress_circle);

    }

    public void getRating() {
        try {
            final DatabaseReference dbRef = database.getReference("Profiles").child(currentUser)
                    .child("Shops").child(sellerKey).child("Ratings");
            dbRef.keepSynced(true);

            final DatabaseReference ratingRef = database.getReference("Profiles").child(currentUser)
                    .child("Shops").child(sellerKey).child("Profile").child("rating");
            ratingRef.keepSynced(true);

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
                    DecimalFormat percentageFormat = new DecimalFormat("0.0");
                    String finalAverage = percentageFormat.format(average);
                    ratingRef.setValue(finalAverage);
                    ratingBar.setRating((float) average);
                    mRating.setText(finalAverage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });
        } catch (Exception e) {
            Toast.makeText(ProfileShopActivity.this, "" + e, Toast.LENGTH_SHORT).show();
        }
    }


    private void initWidgets() {

        mPatronsAlert = findViewById(R.id.tv_alert_patrons);
        mRecosAlert = findViewById(R.id.tv_alert_recos);
        parentView = findViewById(R.id.coordinator);
        progressCircle = findViewById(R.id.progress_circle);
        fabEdit = (FloatingActionButton) findViewById(R.id.fab_edit);
        ratingBar = findViewById(R.id.rating_bar);
        mRecos = findViewById(R.id.tv_recos);
        mName = findViewById(R.id.tv_shop_name);
        mImageview = findViewById(R.id.profile_photo);
        mMantra = findViewById(R.id.tv_mantra);
        mRating = findViewById(R.id.tv_rating);
        mPatrons = findViewById(R.id.tv_patrons);
        mAdd = findViewById(R.id.fab_add);

        recyclerView = findViewById(R.id.recyclerview);
    }

    public void fetchCatalogue() {

        mListingsRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                mListings = new ArrayList();

                for (DataSnapshot tradeSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot collegeSnapshot : tradeSnapshot.getChildren()) {
                        for (DataSnapshot keySnapshot : collegeSnapshot.getChildren()) {

                            Listing listing = keySnapshot.child("Details").getValue(Listing.class);

                            if (listing!=null) {
                                if (listing.getSellerKey()
                                        .equals(shopProfile.getSellerKey())) {
                                    ArrayList<String> album = new ArrayList<>();
                                    for (DataSnapshot imageKey : keySnapshot.child("Album").getChildren()) {
                                        String image = imageKey.child("imageUrl").getValue(String.class);
                                        album.add(image);
                                    }
                                    listing.setAlbum(album);
                                    mListings.add(listing);
                                }
                            }
                        }
                    }
                }
                progressCircle.setVisibility(View.INVISIBLE);

                if (mListings.size() == 0) {
                    Snackbar.make(parentView, "You don't have a catalogue yet.", Snackbar.LENGTH_LONG)
                            .setAction("Get Started", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(shopProfile.getTrade().equals("Products")){
                                        intent = new Intent(ProfileShopActivity.this,ProductListing.class);
                                    }else {
                                        intent = new Intent(ProfileShopActivity.this,ServiceListing.class);
                                    }
                                    intent.putExtra("listingSource","shop");
                                    intent.putExtra("shop", shopProfile);
                                    startActivity(intent);
                                }
                            }).show();

                    return;
                }

                mAdapter = new ListingsAdapter(ProfileShopActivity.this,mListings,ProfileShopActivity.this);
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void callBack(int position) {

            Listing listing = mListings.get(position);

            Intent intent = new Intent().setClass(ProfileShopActivity.this, ProductStatsActivity.class);
            intent.putExtra("listing", listing);
            intent.putExtra("shop", shopProfile);
            startActivity(intent);

    }
}