package com.example.schoolproject.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Interface.DeleteListener;
import com.example.schoolproject.Interface.LoadText;
import com.example.schoolproject.Models.Item;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;
import com.example.schoolproject.Utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public Boolean savedChecker = false;
    public int checkedPosition = 0;
    Context context;
    ArrayList<Item> items;
    Dialog dialog;
    ArrayList<Listing> mListings, myGigs;
    BottomSheetBehavior bottomSheetBehavior;
    LoadText loadText;
    CallBackListener callBackListener;
    DeleteListener deleteListener;
    String source;


    public ListAdapter(ArrayList<Listing> myGigs, Context context, CallBackListener callBackListener
            ,DeleteListener deleteListener) {
        this.context = context;
        this.myGigs = myGigs;
        this.callBackListener = callBackListener;
        this.deleteListener = deleteListener;
        
    }
    public ListAdapter(ArrayList<Listing> mListings, Context context, String source,CallBackListener callBackListener) {
        this.context = context;
        this.mListings = mListings;
        this.source = source;
        this.callBackListener = callBackListener;

    }

    public ListAdapter(Context context, ArrayList<Item> items, Dialog dialog, LoadText loadText) {
        this.context = context;
        this.items = items;
        this.dialog = dialog;
        this.loadText = loadText;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (items != null) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false), loadText, callBackListener, deleteListener);
        } else if (mListings != null) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gig, parent, false), loadText, callBackListener, deleteListener);
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mygig,
                    parent, false), loadText, callBackListener,deleteListener);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (items != null) {
            Item item = items.get(position);
            holder.bindList(item);
        } else if (mListings != null) {
            Listing listing = mListings.get(position);
            holder.bindGigs(listing);
            save(holder, position);
        } else {
            Listing listing = myGigs.get(position);
            holder.bindMyGigs(listing);
        }
    }

    private void save(ViewHolder holder, int position) {

        Listing listing = mListings.get(position);

        DatabaseReference mSavedRef = database.getReference("Saved").child("Gigs");

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = mUser.getUid();

        final String postKey = listing.getPostKey();
        holder.bookmarkChecker(postKey);

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                savedChecker = true;

                mSavedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (savedChecker.equals(true)) {
                            if (snapshot.child(currentUser).child(postKey).hasChild(currentUser)) {

                                mSavedRef.child(currentUser).child(postKey).removeValue();

                                if (source.equals("SavedGigs")) {
                                    mListings.remove(listing);
                                    notifyDataSetChanged();
                                }
                                savedChecker = false;
                            } else {
                                mSavedRef.child(currentUser).child(postKey).child(currentUser).setValue(true);
                                mSavedRef.child(currentUser).child(postKey).child("postKey").setValue(postKey);

                                savedChecker = false;
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

    @Override
    public int getItemCount() {

        if (items != null) {
            return items.size();
        } else if (myGigs != null) {
            return myGigs.size();
        } else {
            return mListings.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        public TextView mName,mTitle, mDescription, mCategory, mOffer, mTime, mCollege, mLeads;
        public RadioButton radioButton;
        LoadText mLoadText;
        ImageButton btnSave, btnDelete,btnLink;
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference savedRef, mLeadsRef;
        CallBackListener mCallbackListener;
        DeleteListener mDeleteListener;
        int leadsCount = 0;
        public ViewHolder(@NonNull View itemView, LoadText loadText, CallBackListener callBackListener,
                          DeleteListener deleteListener) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.tv_name);
            mTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mDescription = (TextView) itemView.findViewById(R.id.tv_desc);
            mCategory = (TextView) itemView.findViewById(R.id.tv_category);
            mOffer = (TextView) itemView.findViewById(R.id.tv_amount);
            mLeads = (TextView) itemView.findViewById(R.id.tv_leads);
            mTime = (TextView) itemView.findViewById(R.id.tv_time);
            mCollege = (TextView) itemView.findViewById(R.id.tv_college);
            btnSave = itemView.findViewById(R.id.btn_bookmark);
            btnLink = itemView.findViewById(R.id.btn_link);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            radioButton = (RadioButton) itemView.findViewById(R.id.radioButton);
            mLoadText = loadText;
            mCallbackListener = callBackListener;
            mDeleteListener = deleteListener;

            itemView.setOnClickListener(this);
            mLeadsRef = database.getReference().child("Statistics").child("Leads");
        }

        @Override
        public void onClick(View view) {

            if(mListings!=null || myGigs!=null){
                mCallbackListener.callBack(getAdapterPosition());
            }
        }
        public void bookmarkChecker(final String postkey) {
            btnSave = itemView.findViewById(R.id.btn_bookmark);

            savedRef = database.getReference("Saved").child("Gigs");
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            savedRef.addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.child(uid).child(postkey).hasChild(uid)) {
                        btnSave.setImageResource(R.drawable.ic_bookmark_on);
                    } else {
                        btnSave.setImageResource(R.drawable.ic_bookmark);
                    }
                }

                public void onCancelled(DatabaseError error) {
                }
            });
        }

        public void bindMyGigs(Listing listing) {

            mTitle.setText(listing.getName());
            mCollege.setText(listing.getLocation());
            mTime.setText(calculateTimeAgo(listing.getTime()));
            mOffer.setText("Compensation: "+listing.getAmount());
            mCategory.setText(listing.getCategory());
            mDescription.setText(listing.getDescription());

            mLeadsRef.child(listing.getPostKey()).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            leadsCount = (int) snapshot.getChildrenCount();
                            if (leadsCount > 1) {
                                mLeads.setText(Utils.formatValue(leadsCount) + " engagements");
                            } else {
                                mLeads.setText(Utils.formatValue(leadsCount) + " engagement");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mDeleteListener.onDelete(getAdapterPosition());
                }
            });
        }

        public void bindGigs(Listing listing) {

            mCollege.setText(listing.getLocation());
            mTime.setText(calculateTimeAgo(listing.getTime()));
            mTitle.setText(listing.getName());
            mOffer.setText("Compensation: "+listing.getAmount());
            mCategory.setText(listing.getCategory());
            mDescription.setText(listing.getDescription());

            btnLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listing.getLink().startsWith("htt")) {
                        Uri uri = Uri.parse(listing.getLink()); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    }
                }
            });

