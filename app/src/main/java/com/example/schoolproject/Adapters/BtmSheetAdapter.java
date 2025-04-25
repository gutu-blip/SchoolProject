package com.example.schoolproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.example.schoolproject.Interface.LoadText;
import com.example.schoolproject.Models.Item;
import com.example.schoolproject.R;

import java.util.ArrayList;

public class BtmSheetAdapter extends RecyclerView.Adapter<BtmSheetAdapter.ViewHolder> {
    public int checkedPosition = 0;
    private FragmentActivity mActivity;
    Context context;
    BottomSheetBehavior bottomSheetBehavior;
    ArrayList<Item> itemList;
    LoadText loadText;

    public BtmSheetAdapter() {

    }
    public BtmSheetAdapter(Context context, BottomSheetBehavior bottomSheetBehavior, ArrayList<Item> itemList) {
        this.context = context;
        this.bottomSheetBehavior = bottomSheetBehavior;
        this.itemList = itemList;
    }

    public BtmSheetAdapter(Context context, BottomSheetBehavior bottomSheetBehavior, ArrayList<Item> itemList,
                           LoadText loadText) {
        this.context = context;
        this.bottomSheetBehavior = bottomSheetBehavior;
        this.itemList = itemList;
        this.loadText = loadText;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false),loadText);

    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindItem(itemList.get(position));
    }

    @Override
    public int getItemCount() {

        return itemList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        ImageView imageView;
        public RadioButton radioButton;
        LoadText mLoadText;

        public ViewHolder(@NonNull View itemView, LoadText loadText) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            imageView = itemView.findViewById(R.id.imageview);
            radioButton = (RadioButton) itemView.findViewById(R.id.radioButton);
            mLoadText = loadText;
        }


        public void bindItem(Item item) {

            if (checkedPosition == -1) {
                radioButton.setChecked(false);
            } else if (checkedPosition == getAdapterPosition()) {
                radioButton.setChecked(true);
            } else {
                radioButton.setChecked(false);
            }

            name.setText(item.getItem());
            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    radioButton.setChecked(true);

                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    bottomSheetBehavior.setPeekHeight(0);

                    if(loadText!=null){
                        mLoadText.onLoadText(item.getItem());
                    }

                    if (checkedPosition != getAdapterPosition()) {
                        notifyItemChanged(checkedPosition);
                        checkedPosition = getAdapterPosition();
                    }
                }
            });
        }
    }

    public Item getSelectedItem() {

        if(itemList!=null) {
            if (itemList.size() != 0) {
                if (checkedPosition != -1) {
                    return itemList.get(checkedPosition);
                }
            }
        }
        return null;
    }
}