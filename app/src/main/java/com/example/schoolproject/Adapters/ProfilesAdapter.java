package com.example.schoolproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.Models.UserProfile;
import com.example.schoolproject.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ViewHolder> {

    ArrayList<UserProfile> userProfiles;
    Context context;
    ArrayList<Seller> shopList;
    CallBackListener callBackListener;

    public ProfilesAdapter(Context context, ArrayList<Seller> shopList, CallBackListener callBackListener) {
        this.context = context;
        this.shopList = shopList;
        this.callBackListener = callBackListener;
    }

    public ProfilesAdapter(ArrayList<UserProfile> userProfiles, Context context) {
        this.context = context;
        this.userProfiles = userProfiles;
    }

    public void filteredList(ArrayList<UserProfile> filteredProfiles) {
        userProfiles = filteredProfiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (userProfiles != null) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_profiles, parent, false);
            return new ViewHolder(view, callBackListener);
        } else {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_shop_logo, parent, false);
            return new ViewHolder(view, callBackListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (userProfiles != null) {
            UserProfile userProfile = userProfiles.get(position);

            holder.userProfilesBind(userProfile);
        } else {
            Seller shop = shopList.get(position);
            holder.shopBind(shop);
        }
    }

    @Override
    public int getItemCount() {
        if (userProfiles != null) {
            return userProfiles.size();
        } else {
            return shopList.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name, title;
        RoundedImageView profilePhoto;
        CallBackListener mCallBackListener;

        public ViewHolder(@NonNull View itemView, CallBackListener callBackListener) {
            super(itemView);
            profilePhoto = itemView.findViewById(R.id.profile_photo);
            title = itemView.findViewById(R.id.tv_title);
            name = itemView.findViewById(R.id.tv_name);
            mCallBackListener = callBackListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (callBackListener != null) {
                mCallBackListener.callBack(getAdapterPosition());
            }
        }

        public void shopBind(Seller shop) {

            Picasso.get().load(shop.getProfilePhoto())
                    .into(profilePhoto);
        }

        public void userProfilesBind(UserProfile userProfile) {
                name.setText(userProfile.getDisplayName());
                Picasso.get().load(userProfile.getProfilePhoto())
                        .placeholder(R.drawable.person)
                        .into(profilePhoto);
        }
    }
}
