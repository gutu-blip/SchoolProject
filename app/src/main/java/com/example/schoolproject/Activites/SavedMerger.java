package com.example.schoolproject.Activites;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.schoolproject.Adapters.PhotoAdapter;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;

import java.util.ArrayList;

public class SavedMerger extends AppCompatActivity {

    ViewPager2 viewPager2;
    ArrayList<Listing> mProducts, mServices;
    Context context = SavedMerger.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_merger);

        viewPager2 = findViewById(R.id.viewpager2);
        mProducts = new ArrayList<>();
        mServices = new ArrayList<>();

        setViewPager();
    }

    private void setViewPager() {

        Listing product = getIntent().getExtras().getParcelable("product");
        Listing service = getIntent().getExtras().getParcelable("service");

        ArrayList<Listing> services = getIntent().getParcelableArrayListExtra("services");
        ArrayList<Listing> products = getIntent().getParcelableArrayListExtra("products");

        String src = getIntent().getStringExtra("src");
        int position = getIntent().getIntExtra("position", 0);

        if (src.equals("product")) {

            if (products.size() > 1) {
                products.remove(position);
                mProducts.add(product);
                mProducts.addAll(products);

                viewPager2.setAdapter(new PhotoAdapter(SavedMerger.this, mProducts, context));
            } else {
                viewPager2.setAdapter(new PhotoAdapter(SavedMerger.this, products, context));
            }

        } else {
            if (services.size() != 1) {
                mServices.add(service);
                mServices.addAll(newList(services,service.getPostKey()));

                viewPager2.setAdapter(new PhotoAdapter(SavedMerger.this, mServices, context));
            } else {
                viewPager2.setAdapter(new PhotoAdapter(SavedMerger.this, services, context));
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

    private ArrayList<Listing> newList(ArrayList<Listing> listings, String key) {

        ArrayList<Listing> newList = new ArrayList<>();
        for (Listing listing : listings) {
            if (!listing.getPostKey().equals(key)) {
                newList.add(listing);
            }
        }
        return newList;
    }

    private void add(ArrayList<Listing> services) {
        for (Listing listing : services) {
            if (!listing.getImageUrl().isEmpty()) {
                mServices.add(listing);
            }
        }
    }
}