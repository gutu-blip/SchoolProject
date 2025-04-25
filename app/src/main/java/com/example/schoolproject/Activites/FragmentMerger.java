package com.example.schoolproject.Activites;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.schoolproject.Adapters.SliderPager;
import com.example.schoolproject.Fragments.ProductDetailsFragment;
import com.example.schoolproject.Fragments.ShopDetailsFragment;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentMerger extends AppCompatActivity {

    PagerAdapter pagerAdapter;
    ViewPager viewPager;
    String uid,shopKey;
    Listing listing;
    public Context context = FragmentMerger.this;
    String source="",trade = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_merger);

        viewPager = findViewById(R.id.container);
        List<Fragment> list = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            source = getIntent().getStringExtra("src");
            trade = getIntent().getStringExtra("trade");
            listing = getIntent().getParcelableExtra("listing");
            uid = listing.getUserID();
            shopKey = listing.getSellerKey();

                if(listing.getListingSource().equals("individual")) {
                    list.add(new ProductDetailsFragment(listing, shopKey, uid,listing.getTrade()));
                }else{
                    list.add(new ProductDetailsFragment(listing, shopKey, uid,listing.getTrade()));
                    list.add(new ShopDetailsFragment(listing,uid,shopKey,listing.getTrade()));
                }
            }

        pagerAdapter = new SliderPager(getSupportFragmentManager(), list);
        viewPager.setAdapter(pagerAdapter);
    }
}