//                btnChat.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    if (isAppInstalled(context, "com.whatsapp.w4b")) {
//                        openWhatsApp(listing.getSellerPhone(), context);
//                    } else if (isAppInstalled(context, "com.whatsapp")) {
//                        openWhatsApp(listing.getSellerPhone(), context);
//                    } else {
//                        Toast.makeText(context, "Whatsapp not installed on your device", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//            });
        }

//        private boolean isAppInstalled(Context ctx, String packageName) {
//            PackageManager pm = ctx.getApplicationContext().getPackageManager();
//            boolean app_installed;
//            try {
//                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
//                app_installed = true;
//            } catch (PackageManager.NameNotFoundException e) {
//                app_installed = false;
//            }
//            return app_installed;
//        }
//
//        private void openWhatsApp(String phoneNumber, Context context) {
//            try {
//                String url = "https://wa.me/" + phoneNumber;
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse(url));
//                context.startActivity(intent);
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(context, "Unable to open WhatsApp", Toast.LENGTH_SHORT).show();
//            }
//        }

        public void bindList(Item item) {

            if (checkedPosition == -1) {
                radioButton.setChecked(false);
            } else if (checkedPosition == getAdapterPosition()) {
                radioButton.setChecked(true);
            } else {
                radioButton.setChecked(false);
            }

            mName.setText(item.getItem());

            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    radioButton.setChecked(true);

                    if (bottomSheetBehavior != null) {

                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        bottomSheetBehavior.setPeekHeight(0);
                    }

                    if (checkedPosition != getAdapterPosition()) {
                        notifyItemChanged(checkedPosition);
                        checkedPosition = getAdapterPosition();

                        if (dialog != null) {
                                mLoadText.onLoadText(item.getItem());
                                dialog.dismiss();
                        }
                    }else{
                        if (dialog != null) {
                            mLoadText.onLoadText(items.get(0).getItem());
                            dialog.dismiss();
                        }
                    }
                }
            });
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


    }

    public Item getSelectedItem() {

        if (items.size() > 0) {
            if (checkedPosition != -1) {
                return items.get(checkedPosition);
            }
        }
        return null;
    }
}