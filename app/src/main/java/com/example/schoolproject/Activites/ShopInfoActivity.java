package com.example.schoolproject.Activites;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.schoolproject.Adapters.BtmSheetAdapter;
import com.example.schoolproject.Adapters.ListAdapter;
import com.example.schoolproject.Interface.LoadText;
import com.example.schoolproject.Models.Item;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.R;
import com.example.schoolproject.Utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class ShopInfoActivity extends AppCompatActivity implements LoadText {

    static final String OPENSHOP = "OpenShop";
    static final String UPDATEPROFILE = "UpdateProfile";
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    TextInputEditText mShopName, mCollege, mRoom, mDescription, mMantra, mTrade, mPhone, mEmail, mHostel;
    TextInputLayout tilCategory, tilTrade;
    CircleImageView mProfilePhoto;
    private Uri mImageUri;
    Context context = ShopInfoActivity.this;
    private StorageReference mStorageRef;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    public DatabaseReference mDatabaseRef, mSubcategoriesRef, mUserProfileRef, mCategoriesRef;
    public StorageTask mUploadTask;
    public TextView mHeader;
    public ProgressBar mProgressBar;
    Button mSubmit, btnGo;
    String name, description, college, email, phone, hostel = "", room = "", trade = "", mantra = "";
    Seller shop;
    String operation, currentUser, filler;
    CoordinatorLayout coordinatorLayout;
    boolean image = false;
    BottomSheetBehavior bottomSheetBehavior;
    private BtmSheetAdapter btmSheetAdapter = new BtmSheetAdapter();
    private RecyclerView btmRecyclerview, dialogRecyclerView;
    private AutoCompleteTextView mSearch;
    private ProgressBar btmProgressCircle;
    ArrayList<Item> paramlist = new ArrayList();
    private ArrayList<String> mCategories, mColleges;
    private ArrayList<Item> itemList;
    private Dialog dialog;
    boolean isCreated = false;
    SweetAlertDialog sweetAlert;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_shop_info);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initWidgets();
        init();
        fetchCategories();
        initBtmSheet();
        setListeners();
    }

    private void initBtmSheet() {

        initBottomSheetWidgets();

        bottomSheetBehavior.setPeekHeight(0);
        //set your sheet hideable or not
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setPeekHeight(0);
                    bottomSheetBehavior.setHideable(false);

                    if (btmSheetAdapter.getSelectedItem() != null) {
                        String item = btmSheetAdapter.getSelectedItem().getItem();

                    }
                } else {
                    bottomSheetBehavior.setHideable(true);

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void initBottomSheetWidgets() {
        RelativeLayout bottomsheetLayout = findViewById(R.id.bottom_sheet_recycler);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        btmRecyclerview = bottomsheetLayout.findViewById(R.id.recyclerview);
        btmProgressCircle = bottomsheetLayout.findViewById(R.id.progress_circle);
        btmRecyclerview.setLayoutManager(new LinearLayoutManager(ShopInfoActivity.this,
                LinearLayoutManager.VERTICAL, false));

        FloatingActionButton fabCancel = bottomsheetLayout.findViewById(R.id.fab_cancel);

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        mSearch = bottomsheetLayout.findViewById(R.id.et_search);
    }

    @Override
    public void onBackPressed() {

        if (bottomSheetBehavior.isHideable()) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetBehavior.setPeekHeight(0);
        } else {
            super.onBackPressed();
        }
    }

    public ArrayList<String> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private void fetchCategories() {

        mCategoriesRef.child(trade).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot keySnapshot : snapshot.getChildren()) {
                    String category = keySnapshot.child("category").getValue(String.class);
                    mCategories.add(category);
                }

                for (String material : mCategories) {
                    itemList.add(new Item(material, false));
                }

                btmSheetAdapter = new BtmSheetAdapter(ShopInfoActivity.this, bottomSheetBehavior, itemList);
                btmRecyclerview.setAdapter(btmSheetAdapter);

                btmProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public ArrayList<Item> setLists(String parameter) {

        paramlist.clear();

        if (parameter.equals("college")) {
            for (String name : mColleges) {
                paramlist.add(new Item(name, false));
            }
            return paramlist;
        } else {
            for (String name : mCategories) {
                paramlist.add(new Item(name, false));
            }
            return paramlist;
        }
    }

    private void init() {

        itemList = new ArrayList<>();
        currentUser = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("Profile_Photos");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Profiles");
        mDatabaseRef.keepSynced(true);

        mUserProfileRef = FirebaseDatabase.getInstance().getReference("Profiles").child(currentUser);
        mUserProfileRef.keepSynced(true);

        mSubcategoriesRef = FirebaseDatabase.getInstance().getReference("Metadata").child("SubCategories");
        mSubcategoriesRef.keepSynced(true);

        mCategoriesRef = database.getReference("Listings_Categories");
        mCategoriesRef.keepSynced(true);

        shop = getIntent().getParcelableExtra("shop");

        if (trade.equals("Products")) {
            mCategories = getArrayList("product");
        } else {
            mCategories = getArrayList("service");
        }
        mColleges = getArrayList("colleges");

        if (shop == null) {
            findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        }
        operation = getIntent().getStringExtra("operation");
        if (operation.equals("OpenShop")) {
            mHeader.setText("Open Shop");
            mSubmit.setText("Create Shop");
            mPhone.setHint("07*******");

            Picasso.get()
                    .load(R.drawable.image_placeholder)
                    .into(mProfilePhoto);

        } else {

            mHeader.setText("Update Profile");
            mSubmit.setText("Save");

            TextInputLayout tilPhone = findViewById(R.id.til_phone);
            tilPhone.setHint("Phone");
            String phone = shop.getPhone();

            mPhone.setText(phone);
            mShopName.setText(shop.getName());
            mEmail.setText(shop.getEmail());
            mMantra.setText(shop.getMantra());
            mCollege.setText(shop.getCollege());
            mRoom.setText(shop.getRoom());
            mHostel.setText(shop.getHostel());
            mDescription.setText(shop.getDescription());
            mTrade.setText(shop.getTrade());

            Picasso.get().load(shop.getProfilePhoto()).into(mProfilePhoto);

        }

        dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.dialog_list);

        dialog.setCanceledOnTouchOutside(false);
        btnGo = dialog.findViewById(R.id.btn_go);
        dialogRecyclerView = dialog.findViewById(R.id.recyclerview);
        dialogRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

    }

    private boolean doesEmailExist(DataSnapshot snapshot, String registeringEmail) {

        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            for (DataSnapshot shopKeySnapshot : userSnapshot.child("Shops").getChildren()) {
                Seller shop = shopKeySnapshot.child("Profile").getValue(Seller.class);
                if (shop.getEmail() != null) {
                    if (shop.getEmail().toLowerCase().equals(registeringEmail.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setListeners() {

        mCollege.setEnabled(true);
        mCollege.setTextIsSelectable(true);
        mCollege.setFocusable(false);
        mCollege.setFocusableInTouchMode(false);
        mCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                setAdapter(setLists("college"));

                mSearch.setHint("Type and click add button to add a new university");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setHideable(false);
                filler = "college";
                mSearch.setText("");
            }
        });

        mTrade.setEnabled(true);
        mTrade.setTextIsSelectable(true);
        mTrade.setFocusable(false);
        mTrade.setFocusableInTouchMode(false);
        mTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filler = "trade";
                dialog.show();

                ArrayList<String> options = new ArrayList<>();

                options.add("Products");
                options.add("Services");

                itemList.clear();

                for (String option : options) {
                    itemList.add(new Item(option, false));
                }
                ListAdapter listAdapter = new ListAdapter(context, itemList, dialog, ShopInfoActivity.this);
                dialogRecyclerView.setAdapter(listAdapter);

            }
        });

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ShopInfoActivity.this, ProfileShopActivity.class);
                intent.putExtra("shop", shop);

                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.fab_edit).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openFileChooser();
            }
        });

        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = editable.toString().trim();

                        if (doesEmailExist(snapshot, email)) {
                            Snackbar.make(coordinatorLayout, "Email already in use", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });


        mSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (isCreated == false) {

                    sweetAlert = new SweetAlertDialog(ShopInfoActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                    sweetAlert.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    sweetAlert.setTitleText("Uploading...");
                    sweetAlert.setCancelable(false);
                    sweetAlert.getProgressHelper().setRimColor(Color.parseColor("#130822"));

                    name = mShopName.getText().toString().trim();
                    college = mCollege.getText().toString().trim();
                    email = mEmail.getText().toString().trim();
                    phone = mPhone.getText().toString().trim();
                    hostel = mHostel.getText().toString().trim();
                    room = mRoom.getText().toString().trim();
                    mantra = mMantra.getText().toString().trim();
                    trade = mTrade.getText().toString();
                    description = mDescription.getText().toString();

                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(college) && !TextUtils.isEmpty(phone)
                            && Utils.utils.isEmailValid(email) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(trade)) {

                        if (operation.equals("UpdateProfile")) {

                            if (email.equals(shop.getEmail())) {
                                if (mUploadTask == null || !mUploadTask.isInProgress()) {
                                    uploadFile();
                                } else {
                                    Toast.makeText(ShopInfoActivity.this, "Updating profile", Toast.LENGTH_SHORT).show();
                                    mSubmit.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (doesEmailExist(snapshot, email)) {
                                            Snackbar.make(coordinatorLayout, "Email already in use", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        } else {
                                            if (mUploadTask == null || !mUploadTask.isInProgress()) {
                                                uploadFile();
                                            } else {
                                                Toast.makeText(ShopInfoActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                                                mSubmit.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        } else {
                            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (doesEmailExist(snapshot, email)) {
                                        Snackbar.make(coordinatorLayout, "Email already in use", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    } else if (mImageUri == null) {
                                        Snackbar.make(coordinatorLayout, "A profile picture is required", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    } else {
                                        if (mUploadTask == null || !mUploadTask.isInProgress()) {
                                            uploadFile();
                                        } else {
                                            Toast.makeText(ShopInfoActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                                            mSubmit.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }
                    } else {
                        if (TextUtils.isEmpty(college)) {
                            Snackbar.make(coordinatorLayout, "University is required", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else if (TextUtils.isEmpty(name)) {
                            Snackbar.make(coordinatorLayout, "Shop name is required", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else if (TextUtils.isEmpty(trade)) {
                            Snackbar.make(coordinatorLayout, "What are you selling?", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else if (TextUtils.isEmpty(phone)) {
                            Snackbar.make(coordinatorLayout, "Whatsapp number is required", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else if (TextUtils.isEmpty(email)) {
                            Snackbar.make(coordinatorLayout, "Email is required", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else if (TextUtils.isEmpty(description)) {
                            Snackbar.make(coordinatorLayout, "Description is required", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                } else {
                    Snackbar.make(coordinatorLayout, "Try again later", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private void setAdapter(ArrayList<Item> list) {

        btmSheetAdapter = new BtmSheetAdapter(context, bottomSheetBehavior, list, ShopInfoActivity.this);
        SlideInBottomAnimationAdapter alphaInAnimationAdapter = new SlideInBottomAnimationAdapter(btmSheetAdapter);
        alphaInAnimationAdapter.setDuration(1000);
        alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        alphaInAnimationAdapter.setFirstOnly(false);

        btmRecyclerview.setAdapter(alphaInAnimationAdapter);
        btmSheetAdapter.notifyDataSetChanged();
    }

    private void initWidgets() {

        tilCategory = findViewById(R.id.til_category);
        tilTrade = findViewById(R.id.til_trade);
        mHeader = findViewById(R.id.header);
        coordinatorLayout = findViewById(R.id.coordinator);
        mPhone = findViewById(R.id.et_phone);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProfilePhoto = findViewById(R.id.profile_photo);
        mShopName = findViewById(R.id.et_name);
        mCollege = findViewById(R.id.et_college);
        mMantra = findViewById(R.id.et_catchphrase);
        mRoom = findViewById(R.id.et_room);
        mEmail = findViewById(R.id.et_email);
        mHostel = findViewById(R.id.et_hostel);
        mSubmit = findViewById(R.id.btn_save);
        mTrade = findViewById(R.id.et_trade);
        mDescription = findViewById(R.id.et_description);
    }

    public void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1 && data != null && data.getData() != null) {
            mImageUri = data.getData();
            image = true;
            Picasso.get().load(mImageUri).into(mProfilePhoto);
        }
    }

    private String getFileExtension(Uri uri) {
        if (image == true) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
        } else {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(getUriToDrawable(context, R.drawable.person)));
        }
    }

    public void uploadFile() {
        final String time = new SimpleDateFormat("dd-MMMM-yyyy").format(Calendar.getInstance().getTime()) + ":" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

        if (this.mImageUri != null) {
            final StorageReference fileReference = this.mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(this.mImageUri));

            mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener((OnSuccessListener) new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                public void onSuccess(final Uri uri) {


                                    if (operation.equals(OPENSHOP)) {
                                        sweetAlert.getProgressHelper().stopSpinning();
                                        sweetAlert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                        sweetAlert.setTitleText("Shop Created");

                                        isCreated = true;

                                        String shopKey = mDatabaseRef.push().getKey();

                                        Seller sellerProfile = new Seller(uri.toString(), "0", college, mantra, email, phone,
                                                name, currentUser, time, shopKey, hostel, room, "shop", trade, description);

                                        mDatabaseRef.child(currentUser).child("Shops").child(shopKey).child("Profile").setValue(sellerProfile);

                                    } else {
                                        sweetAlert.getProgressHelper().stopSpinning();
                                        sweetAlert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                        sweetAlert.setTitleText("Profile up-to-date ");

                                        isCreated = true;

                                        Seller sellerProfile = new Seller(uri.toString(), "0", college, mantra, email, phone,
                                                name, currentUser, time, shop.getSellerKey(), hostel, room, "shop", trade, description);

                                        mDatabaseRef.child(currentUser).child("Shops").child(shop.getSellerKey()).child("Profile").setValue(sellerProfile);

                                    }
                                }
                            });
                        }
                    }).
                    addOnFailureListener((OnFailureListener) new OnFailureListener() {
                        public void onFailure(Exception e) {
                            sweetAlert.getProgressHelper().stopSpinning();
                            sweetAlert.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            sweetAlert.setTitleText("Error");
                            sweetAlert.setContentText(e.getMessage());
                        }
                    }).

                    addOnProgressListener((OnProgressListener) new OnProgressListener<UploadTask.TaskSnapshot>() {
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            sweetAlert.show();
                        }
                    });
        } else {
            Glide.with(this).asBitmap().load(shop.getProfilePhoto())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                            updateShopProfile(resource);

                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

        }
    }

    private void updateShopProfile(Bitmap bitmap) {

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        Uri mImageUri;

        mImageUri = saveImage(bitmap, getApplicationContext());
        final String time = new SimpleDateFormat("dd-MMMM-yyyy").format(Calendar.getInstance().getTime()) + ":" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

        final StorageReference fileReference = this.mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

        mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener((OnSuccessListener) new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            public void onSuccess(final Uri uri) {
                                sweetAlert.getProgressHelper().stopSpinning();
                                sweetAlert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                sweetAlert.setTitleText("Profile up-to-date ");

                                isCreated = true;

                                Seller sellerProfile = new Seller(uri.toString(), "0", college, mantra, email, phone,
                                        name, currentUser, time, shop.getSellerKey(), hostel, room, "shop", trade, description);

                                mDatabaseRef.child(currentUser).child("Shops").child(shop.getSellerKey()).child("Profile").setValue(sellerProfile);

                            }
                        });
                    }
                }).

                addOnFailureListener((OnFailureListener) new
                        OnFailureListener() {
                            public void onFailure(Exception e) {
                                sweetAlert.getProgressHelper().stopSpinning();
                                sweetAlert.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                sweetAlert.setTitleText("Error");
                                sweetAlert.setContentText(e.getMessage());                            }
                        }).
                addOnProgressListener((OnProgressListener) new OnProgressListener<UploadTask.TaskSnapshot>() {
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        sweetAlert.show();
                    }
                });

    }

    public static final Uri getUriToDrawable(@NonNull Context context,
                                             @AnyRes int drawableId) {
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
        return imageUri;
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
                    context.getPackageName() + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    @Override
    public void onLoadText(String text) {
        if (filler.equals("trade")) {
            mTrade.setText(text);
            trade = text;
        } else {
            mCollege.setText(text);
            college = text;
        }
    }
}
