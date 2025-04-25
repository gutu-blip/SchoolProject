package com.example.schoolproject.Adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.BuildConfig;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.R;
import com.example.schoolproject.Utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator3;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private FragmentActivity mActivity;
    Context context;
    public List<Uri> mListPhotos;
    RemoveListener removeListener;
    private ArrayList<Listing> savedListings;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    public PhotoAdapter(List<Uri> mListPhotos, Context context, RemoveListener removeListener) {
        this.mListPhotos = mListPhotos;
        this.context = context;
        this.removeListener = removeListener;
    }

    public PhotoAdapter(FragmentActivity mActivity, ArrayList<Listing> savedListings, Context context) {
        this.mActivity = mActivity;
        this.savedListings = savedListings;
        this.context = context;
        registerPermissionLauncher();

    }

    private void registerPermissionLauncher() {
        requestPermissionLauncher = mActivity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (!allGranted) {
                        Toast.makeText(context, "Permission denied.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (mListPhotos != null) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_placeholder, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (mListPhotos != null) {
            Picasso.get()
                    .load(mListPhotos.get(position))
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.imageView);

            holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.mRemoveListener.onRemove(mListPhotos.get(position));
                }
            });
        } else {
            Listing listing = savedListings.get(position);
            holder.bindSavedListing(listing);
        }
    }

    @Override
    public int getItemCount() {

        if (mListPhotos != null) {
            return mListPhotos.size();
        } else {
            return savedListings.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        RemoveListener mRemoveListener;
        ImageButton btnRemove;
        CircleImageView shopPhoto;
        ShimmerFrameLayout shimmerProfilePhoto;
        RatingBar shopRating;
        Button btnChat;
        TextView mCondition,mTime, mRating, mDesc, mPrice, shopName, mName, mRoom, mConditionHeader,
                mCollege, mHostel, mPriceHeader,mCategory;
        DatabaseReference mStatsRef = FirebaseDatabase.getInstance().getReference("Statistics");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        double average = 0.0;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shimmerProfilePhoto = itemView.findViewById(R.id.shimmer_profile_photo);
            shopPhoto = itemView.findViewById(R.id.shop_logo);
            btnChat = itemView.findViewById(R.id.btn_chat);
            mName = itemView.findViewById(R.id.tv_name);
            shopName = itemView.findViewById(R.id.shop_name);
            mPrice = itemView.findViewById(R.id.tv_price);
            mDesc = itemView.findViewById(R.id.tv_desc);
            mCategory = itemView.findViewById(R.id.tv_category);
            mTime = itemView.findViewById(R.id.tv_posted);
            mRating = itemView.findViewById(R.id.tv_rating);
            mRoom = itemView.findViewById(R.id.tv_room);
            mHostel = itemView.findViewById(R.id.tv_hostel);
            mCondition = itemView.findViewById(R.id.tv_condition);
            mConditionHeader = itemView.findViewById(R.id.header_condition);
            mPriceHeader = itemView.findViewById(R.id.header_price);
            mCollege = itemView.findViewById(R.id.tv_college);
            imageView = itemView.findViewById(R.id.imageview);
            mRemoveListener = removeListener;
            shopRating = itemView.findViewById(R.id.rating_bar);
            btnRemove = itemView.findViewById(R.id.btn_remove);

        }

        private void bindSavedListing(Listing listing) {

            ImageView apparelPhoto = itemView.findViewById(R.id.imageview);


            String listingName = listing.getName();
            String condition = listing.getCondition();
            int price = listing.getPrice();
            String desc = listing.getDescription();
            String image = listing.getImageUrl();
            String category = listing.getCategory();
            ArrayList<String> album = listing.getAlbum();

            Picasso.get()
                    .load(image)
                    .placeholder(R.drawable.image_placeholder)
                    .into(apparelPhoto);

            mName.setText(listingName);
            mDesc.setText(desc);
            mCategory.setText(category);
            mTime.setText(Utils.utils.calculateTimeAgo(listing.getTime()));

            if(!TextUtils.isEmpty(listing.getRoom())){
                itemView.findViewById(R.id.header_room).setVisibility(View.VISIBLE);
                mRoom.setVisibility(View.VISIBLE);
                mRoom.setText(listing.getRoom());
            }
            if(!TextUtils.isEmpty(listing.getHostel())){
                itemView.findViewById(R.id.header_hostel).setVisibility(View.VISIBLE);
                mHostel.setVisibility(View.VISIBLE);
                mHostel.setText(listing.getHostel());
            }

            if (listing.getTrade().equals("Products")) {
                mConditionHeader.setVisibility(View.VISIBLE);
                mCondition.setVisibility(View.VISIBLE);
                mConditionHeader.setText("Condition");
                mCondition.setText(condition);

                mPriceHeader.setVisibility(View.VISIBLE);
                mPrice.setVisibility(View.VISIBLE);
                mPrice.setText(Integer.toString(price));
            } else {
                mConditionHeader.setVisibility(View.VISIBLE);
                mCondition.setVisibility(View.VISIBLE);
                mConditionHeader.setText("Rates");
                mCondition.setText(listing.getRates());

                mPriceHeader.setVisibility(View.INVISIBLE);
                mPrice.setVisibility(View.INVISIBLE);
            }

            if (listing.getUploadType().equals("album")) {
                itemView.findViewById(R.id.cv_album).setVisibility(View.VISIBLE);
            } else {
                itemView.findViewById(R.id.cv_album).setVisibility(View.GONE);
            }

            final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (listing.getListingSource().equals("shop")) {
                FirebaseDatabase.getInstance().getReference("Profiles").child(listing.getUserID()).child("Shops").child(listing.getSellerKey())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                Seller seller = snapshot.child("Profile").getValue(Seller.class);

                                if (seller != null) {
                                    mCollege.setText(seller.getCollege());
                                    shopName.setText(seller.getName());

                                    shimmerProfilePhoto.setVisibility(View.INVISIBLE);
                                    Picasso.get()
                                            .load(seller.getProfilePhoto())
                                            .placeholder(R.drawable.image_placeholder)
                                            .into(shopPhoto);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } else {
                mCollege.setText(listing.getLocation());
                shopName.setText(listing.getSellerName());
                shimmerProfilePhoto.setVisibility(View.INVISIBLE);

                Picasso.get()
                        .load(listing.getSellerPhoto())
                        .placeholder(R.drawable.image_placeholder)
                        .into(shopPhoto);
            }


            if(listing.getListingSource().equals("individual")){
                mRating.setVisibility(View.INVISIBLE);
                shopRating.setVisibility(View.INVISIBLE);
            }else {
                getRating(listing);
            }
            Glide.with(context).asBitmap().load(listing.getImageUrl())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                            btnChat.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {

                                    requestPermission(resource, currentUser, listing);

                                }
                            });

                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

            Dialog dialog = new Dialog(context);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            dialog.setContentView(R.layout.dialog_album);

            Button btnClose = dialog.findViewById(R.id.btn_dismiss);

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            apparelPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listing.getUploadType().equals("album")) {
                        dialog.show();
                    }
                }
            });

            ViewPager2 viewPager2 = dialog.findViewById(R.id.viewpager2);
            CircleIndicator3 indicator = (CircleIndicator3) dialog.findViewById(R.id.indicator);

            if (listing.getUploadType().equals("album")) {

                VPSliderAdapter adapter = new VPSliderAdapter(mActivity, album);
                viewPager2.setAdapter(adapter);

                indicator.setViewPager(viewPager2);
                adapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

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
        }


        public void getRating(Listing listing) {

            try {
                final DatabaseReference dbRef = database.getReference("Profiles").child(listing.getUserID())
                        .child("Shops").child(listing.getSellerKey()).child("Ratings");

                final DatabaseReference ratingRef = database.getReference("Profiles").child(listing.getUserID())
                        .child("Shops").child(listing.getSellerKey()).child("Profile").child("rating");

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
                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
            }
        }


        private void requestPermission(Bitmap bitmap, String userId, Listing listing) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                shareImage(bitmap, userId, listing);
            } else {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
            }
        }

        private void shareImage(Bitmap bitmap, String userId, Listing listing) {
            try {
                Uri imageUri;
                OutputStream outputStream;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "listing_image.jpg");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                    Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    outputStream = context.getContentResolver().openOutputStream(uri);
                    imageUri = uri;
                } else {
                    String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    File image = new File(imagePath, "listing_image.jpg");
                    outputStream = new FileOutputStream(image);
                    imageUri = Uri.fromFile(image);
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this listing from Ace!\n" + listing.getName());
                context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            } catch (Exception e) {
                Toast.makeText(context, "Failed to share image.", Toast.LENGTH_SHORT).show();
            }
        }
        private void chat(Bitmap resource, String currentUser, Listing listing) {
            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);

            mStatsRef.child("Leads").child(listing.getPostKey()).child(currentUser).child("name")
                    .setValue(signInAccount.getDisplayName());
            mStatsRef.child("Leads").child(listing.getPostKey()).child(currentUser).child("userId").
                    setValue(currentUser);

            if (isAppInstalled(context, "com.whatsapp.w4b")) {

                try {
                    saveImageToGallery(resource, listing, listing.getName());
                    openWhatsApp(listing.getSellerPhone());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (isAppInstalled(context, "com.whatsapp")) {

                try {
                    saveImageToGallery(resource, listing, listing.getName());
                    openWhatsApp(listing.getSellerPhone());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "Whatsapp not installed on your device", Toast.LENGTH_SHORT).show();
            }

        }

        private void saveImageToGallery(Bitmap bitmap, Listing listing, @NonNull String name) throws IOException {
            boolean saved;
            OutputStream fos;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/" + "CladeInterests");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).toString() + File.separator + "CladeInterests";

                File file = new File(imagesDir);

                if (!file.exists()) {
                    file.mkdir();
                }

                File image = new File(imagesDir, name + ".png");
                fos = new FileOutputStream(image);

                scanner(image.getAbsolutePath(), listing);

            }

            saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        }

        private void scanner(String path, Listing listing) {

            MediaScannerConnection.scanFile(context,
                    new String[]{path}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {

                        public void onScanCompleted(String path, Uri uri) {

                            openWhatsApp(listing.getSellerPhone());
                        }
                    });
        }

        private void openWhatsApp(String number) {
            try {
                number = number.replace(" ", "").replace("+", "");

                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
                sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(number) + "@s.whatsapp.net");
                context.startActivity(sendIntent);

            } catch (Exception e) {
            }
        }

        private boolean isAppInstalled(Context ctx, String packageName) {
            PackageManager pm = ctx.getApplicationContext().getPackageManager();
            boolean app_installed;
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                app_installed = true;
            } catch (PackageManager.NameNotFoundException e) {
                app_installed = false;
            }
            return app_installed;
        }

    }

    private static Uri saveImage(Bitmap image, Context context) {

        File imagefolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imagefolder.mkdirs();
            File file = new File(imagefolder, "shared_images.jpg");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.flush();
            stream.close();

            uri = FileProvider.getUriForFile(Objects.requireNonNull(context.getApplicationContext()),
                    BuildConfig.APPLICATION_ID + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }


    public interface RemoveListener {
        void onRemove(Uri uri);
    }
}