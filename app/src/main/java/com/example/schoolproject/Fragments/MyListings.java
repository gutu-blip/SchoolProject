package com.example.schoolproject.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.example.schoolproject.Adapters.MyPagerAdapter;
import com.example.schoolproject.R;


public class MyListings extends Fragment {

    CoordinatorLayout parentView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static Toolbar toolbar;
    TextView mTitle;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_my_listings, container, false);

       parentView = view.findViewById(R.id.parent);
       tabLayout = view.findViewById(R.id.tabLayout);
       viewPager = view.findViewById(R.id.viewpager);
       mTitle = view.findViewById(R.id.tv_title);
       toolbar = view.findViewById(R.id.toolbar);
       return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTitle.setText("My Listings");
        setTabs();

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

        pagerAdapter.addFragment(new MyProductsListings());
        pagerAdapter.addFragment(new MyServiceListings());
        pagerAdapter.addFragment(new MyGigs());

        viewPager.setAdapter(pagerAdapter);
    }
}