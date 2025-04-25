package com.example.schoolproject.Activites;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.schoolproject.Adapters.BtmSheetAdapter;
import com.example.schoolproject.Adapters.ListAdapter;
import com.example.schoolproject.Adapters.PhotoAdapter;
import com.example.schoolproject.Interface.LoadText;
import com.example.schoolproject.Models.Item;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.R;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ProductListing extends AppCompatActivity implements PhotoAdapter.RemoveListener,
        EasyPermissions.PermissionCallbacks, LoadText {

    private static final String PREFS_HOSTEL = "hostel";
    private static final String PREFS_ROOM = "room";
    private static final String PREFS_COLLEGE = "college";
    private static final String PREFS_PHONE = "phone";

    EditText mName, mDescription, mHostel, mRoom, mPrice, mPhone;
    TextInputEditText mCategory, mLocation, mCondition;
    Button btnPost;
    ImageButton btnAdd;
    FloatingActionButton fabAdd;
    BottomSheetBehavior bottomSheetBehavior;
    ProgressBar btmProgressCircle;
    RecyclerView mRecyclerView;
    private RecyclerView btmRecyclerview, dialogRecyclerview;
    private BtmSheetAdapter btmSheetAdapter = new BtmSheetAdapter();
    AutoCompleteTextView mSearch;
    PhotoAdapter photoAdapter;
    ArrayList<Uri> imageList;
    DatabaseReference mListingsRef, mCategoriesRef, mCollegesRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    public StorageTask mUploadTask;
    private StorageReference mStorageRef;
    private String currentUser;
    Dialog dialog;
    ListAdapter listAdapter;
    List<String> conditions = Arrays.asList("New", "Fairly Used", "Slightly Used");
    ArrayList<String> mColleges = new ArrayList<>();
    ArrayList<String> mCategories = new ArrayList<>();
    private ArrayList<Item> categoryList, collegeList;
    private int Price, i = 0;
    private String listingSource = "";
    String sellerKey = "", sellerPhone = "", sellerPhoto = "", sellerName = "";
    String description, name, condition, location, prevLocation = "",prevCategory = "", room = "", hostel = "", price, category, phone;
    Context context = ProductListing.this;
    private CoordinatorLayout parentView;
    private String filler = "";
    Seller shop;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    SweetAlertDialog sweetAlert;

    private static final int REQUEST_CODE_PICK_IMAGES = 1001;
    private static final int MAX_IMAGE_SELECTION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_listing);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initWidgets();
        initBottomSheetWidgets();
        initBtmSheet();
        setItems();
        setListeners();
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

    private void setListeners() {

        mCategory.setEnabled(true);
        mCategory.setTextIsSelectable(true);
        mCategory.setFocusable(false);
        mCategory.setFocusableInTouchMode(false);
        mCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                categoryList.clear();
                for (String item : mCategories) {
                    categoryList.add(new Item(item, false));
                }

                BtmSheetAdapter mAdapter = new BtmSheetAdapter(context, bottomSheetBehavior, categoryList, ProductListing.this);
                btmRecyclerview.setAdapter(mAdapter);

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setHideable(false);
                mSearch.setHint("Type and click add button to add a new category");

                filler = "category";
                mSearch.setText("");
            }
        });

        mLocation.setEnabled(true);
        mLocation.setTextIsSelectable(true);
        mLocation.setFocusable(false);
        mLocation.setFocusableInTouchMode(false);
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                collegeList.clear();
                for (String item : mColleges) {
                    collegeList.add(new Item(item, false));
                }

                BtmSheetAdapter mAdapter = new BtmSheetAdapter(context, bottomSheetBehavior, collegeList, ProductListing.this);
                btmRecyclerview.setAdapter(mAdapter);

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setHideable(false);
                mSearch.setHint("Type and click add button to add a new university");

                filler = "college";
                mSearch.setText("");

            }
        });


        mCondition.setEnabled(true);
        mCondition.setTextIsSelectable(true);
        mCondition.setFocusable(false);
        mCondition.setFocusableInTouchMode(false);
        mCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filler = "condition";
                dialog.show();

                ArrayList<Item> list = new ArrayList<>();

                for (String condition : conditions) {
                    list.add(new Item(condition, false));
                }

                listAdapter = new ListAdapter(context, list, dialog, ProductListing.this);
                dialogRecyclerview.setAdapter(listAdapter);

            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = 0;
                sweetAlert = new SweetAlertDialog(ProductListing.this, SweetAlertDialog.PROGRESS_TYPE);
                sweetAlert.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                sweetAlert.setTitleText("Uploading...");
                sweetAlert.setCancelable(false);
                sweetAlert.getProgressHelper().setRimColor(Color.parseColor("#130822"));

                description = mDescription.getText().toString();
                name = mName.getText().toString().trim();
                condition = mCondition.getText().toString();

                if (listingSource.equals("individual")) {
                    location = mLocation.getText().toString().trim();
                    editor.putString(PREFS_COLLEGE, location);
                    editor.apply();

                    hostel = mHostel.getText().toString().trim();
                    editor.putString(PREFS_HOSTEL, hostel);
                    editor.apply();

                    room = mRoom.getText().toString().trim();
                    editor.putString(PREFS_ROOM, room);
                    editor.apply();

                    phone = mPhone.getText().toString().trim();
                    editor.putString(PREFS_PHONE, phone);
                    editor.apply();
                } else {
                    room = shop.getRoom();
                    hostel = shop.getHostel();
                    phone = shop.getPhone();
                    location = shop.getCollege();
                }
                price = mPrice.getText().toString().trim();
                category = mCategory.getText().toString().trim();

                if (!TextUtils.isEmpty(description) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone)
                        && !TextUtils.isEmpty(location) && !TextUtils.isEmpty(condition) &&
                        !TextUtils.isEmpty(price) && !TextUtils.isEmpty(category) && imageList.size() > 0) {

                    if (mUploadTask == null || !mUploadTask.isInProgress()) {
                        uploadData();
                    } else {
                        Snackbar.make(parentView, "Upload in progress", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    if (TextUtils.isEmpty(price)) {
                        Snackbar.make(parentView,
                                "Price is required", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else if (TextUtils.isEmpty(description)) {
                        Snackbar.make(parentView, "Description is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    } else if (TextUtils.isEmpty(condition)) {
                        Snackbar.make(parentView, "Condition is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    } else if (TextUtils.isEmpty(phone)) {
                        Snackbar.make(parentView, "Phone Number is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    } else if (TextUtils.isEmpty(name)) {
                        Snackbar.make(parentView, "Name is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    } else if (TextUtils.isEmpty(category)) {
                        Snackbar.make(parentView, "Category is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    } else if (imageList.size() == 0) {
                        Snackbar.make(parentView, "At least one photo is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            }
        });
    }

    private void uploadData() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(ProductListing.this);

        if (mPrice.getText().toString().length() != 0) {
            Price = Integer.parseInt(mPrice.getText().toString());
        }
        String key = this.mListingsRef.push().getKey();

        if (listingSource.equals("shop")) {
            sellerKey = shop.getSellerKey();
            sellerPhone = shop.getPhone();
            sellerPhoto = shop.getProfilePhoto();
            sellerName = shop.getName();
        } else {
            sellerName = signInAccount.getDisplayName();
            sellerKey = currentUser;
            sellerPhoto = signInAccount.getPhotoUrl().toString();
            sellerPhone = phone;
        }

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
        final String strDate = formatter.format(date);

        if (imageList.size() == 1) {
            for (Uri mImageUri : imageList) {
                if (mImageUri != null) {

                    StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." +
                            getFileExtension(mImageUri));

                    mUploadTask = fileReference.putFile(mImageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            if (!prevCategory.toLowerCase().equals(category.toLowerCase())) {
                                                if (!doesCategoryExist()) {
                                                    database.getReference("Categories").child("Products").child(key).child("title").setValue(category);
                                                }
                                            }
                                            if (!prevLocation.toLowerCase().equals(location.toLowerCase())) {
                                                if (!doesCollegeExist()) {
                                                    database.getReference("Colleges").child(key).child("name").setValue(location);
                                                }
                                            }

                                            Listing listing = new Listing(name, description, category, strDate, key, listingSource,
                                                    "Products", currentUser, location, hostel, room, condition,
                                                    uri.toString(), "singles", sellerKey, sellerName, sellerPhone.replaceFirst("0", "+254"),
                                                    sellerPhoto, Price, "");

                                            mListingsRef.child("Products").child(location).child(key).child("Details").setValue(listing);

                                            sweetAlert.getProgressHelper().stopSpinning();
                                            sweetAlert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            sweetAlert.setTitleText("Upload Successful");
                                            prevLocation = location;
                                            prevCategory = category;

                                        }
                                    });
                                }
                            }).addOnFailureListener((OnFailureListener) new OnFailureListener() {
                                public void onFailure(Exception e) {
                                    sweetAlert.getProgressHelper().stopSpinning();
                                    sweetAlert.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                    sweetAlert.setTitleText("Error");
                                    sweetAlert.setContentText(e.getMessage());
                                }
                            }).addOnProgressListener((OnProgressListener) new OnProgressListener<UploadTask.TaskSnapshot>() {
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    sweetAlert.show();
                                }
                            });
                }
            }
        } else if (imageList.size() > 1) {

            for (Uri imageUri : imageList) {
                if (imageUri != null) {

                    final StorageReference fileReference = this.mStorageRef.child(System.currentTimeMillis() + "." +
                            getFileExtension(imageUri));

                    mUploadTask = fileReference.putFile(imageUri).addOnSuccessListener((OnSuccessListener) new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                public void onSuccess(Uri uri) {

                                    String imageKey = mListingsRef.push().getKey();

                                    mListingsRef.child("Products").child(location).child(key).
                                            child("Album").child(imageKey).child("imageUrl").setValue(uri.toString());

                                    if (i == 0) {
                                        if (!prevCategory.toLowerCase().equals(category.toLowerCase())) {
                                            if (!doesCategoryExist()) {
                                                database.getReference("Categories").child("Products").child(key).child("title").setValue(category);
                                            }
                                        }
                                        if (!prevLocation.toLowerCase().equals(location.toLowerCase())) {
                                            if (!doesCollegeExist()) {
                                                database.getReference("Colleges").child(key).child("name").setValue(location);
                                            }
                                        }

                                        Listing marketPlace = new Listing(name, description, category,
                                                strDate, key, listingSource, "Products", currentUser, location, hostel, room, condition,
                                                uri.toString(), "album", sellerKey, sellerName, sellerPhone.replaceFirst("0", "+254"),
                                                sellerPhoto, Price, "");

                                        mListingsRef.child("Products").child(location).child(key).child("Details").setValue(marketPlace);


                                    }
                                    if (i == imageList.size() - 1) {
                                        sweetAlert.getProgressHelper().stopSpinning();
                                        sweetAlert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                        sweetAlert.setTitleText("Upload Successful");
                                        prevLocation = location;
                                        prevCategory = category;
                                    }

                                    i++;
                                }
                            });
                        }
                    }).addOnFailureListener((OnFailureListener) new OnFailureListener() {
                        public void onFailure(Exception e) {
                            sweetAlert.getProgressHelper().stopSpinning();
                            sweetAlert.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            sweetAlert.setTitleText("Error");
                            sweetAlert.setContentText(e.getMessage());
                        }
                    }).addOnProgressListener((OnProgressListener) new OnProgressListener<UploadTask.TaskSnapshot>() {
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            sweetAlert.show();
                        }
                    });
                }
            }
        }
    }

    private boolean doesCategoryExist() {
        for (String cat : mCategories) {
            if (cat.trim().toLowerCase().equals(category.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean doesCollegeExist() {
        for (String university : mColleges) {
            if (university.trim().toLowerCase().equals(location.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private void setItems() {
        listingSource = getIntent().getStringExtra("listingSource");

        editor = getSharedPreferences("preferences", MODE_PRIVATE).edit();
        prefs = getSharedPreferences("preferences", MODE_PRIVATE);

        mCategories = getArrayList("product");
        mColleges = getArrayList("colleges");

        if (listingSource.equals("individual")) {

            String prefsHostel = prefs.getString(PREFS_HOSTEL, "");
            String prefsRoom = prefs.getString(PREFS_ROOM, "");
            String prefsCollege = prefs.getString(PREFS_COLLEGE, "");
            String prefsPhone = prefs.getString(PREFS_PHONE, "");

            mLocation.setText(prefsCollege);
            mHostel.setText(prefsHostel);
            mRoom.setText(prefsRoom);
            mPhone.setText(prefsPhone);

        } else {
            shop = getIntent().getParcelableExtra("shop");

            findViewById(R.id.til_location).setVisibility(View.GONE);
            findViewById(R.id.til_hostel).setVisibility(View.GONE);
            findViewById(R.id.til_room).setVisibility(View.GONE);
            findViewById(R.id.til_phone).setVisibility(View.GONE);

        }

        imageList = new ArrayList<>();
        mStorageRef = FirebaseStorage.getInstance().getReference("Listings");
        mCategoriesRef = database.getReference("Categories");
        mCollegesRef = database.getReference("Colleges");
        currentUser = mUser.getUid();
        mListingsRef = database.getReference("Listings");
        categoryList = new ArrayList<>();
        collegeList = new ArrayList<>();

        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.dialog_list);

        dialogRecyclerview = dialog.findViewById(R.id.recyclerview);
        dialogRecyclerview.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));

    }

    private void initBtmSheet() {

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

                        filler = "";
                        if (filler.equals("category")) {
                            mCategory.setText(item);
                        } else if (filler.equals("college")) {
                            mLocation.setText(item);

                            if (listingSource.equals("individual")) {
                                editor.putString(PREFS_COLLEGE, item);
                                editor.apply();
                            }
                        }
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

        mSearch = bottomsheetLayout.findViewById(R.id.et_search);
        btnAdd = bottomsheetLayout.findViewById(R.id.btn_add);

        FloatingActionButton fabCancel = bottomsheetLayout.findViewById(R.id.fab_cancel);

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetBehavior.setPeekHeight(0);

                if (filler.equals("college")) {
                    mLocation.setText(mSearch.getText().toString());
                } else {
                    mCategory.setText(mSearch.getText().toString());
                }
            }
        });
        btmRecyclerview = bottomsheetLayout.findViewById(R.id.recyclerview);
        btmProgressCircle = bottomsheetLayout.findViewById(R.id.progress_circle);
        btmProgressCircle.setVisibility(View.INVISIBLE);
        btmRecyclerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));


    }

    private void initWidgets() {

        parentView = findViewById(R.id.parent);
        mPhone = findViewById(R.id.et_phone);
        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        fabAdd = findViewById(R.id.fab_add);
        mCategory = findViewById(R.id.et_category);
        mCondition = findViewById(R.id.et_condition);
        mDescription = findViewById(R.id.et_desc);
        mHostel = findViewById(R.id.et_hostel);
        mLocation = findViewById(R.id.et_location);
        mName = findViewById(R.id.et_name);
        mPrice = findViewById(R.id.et_price);
        mPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
        mRoom = findViewById(R.id.et_room);
        btnPost = findViewById(R.id.btn_post);
    }

    private void requestPermission() {
        String[] strings = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(this, strings)) {

            imagePicker();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "App needs access to your camera & storage",
                    100,
                    strings
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            imageList = new ArrayList<>();

            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), MAX_IMAGE_SELECTION);

                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageList.add(imageUri);
                }
            } else if (data.getData() != null) {
                imageList.add(data.getData());
            }

            mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            photoAdapter = new PhotoAdapter(imageList, context, ProductListing.this);
            mRecyclerView.setAdapter(photoAdapter);
        }
    }

    private void imagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select up to 4 images"), REQUEST_CODE_PICK_IMAGES);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        if (requestCode == 100 && perms.size() == 2) {

            imagePicker();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {

            new AppSettingsDialog.Builder(this).build().show();
        } else {

            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    @Override
    public void onRemove(Uri uri) {

        imageList.remove(uri);

        photoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadText(String text) {
        if (filler.equals("college")) {
            mLocation.setText(text);
        } else if (filler.equals("category")) {
            mCategory.setText(text);
        } else {
            mCondition.setText(text);
        }
    }
}