package com.example.schoolproject.Adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.Activites.FragmentMerger;
import com.example.schoolproject.Activites.ProductStatsActivity;
import com.example.schoolproject.Activites.SavedMerger;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VPSliderAdapter extends RecyclerView.Adapter<VPSliderAdapter.ViewHolder> {

    ArrayList<String> imageList;
    Listing listing;
    Context context;
    ArrayList<Listing> savedListings;
    int savedPosition;
    String calling="";

    public VPSliderAdapter(Context context, ArrayList<String> imageList, Listing listing,String calling) {
        this.imageList = imageList;
        this.context = context;
        this.listing = listing;
        this.calling = calling;

    }
    public VPSliderAdapter(Context context, ArrayList<String> imageList, Listing listing,int savedPosition,
                           String calling) {
        this.imageList = imageList;
        this.context = context;
        this.listing = listing;
        this.savedPosition = savedPosition;
        this.calling = calling;
    }

    public VPSliderAdapter(Context context, ArrayList<String> imageList,int savedPosition,
                           ArrayList<Listing> savedListings,Listing listing) {
        this.context = context;
        this.imageList = imageList;
        this.savedPosition = savedPosition;
        this.savedListings = savedListings;
        this.listing = listing;

    }
    public VPSliderAdapter(Context context, ArrayList<String> imageList) {
        this.imageList = imageList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider_viewpager, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (listing!=null) {
            Picasso.get().load(imageList.get(position))
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.roundedImageView);

            holder.roundedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(savedListings!=null) {
                        Intent intent = new Intent(context, SavedMerger.class);

                        intent.putExtra("service", listing);
                        intent.putExtra("src", "service");
                        intent.putExtra("position", savedPosition);
                        intent.putParcelableArrayListExtra("services", savedListings);
                        context.startActivity(intent);
                    }else {
                        if(calling.equals("ServicesPosts")){
                            Intent intent = new Intent(context, FragmentMerger.class);
                            intent.putExtra("listing", listing);
                            context.startActivity(intent);
                        }else{
                            SharedPreferences.Editor editor = context.getSharedPreferences("POS_PREFS", MODE_PRIVATE).edit();
                            editor.putInt("pos",savedPosition);
                            editor.apply();

                            Intent intent = new Intent(context, ProductStatsActivity.class);
                            intent.putExtra("listing", listing);

                            context.startActivity(intent);
                        }

                    }
                }
            });
        }else{
            Picasso.get().load(imageList.get(position))
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.roundedImageView);
            holder.roundedImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        }
    }

    @Override
    public int getItemCount() {

        if (imageList!=null) {
            return imageList.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView roundedImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            roundedImageView = itemView.findViewById(R.id.imageSlide);
        }
    }
}
