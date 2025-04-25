package com.example.schoolproject.Activites;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.schoolproject.Adapters.BtmSheetAdapter;
import com.example.schoolproject.Interface.LoadText;
import com.example.schoolproject.Models.Item;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class GigListing extends AppCompatActivity implements LoadText {

    private static final String PREFS_COLLEGE = "college";
    private static final String PREFS_PHONE = "phone";

    EditText mDescription, mAmount, mPhone,mLink;
    TextInputEditText mCategory,mCollege,mTitle;
    Button btnPost;
    BottomSheetBehavior bottomSheetBehavior;
    ProgressBar btmProgressCircle;
    RecyclerView btmRecyclerview;
    AutoCompleteTextView mSearch;
    ImageButton btnAdd;
    DatabaseReference mListingsRef;
    private BtmSheetAdapter btmSheetAdapter = new BtmSheetAdapter();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private String currentUser;
    ArrayList<String> mColleges = new ArrayList<>();
    ArrayList<String> mCategories = new ArrayList<>();
    private int i;
    String description, amount,college,link, category,prevCategory = "", prevLocation = "", phone,title,userPhoto,userName;
    Context context = GigListing.this;
    CoordinatorLayout parentView;
    String filler = "";
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    private ArrayList<Item> categoryList, collegeList;
    SweetAlertDialog sweetAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gig_listing);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initWidgets();
        initBottomSheetWidgets();
        initBtmSheet();
        setItems();
        setListeners();
    }

    public ArrayList<String> getArrayList(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void setItems() {

        editor = getSharedPreferences("preferences", MODE_PRIVATE).edit();
        prefs = getSharedPreferences("preferences", MODE_PRIVATE);

        String prefsCollege = prefs.getString(PREFS_COLLEGE, "");
        String prefsPhone = prefs.getString(PREFS_PHONE, "");

        mCategories = getArrayList("service");
        mColleges = getArrayList("colleges");

        mCollege.setText(prefsCollege);
        mPhone.setText(prefsPhone);

        currentUser = mUser.getUid();
        mListingsRef = database.getReference("Listings");
        categoryList = new ArrayList<>();
        collegeList = new ArrayList<>();
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

                BtmSheetAdapter mAdapter = new BtmSheetAdapter(context,bottomSheetBehavior,categoryList,
                        GigListing.this);
                btmRecyclerview.setAdapter(mAdapter);

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setHideable(false);
                mSearch.setHint("Type and click add button to add a new category");

                filler = "category";
                mSearch.setText("");
            }
        });

        mCollege.setEnabled(true);
        mCollege.setTextIsSelectable(true);
        mCollege.setFocusable(false);
        mCollege.setFocusableInTouchMode(false);
        mCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collegeList.clear();
                for (String item : mColleges) {
                    collegeList.add(new Item(item, false));
                }

                BtmSheetAdapter mAdapter = new BtmSheetAdapter(context,bottomSheetBehavior,collegeList,GigListing.this);
                btmRecyclerview.setAdapter(mAdapter);

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setHideable(false);
                mSearch.setHint("Type and click add button to add a new university");

                filler = "college";
                mSearch.setText("");
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = 0;

                sweetAlert = new SweetAlertDialog(GigListing.this, SweetAlertDialog.PROGRESS_TYPE);
                sweetAlert.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                sweetAlert.setTitleText("Uploading...");
                sweetAlert.setCancelable(false);
                sweetAlert.getProgressHelper().setRimColor(Color.parseColor("#130822"));

                description = mDescription.getText().toString();
                college = mCollege.getText().toString().trim();
                editor.putString(PREFS_COLLEGE, college);
                editor.apply();

                amount = mAmount.getText().toString().trim();
                category = mCategory.getText().toString().trim();
                phone = mPhone.getText().toString().trim();
                title = mTitle.getText().toString();
                link = mLink.getText().toString();
                editor.putString(PREFS_PHONE, phone);
                editor.apply();

                if (!TextUtils.isEmpty(description) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(college)
                        && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(category)) {
                        uploadData();
                } else {
                    if (TextUtils.isEmpty(college)) {
                        Snackbar.make(parentView,
                                "University is required", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else if (TextUtils.isEmpty(description)) {
                        Snackbar.make(parentView, "Description is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if (TextUtils.isEmpty(title)) {
                        Snackbar.make(parentView, "A Title is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if (TextUtils.isEmpty(phone)) {
                        Snackbar.make(parentView, "Phone Number is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    } else if (TextUtils.isEmpty(category)) {
                        Snackbar.make(parentView, "Category is required", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            }
        });
    }

    private void uploadData() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(GigListing.this);
        sweetAlert.show();

        userName = signInAccount.getDisplayName();
        userPhoto = signInAccount.getPhotoUrl().toString();
        String key = this.mListingsRef.push().getKey();

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

        final String time = formatter.format(date);
        int price = Integer.parseInt(amount.replaceAll("\\D+", ""));

        Listing listing = new Listing(category,title,description,time,key,currentUser, college,
                amount,userName,phone.replaceFirst("0","+254"),
                userPhoto,link,price);

        if (!prevCategory.toLowerCase().equals(category.toLowerCase())) {
            if (!doesCategoryExist()) {
                database.getReference("Categories").child("Services").child(key).child("title").setValue(category);
            }
        }

        if (!prevLocation.toLowerCase().equals(college.toLowerCase())) {
            if (!doesCollegeExist()) {
                database.getReference("Colleges").child(key).child("name").setValue(college);
            }
        }

        mListingsRef.child("Gigs").child(college).child(key).setValue(listing).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                sweetAlert.getProgressHelper().stopSpinning();
                sweetAlert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                sweetAlert.setTitleText("Upload Successful");

                prevLocation = college;
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
    }

    private boolean doesCategoryExist() {
        for(String cat : mCategories){
            if(cat.trim().toLowerCase().equals(category.trim().toLowerCase())){
                return true;
            }
        }
        return false;
    }

    private boolean doesCollegeExist() {

        for(String university : mColleges){
            if(university.trim().toLowerCase().equals(college.trim().toLowerCase())){
                return true;
            }
        }
        return false;
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

                        if (filler.equals("category")) {
                            mCategory.setText(item);
                        } else if (filler.equals("college")) {
                            mCollege.setText(item);
                        }
                    }
                    filler = "";

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
                    mCollege.setText(mSearch.getText().toString());
                } else {
                    mCategory.setText(mSearch.getText().toString());
                }
            }
        });
        btmRecyclerview = bottomsheetLayout.findViewById(R.id.recyclerview);
        btmProgressCircle = bottomsheetLayout.findViewById(R.id.progress_circle);
        btmProgressCircle.setVisibility(View.GONE);
        btmRecyclerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false));
    }

    private void initWidgets() {

        mTitle = findViewById(R.id.et_title);
        mLink = findViewById(R.id.et_link);
        parentView = findViewById(R.id.coordinator);
        mPhone = findViewById(R.id.et_phone);
        mCategory = findViewById(R.id.et_category);
        mCategory.setInputType(InputType.TYPE_NULL);
        mDescription = findViewById(R.id.et_desc);
        mCollege = findViewById(R.id.et_location);
        mCollege.setInputType(InputType.TYPE_NULL);
        mAmount = findViewById(R.id.et_offer);
        btnPost = findViewById(R.id.btn_post);
    }

    @Override
    public void onLoadText(String text) {
        if (filler.equals("college")) {
            mCollege.setText(text);
        } else {
            mCategory.setText(text);
        }
    }
}