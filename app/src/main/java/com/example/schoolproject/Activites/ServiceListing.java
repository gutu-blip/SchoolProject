package com.example.schoolproject.Activites;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.example.schoolproject.Adapters.PhotoAdapter;
import com.example.schoolproject.Interface.LoadText;
import com.example.schoolproject.Models.Item;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.R;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ServiceListing extends AppCompatActivity implements PhotoAdapter.RemoveListener, EasyPermissions.PermissionCallbacks, LoadText {

    private static final String PREFS_HOSTEL = "hostel";
    private static final String PREFS_ROOM = "room";
    private static final String PREFS_COLLEGE = "college";
    private static final String PREFS_PHONE = "phone";

    EditText mName, mDescription, mHostel, mRoom, mRates, mPhone;
    TextInputEditText mCategory, mLocation;
    Button btnPost;
    ImageButton btnAdd;
    FloatingActionButton fabAdd;
    BottomSheetBehavior bottomSheetBehavior;
    ProgressBar btmProgressCircle;

    RecyclerView mRecyclerView, btmRecyclerview;
    AutoCompleteTextView mSearch;
    PhotoAdapter photoAdapter;
    ArrayList<Uri> imageList = new ArrayList<>();
    DatabaseReference mListingsRef;
    private BtmSheetAdapter btmSheetAdapter = new BtmSheetAdapter();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    public StorageTask mUploadTask;
    private StorageReference mStorageRef;
    private String currentUser;
    ArrayList<String> mColleges = new ArrayList<>();
    ArrayList<String> mCategories = new ArrayList<>();
    ArrayList<Item> paramlist = new ArrayList();
    private int i;
    private String listingSource = "";
    String description, name, location, room = "", prevLocation = "", prevCategory = "", hostel = "", rates, category, phone;
    Context context = ServiceListing.this;
    CoordinatorLayout parentView;
    String filler = "";
    String sellerKey = "", sellerPhone = "", sellerPhoto = "", sellerName = "";
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    private Seller shop;
    SweetAlertDialog sweetAlert;
    private static final int REQUEST_CODE_PICK_IMAGES = 1001;
    private static final int MAX_IMAGE_SELECTION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_listing);
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

    public ArrayList<Item> setLists(String parameter) {

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

        mCategories = getArrayList("service");
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

        currentUser = mUser.getUid();
        mListingsRef = database.getReference("Listings");
        mStorageRef = FirebaseStorage.getInstance().getReference("Listings");

    }

    private void setAdapter(ArrayList<Item> list) {

        btmSheetAdapter = new BtmSheetAdapter(context, bottomSheetBehavior, list, ServiceListing.this);
        SlideInBottomAnimationAdapter alphaInAnimationAdapter = new SlideInBottomAnimationAdapter(btmSheetAdapter);
        alphaInAnimationAdapter.setDuration(1000);
        alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        alphaInAnimationAdapter.setFirstOnly(false);

        btmRecyclerview.setAdapter(alphaInAnimationAdapter);
        btmSheetAdapter.notifyDataSetChanged();
    }

    private void setListeners() {

        mCategory.setEnabled(true);
        mCategory.setTextIsSelectable(true);
        mCategory.setFocusable(false);
        mCategory.setFocusableInTouchMode(false);
        mCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (setLists("category") != null) {
                    setLists("category").clear();
                }
                setAdapter(setLists("category"));

                mSearch.setHint("Type and click add button to add a new category");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setHideable(false);
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

                if (setLists("college") != null) {
                    setLists("college").clear();
                }
                setAdapter(setLists("college"));

                mSearch.setHint("Type and click add button to add a new university");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setHideable(false);
                filler = "college";
                mSearch.setText("");

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
                sweetAlert = new SweetAlertDialog(ServiceListing.this, SweetAlertDialog.PROGRESS_TYPE);
                sweetAlert.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                sweetAlert.setTitleText("Uploading...");
                sweetAlert.setCancelable(false);
                sweetAlert.getProgressHelper().setRimColor(Color.parseColor("#130822"));

                description = mDescription.getText().toString();
                name = mName.getText().toString().trim();
                phone = mPhone.getText().toString().trim();
                hostel = mHostel.getText().toString().trim();
                room = mRoom.getText().toString().trim();
                location = mLocation.getText().toString().trim();

                if (listingSource.equals("individual")) {
                    location = mLocation.getText().toString().trim();
                    editor.putString(PREFS_COLLEGE, location);
                    editor.apply();

                    room = mRoom.getText().toString().trim();
                    editor.putString(PREFS_ROOM, room);
                    editor.apply();

                    hostel = mHostel.getText().toString().trim();
                    editor.putString(PREFS_HOSTEL, hostel);
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
                rates = mRates.getText().toString().trim();
                category = mCategory.getText().toString().trim();

                if (!TextUtils.isEmpty(description) && !TextUtils.isEmpty(name)
                        && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(location)
                        && !TextUtils.isEmpty(rates) && !TextUtils.isEmpty(category)) {

                    if (mUploadTask == null || !mUploadTask.isInProgress()) {
                        uploadData();
                    } else {
                        Snackbar.make(parentView, "Upload in progress",
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    if (TextUtils.isEmpty(rates)) {
                        Snackbar.make(parentView,
                                "Enter amount you charge for the service", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else if (TextUtils.isEmpty(description)) {
                        Snackbar.make(parentView, "Description is required", Snackbar.LENGTH_LONG)
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
                    }
                }
            }
        });
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

    private void uploadData() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(ServiceListing.this);

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
        int price = Integer.parseInt(rates.replaceAll("\\D+", ""));

        if (imageList.size() == 0) {
            sweetAlert.show();

            if (!prevCategory.toLowerCase().equals(category.toLowerCase())) {
                if (!doesCategoryExist()) {
                    database.getReference("Categories").child("Services").child(key).child("title").setValue(category);
                }
            }
            if (!prevLocation.toLowerCase().equals(location.toLowerCase())) {
                if (!doesCollegeExist()) {
                    database.getReference("Colleges").child(key).child("name").setValue(location);
                }
            }

            Listing listing = new Listing(name, description, category, strDate, key, listingSource,
                    "Services", currentUser, location, hostel, room, "",
                    "", "", sellerKey, sellerName, sellerPhone.replaceFirst("0", "+254"),
                    sellerPhoto, price, rates);

            mListingsRef.child("Services").child(location).child(key).child("Details").setValue(listing).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    sweetAlert.getProgressHelper().stopSpinning();
                    sweetAlert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    sweetAlert.setTitleText("Upload Successful");

                    prevLocation = location;
                    prevCategory = category;
                }
            }).addOnFailureListener((OnFailureListener) new OnFailureListener() {
                public void onFailure(Exception e) {
                    sweetAlert.getProgressHelper().stopSpinning();
                    sweetAlert.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    sweetAlert.setTitleText("Error");
                    sweetAlert.setContentText(e.getMessage());
                }
            });

        } else if (imageList.size() == 1) {
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
                                                    database.getReference("Categories").child("Services").child(key).child("title").setValue(category);
                                                }
                                            }
                                            if (!prevLocation.toLowerCase().equals(location.toLowerCase())) {
                                                if (!doesCollegeExist()) {
                                                    database.getReference("Colleges").child(key).child("name").setValue(location);
                                                }
                                            }
                                            Listing listing = new Listing(name, description, category, strDate, key, listingSource,
                                                    "Services", currentUser, location, hostel, room, "",
                                                    uri.toString(), "singles", sellerKey, sellerName, sellerPhone.replaceFirst("0", "+254"),
                                                    sellerPhoto, price, rates);

                                            mListingsRef.child("Services").child(location).child(key).child("Details").setValue(listing);

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
                                    mListingsRef.child("Services").child(location).child(key).
                                            child("Album").child(imageKey).child("imageUrl").setValue(uri.toString());

                                    if (i == 0) {
                                        if (!prevCategory.toLowerCase().equals(category.toLowerCase())) {
                                            if (!doesCategoryExist()) {
                                                database.getReference("Categories").child("Services").child(key).child("title").setValue(category);
                                            }
                                        }
                                        if (!prevLocation.toLowerCase().equals(location.toLowerCase())) {
                                            if (!doesCollegeExist()) {
                                                database.getReference("Colleges").child(key).child("name").setValue(location);
                                            }
                                        }

                                        Listing marketPlace = new Listing(name, description, category,
                                                strDate, key, listingSource, "Services", currentUser, location, hostel, room, "",
                                                uri.toString(), "album", sellerKey, sellerName, sellerPhone.replaceFirst("0", "+254"),
                                                sellerPhoto, price, rates);

                                        mListingsRef.child("Services").child(location).child(key).child("Details").setValue(marketPlace);

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

        FloatingActionButton fabCancel = bottomsheetLayout.findViewById(R.id.fab_cancel);
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        mSearch = bottomsheetLayout.findViewById(R.id.et_search);
        btnAdd = bottomsheetLayout.findViewById(R.id.btn_add);

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
        btmRecyclerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        btmProgressCircle = bottomsheetLayout.findViewById(R.id.progress_circle);
        btmProgressCircle.setVisibility(View.INVISIBLE);

    }

    private void initWidgets() {

        parentView = findViewById(R.id.coordinator);
        mPhone = findViewById(R.id.et_phone);
        mRecyclerView = findViewById(R.id.recyclerview);
        fabAdd = findViewById(R.id.fab_add);
        mCategory = findViewById(R.id.et_category);
        mCategory.setInputType(InputType.TYPE_NULL);
        mDescription = findViewById(R.id.et_desc);
        mHostel = findViewById(R.id.et_hostel);
        mLocation = findViewById(R.id.et_location);
        mLocation.setInputType(InputType.TYPE_NULL);
        mName = findViewById(R.id.et_name);
        mRates = findViewById(R.id.et_rates);
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

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_PICK_IMAGES) {

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
                photoAdapter = new PhotoAdapter(imageList, context, ServiceListing.this);
                mRecyclerView.setAdapter(photoAdapter);
            }
        }
    }

    private void imagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select up to 10 images"), REQUEST_CODE_PICK_IMAGES);
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

    @Override
    public void onRemove(Uri uri) {

        imageList.remove(uri);
        photoAdapter.notifyDataSetChanged();
    }

    private String getFileExtension(Uri uri) {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    @Override
    public void onLoadText(String text) {

        if (filler.equals("category")) {
            mCategory.setText(text);
        } else {
            mLocation.setText(text);
        }
    }
}