package com.example.schoolproject.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.example.schoolproject.Activites.ProfileShopActivity;
import com.example.schoolproject.Activites.ShopInfoActivity;
import com.example.schoolproject.Adapters.MyPagerAdapter;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    CoordinatorLayout parentView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    CircleImageView shopLogo;
    com.getbase.floatingactionbutton.FloatingActionButton fabShop;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mShopRef, mCollegesRef, mCategoriesRef;
    String currentUser = mUser.getUid();
    View view;
    ShimmerFrameLayout shimmerShop;
    ArrayList<Seller> mShops;
    ArrayList<String> mColleges = new ArrayList<>();
    ArrayList<String> serviceCategories = new ArrayList<>();
    ArrayList<String> productCategories = new ArrayList<>();
    int i = 0;
    LoadListener mLoadListener;

    public interface LoadListener {
        void loadColleges(ArrayList<String> list);

        void loadProductCategories(ArrayList<String> list);

        void loadServiceCategories(ArrayList<String> list);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.frag_home, container, false);

        shimmerShop = view.findViewById(R.id.shimmer_shop);
        shopLogo = view.findViewById(R.id.shop_photo);
        parentView = view.findViewById(R.id.parent);
        fabShop = view.findViewById(R.id.fab_shop);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewpager);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        setItems();
        setTabs();
        checkShop();
        fetchCategories();
        fetchColleges();
        setListeners();

    }


    private void checkShop() {

        DatabaseReference mShopRef = database.getReference("Profiles").child(currentUser).child("Shops");
        mShopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChildren()) {
                    for (DataSnapshot keySnapshot : snapshot.getChildren()) {

                        Seller seller = keySnapshot.child("Profile").getValue(Seller.class);
                        mShops.add(seller);
                    }
                    shimmerShop.stopShimmer();
                    shimmerShop.setVisibility(View.GONE);
                    shopLogo.setVisibility(View.VISIBLE);

                    Picasso.get()
                            .load(mShops.get(0).getProfilePhoto())
                            .into(shopLogo);

                    shopLogo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), ProfileShopActivity.class);
                            intent.putExtra("shop", mShops.get(0));
                            startActivity(intent);
                        }
                    });
                } else {
                    shimmerShop.stopShimmer();
                    shimmerShop.setVisibility(View.GONE);
                    fabShop.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void saveColleges(ArrayList<String> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    public void saveProductCategories(ArrayList<String> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public void saveServiceCategories(ArrayList<String> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    private void fetchColleges() {

        mCollegesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mColleges.clear();
                for (DataSnapshot keySnapshot : snapshot.getChildren()) {
                    String college = keySnapshot.child("name").getValue(String.class);
                    mColleges.add(college);
                }

                saveColleges(mColleges, "colleges");
                mLoadListener.loadColleges(mColleges);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchCategories() {

        mCategoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                productCategories.clear();
                serviceCategories.clear();
                for (DataSnapshot tradeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot keySnapshot : tradeSnapshot.getChildren()) {
                        String category = keySnapshot.child("title").getValue(String.class);
                        if (tradeSnapshot.getKey().equals("Products")) {
                            productCategories.add(category);
                        } else {
                            serviceCategories.add(category);
                        }
                    }
                }

                saveProductCategories(productCategories, "product");
                saveServiceCategories(serviceCategories, "service");

                if (mLoadListener != null) {
                    mLoadListener.loadProductCategories(productCategories);
                    mLoadListener.loadServiceCategories(serviceCategories);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setTabs() {
        addPages();

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {

                        tabLayout.getSelectedTabPosition();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
    }

    private void addPages() {
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getChildFragmentManager());

        pagerAdapter.addFragment(new GoodsPosts());
        pagerAdapter.addFragment(new ServicesPosts());
        pagerAdapter.addFragment(new Gigs());

        viewPager.setAdapter(pagerAdapter);
    }

    private void setItems() {
        mShops = new ArrayList<>();

        shimmerShop.stopShimmer();
        shimmerShop.setVisibility(View.VISIBLE);

        mShopRef = database.getReference("Profiles").child(currentUser).child("Shops");
        mShopRef.keepSynced(true);
        mCategoriesRef = database.getReference("Categories");
        mCollegesRef = database.getReference("Colleges");
        mCategoriesRef.keepSynced(true);
        mCollegesRef.keepSynced(true);

    }


    private void setListeners() {
        fabShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShopInfoActivity.class);
                intent.putExtra("operation", "OpenShop");

                getActivity().startActivity(intent);
            }
        });


    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof LoadListener) {
            mLoadListener = (LoadListener) childFragment;
        }
    }
}