package com.example.schoolproject.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.schoolproject.Utils.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Adapters.ListingsAdapter;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ShopDetailsFragment extends Fragment {

    DatabaseReference mShopRef, recommendRef, mListingsRef;
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String currentUser;
    ImageView profilePhoto;
    private TextView mShopName, mRecos, mHostel, mMantra, mPhone, mEmail, mCollege, mRoom,
            mRating, mPatrons;
    RatingBar shopRating;
    Button rateBtn, btnRecommend, btnPatron, btnContacts;
    double average = 0.0;
    RecyclerView recyclerView;
    int recosCount, followerscount;
    public Boolean recommendChecker = false;
    private DatabaseReference followedRef;
    public Boolean followedChecker = false;
    ArrayList<Listing> catalogueList;
    ProgressBar progressCircle;
    String shopUid, shopKey;
    Dialog dialog;
    String trade = "";
    Listing listing;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.activity_shop_profile, container, false);

        initWidgets(view);
        return view;
    }

    public ShopDetailsFragment(Listing listing, String shopUid, String shopKey,String trade) {
        this.listing = listing;
        this.shopUid = shopUid;
        this.shopKey = shopKey;
        this.trade = trade;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setItems();
        setDialog();
        fetchCatalogue();
        followEvent();
        recommendEvent();
        fetchShopInfo();
        setListeners();
        if(listing.getListingSource().equals("shop")){
            getRating();
        }
    }

    private void setDialog() {
        dialog = new Dialog(getActivity());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.dialog_shop_profile);

        mCollege = dialog.findViewById(R.id.tv_college);
        mEmail = dialog.findViewById(R.id.tv_email);
        mPhone = dialog.findViewById(R.id.tv_phone);
        mHostel = dialog.findViewById(R.id.tv_hostel);
        mRoom = dialog.findViewById(R.id.tv_room);
    }

    private void setItems() {

        currentUser = mUser.getUid();
        mListingsRef = database.getReference("Listings");
        mShopRef = database.getReference("Profiles");
        mShopRef.keepSynced(true);

        followedRef = database.getReference("Followed").child("Shops");
        followedRef.keepSynced(true);

        recommendRef = database.getReference("Followed").child("Shop Recos");
        recommendRef.keepSynced(true);

        SharedPreferences prefs_profile = getActivity().getSharedPreferences("HOME_PROFILE_PREFS", MODE_PRIVATE);
        if (currentUser.equals(shopUid)) {

            btnPatron.setVisibility(View.GONE);
            btnRecommend.setVisibility(View.GONE);
            rateBtn.setVisibility(View.GONE);
        }
    }

    private void initWidgets(View view) {
        btnRecommend = view.findViewById(R.id.btn_recommend);
        btnPatron = view.findViewById(R.id.btn_patron);
        btnContacts = view.findViewById(R.id.btn_contacts);
        progressCircle = view.findViewById(R.id.progress_circle);
        mRecos = view.findViewById(R.id.tv_recommenations);
        recyclerView = view.findViewById(R.id.recyclerview);

        mPatrons = view.findViewById(R.id.tv_patrons);
        mRating = view.findViewById(R.id.tv_rating);
        rateBtn = view.findViewById(R.id.btn_rate);
        shopRating = view.findViewById(R.id.rating_bar);
        mShopName = view.findViewById(R.id.tv_shop_name);
        mMantra = view.findViewById(R.id.tv_mantra);
        profilePhoto = view.findViewById(R.id.profile_photo);

    }

    public void followChecker(final String shopKey) {

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        followedRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.child(shopKey).hasChild(uid)) {

                    btnPatron.setText("Patron");
                    followerscount = (int) snapshot.child(shopKey).getChildrenCount();
                    mPatrons.setText(Utils.formatValue(followerscount));

                } else {
                    btnPatron.setText("Become a Patron");
                    followerscount = (int) snapshot.child(shopKey).getChildrenCount();
                    mPatrons.setText(Utils.formatValue(followerscount));
                }
            }

            public void onCancelled(DatabaseError error) {

            }
        });
    }

    public void recommendChecker(final String shopKey) {

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recommendRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.child(shopKey).hasChild(uid)) {

                    btnRecommend.setText("Recommended");
                    recosCount = (int) snapshot.child(shopKey).getChildrenCount();
                    mRecos.setText(Utils.formatValue(recosCount));

                } else {
                    btnRecommend.setText("Recommend");
                    recosCount = (int) snapshot.child(shopKey).getChildrenCount();
                    mRecos.setText(Utils.formatValue(recosCount));
                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void recommendEvent() {

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = mUser.getUid();

        recommendChecker(shopKey);

        if (shopKey != null) {

            btnRecommend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
                    String name = signInAccount.getDisplayName();
                    String photoUrl = signInAccount.getPhotoUrl().toString();

                    recommendChecker = true;

                    recommendRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (recommendChecker.equals(true)) {
                                if (snapshot.child(shopKey).hasChild(currentUser)) {

                                    recommendRef.child(shopKey).child(currentUser).removeValue();
                                    recommendChecker = false;
                                } else {

                                    recommendRef.child(shopKey).child(currentUser).child(currentUser).setValue(true);
                                    recommendRef.child(shopKey).child(currentUser).child("recommended by").setValue(currentUser);

                                    recommendRef.child(shopKey).child(currentUser).child("shop").setValue(shopKey);
                                    recommendRef.child(shopKey).child(currentUser).child("profilephoto").setValue(photoUrl);
                                    recommendRef.child(shopKey).child(currentUser).child("name").setValue(name);

                                    recommendChecker = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                }
            });
        } else {
            Toast.makeText(getActivity(), "null", Toast.LENGTH_SHORT).show();
        }
    }

    private void followEvent() {

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = mUser.getUid();

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

                                    followedRef.child(shopKey).child(currentUser).child(currentUser).setValue(true);
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
        } else {
            Toast.makeText(getActivity(), "null", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchShopInfo() {

        mShopRef.child(shopUid).child("Shops").child(shopKey).child("Profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Seller seller = snapshot.getValue(Seller.class);
                if (snapshot.exists()) {

                   if(seller.getTrade().equals("Products")){
                       recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                   }else{
                       recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                   }

                    mHostel.setText(seller.getHostel());
                    mShopName.setText(seller.getName());
                    mCollege.setText(seller.getCollege());
                    mRoom.setText(seller.getRoom());
                    mMantra.setText(seller.getMantra());
                    mEmail.setText(seller.getEmail());
                    mPhone.setText(seller.getPhone());

                    Picasso.get()
                            .load(seller.getProfilePhoto())
                            .into(profilePhoto);

                    if (seller.getUserID() != null) {
                        //Fetch recommendations
                        final DatabaseReference recoRef = database.getReference("Profiles").child(seller.getUserID())
                                .child("Shops");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchCatalogue() {

        catalogueList = new ArrayList<>();

        mListingsRef.child(trade).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catalogueList.clear();

                    for (DataSnapshot collegeSnapshot : snapshot.getChildren()) {
                        for (DataSnapshot keySnapshot : collegeSnapshot.getChildren()) {
                            Listing mListing = keySnapshot.child("Details").getValue(Listing.class);

                            ArrayList<String> album = new ArrayList<>();
                            for (DataSnapshot imageKey : keySnapshot.child("Album").getChildren()) {
                                String image = imageKey.child("imageUrl").getValue(String.class);
                                album.add(image);
                            }

                            mListing.setAlbum(album);
                            if (mListing.getSellerKey().equals(shopKey)) {
                                catalogueList.add(mListing);
                            }
                        }
                    }
                onLoadCatalogue(catalogueList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onLoadCatalogue(ArrayList<Listing> catalogueList) {

        ListingsAdapter mAdapter = new ListingsAdapter(getActivity(), catalogueList);
        recyclerView.setAdapter(mAdapter);

        progressCircle.setVisibility(View.INVISIBLE);

    }

    private void setListeners() {

        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        dialog.findViewById(R.id.btn_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        rateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(getActivity());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.setContentView(R.layout.dialog_ratingbar);
                dialog.show();

                RatingBar ratingBar = dialog.findViewById(R.id.rating_bar);
                TextView tvRating = dialog.findViewById(R.id.tv_rating);
                Button btnSubmit = dialog.findViewById(R.id.btn_submit);

                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                        tvRating.setText(String.format("(%s)", v));
                    }
                });

                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final DatabaseReference dbRef = database.getReference("Profiles").child(shopUid)
                                .child("Shops").child(shopKey).child("Ratings").child(currentUser).child("rating");

                        double intRating = ratingBar.getRating();
                        dbRef.setValue(intRating);

                        dialog.dismiss();

                    }
                });

            }
        });

    }


    public void getRating() {
        try {
            final DatabaseReference dbRef = database.getReference("Profiles").child(shopUid)
                    .child("Shops").child(shopKey).child("Ratings");

            final DatabaseReference ratingRef = database.getReference("Profiles").child(shopUid)
                    .child("Shops").child(shopKey).child("Profile").child("rating");

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

                    shopRating.setRating((float) average);
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
}