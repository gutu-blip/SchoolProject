package com.example.schoolproject.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.schoolproject.Activites.MainActivity;
import com.example.schoolproject.Adapters.BtmSheetAdapter;
import com.example.schoolproject.Adapters.ListAdapter;
import com.example.schoolproject.Adapters.ServiceAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Interface.DeleteListener;
import com.example.schoolproject.Interface.LoadText;
import com.example.schoolproject.Models.Item;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.Models.ServiceItem;
import com.example.schoolproject.R;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.time.temporal.ValueRange;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;

public class ServicesPosts extends Fragment implements LoadText, HomeFragment.LoadListener, CallBackListener, DeleteListener {


    static final String PREFS_COLLEGE = "college";
    static final String PREFS_CATEGORY = "category";

    RecyclerView recyclerView, btmRecyclerview, dialogRecyclerview;
    ProgressBar progressCircle;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mServicesRef;
    private ServiceAdapter mAdapter;
    Chip chipCategory, chipCollege, chipPrice;
    private AutoCompleteTextView mSpecsSearch;
    RelativeLayout bottomsheetLayout;
    View view;
    AutoCompleteTextView mSearch;
    FloatingActionButton fabClear;
    ImageView ivSearch;
    RatingBar mRatingBar;
    TextView mRating;
    Dialog dialog;
    double average = 0.0;
    TextInputEditText etMinPrice, etMaxPrice;
    int maxPrice = -1;
    int minPrice = -1;
    ArrayList<Item> paramlist = new ArrayList();
    ArrayList<String> selectedFilters = new ArrayList<>();
    private ArrayList<String> mColleges = new ArrayList<>();
    private ArrayList<String> mCategories = new ArrayList<>();
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    BtmSheetAdapter btmSheetAdapter;
    BottomSheetBehavior bottomSheetBehavior;
    ArrayList<ServiceItem> items;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_listings, container, false);

        initWidgets(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setItems();
        setListeners();
        chipListeners();
        fetchServices();

    }

    private void setListeners() {

        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearch.getText().clear();
            }
        });
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    ivSearch.setVisibility(View.VISIBLE);
                    fabClear.setVisibility(View.GONE);
                    mSearch.setPadding(5,0,0,0);
                } else {
                    mSearch.setPadding(35,0,0,0);
                    fabClear.setVisibility(View.VISIBLE);
                    ivSearch.setVisibility(View.GONE);
                }
                searchItems(editable.toString());
            }
        });
    }

    private void searchItems(String name) {
        ArrayList<ServiceItem> filteredList = new ArrayList<>();

        if (mAdapter != null) {
            for (ServiceItem item : items) {
                Listing listing = (Listing) item.getObject();

                if (listing.getDescription().toLowerCase().contains(name.toLowerCase())||
                    listing.getName().toLowerCase().contains(name.toLowerCase()) ||
                    listing.getCategory().toLowerCase().contains(name.toLowerCase())||
                    listing.getLocation().toLowerCase().contains(name.toLowerCase())||
                    String.valueOf(listing.getPrice()).contains(name.toLowerCase())||
                    listing.getHostel().toLowerCase().contains(name.toLowerCase())||
                    listing.getRoom().toLowerCase().contains(name.toLowerCase())||
                    listing.getRates().toLowerCase().contains(name.toLowerCase())
                    ){
                    filteredList.add(item);
                }
            }
            onLoadListings(filteredList);
        }
    }

    private void chipListeners() {

        dialog = new Dialog(getActivity());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.dialog_list);

        dialogRecyclerview = dialog.findViewById(R.id.recyclerview);
        dialogRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));



        chipCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

                String param = ((Chip) view).getText().toString();

                if (setLists(param) != null) {
                    setLists(param).clear();
                }

                ListAdapter listAdapter = new ListAdapter(getActivity(), setLists(param), dialog, ServicesPosts.this);
                dialogRecyclerview.setAdapter(listAdapter);

            }
        });
        chipCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

                String param = ((Chip) view).getText().toString();
                if (setLists(param) != null) {
                    setLists(param).clear();
                }

                ListAdapter listAdapter = new ListAdapter(getActivity(), setLists(param), dialog, ServicesPosts.this);
                dialogRecyclerview.setAdapter(listAdapter);
            }
        });

        chipPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(getActivity());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.setContentView(R.layout.dialog_price_filter);
                dialog.show();

                Button btnGo = dialog.findViewById(R.id.btn_go);
                etMinPrice = dialog.findViewById(R.id.et_minprice);
                etMinPrice.setInputType(InputType.TYPE_CLASS_NUMBER);

                etMaxPrice = dialog.findViewById(R.id.et_maxprice);
                etMaxPrice.setInputType(InputType.TYPE_CLASS_NUMBER);

                btnGo.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View view) {
                        String max = etMaxPrice.getText().toString().trim();
                        String min = etMinPrice.getText().toString().trim();

                        if (etMaxPrice.length() > 0 && etMinPrice.length() > 0) {
                            maxPrice = Integer.parseInt(max);
                            minPrice = Integer.parseInt(min);

                            checkSelectedFilters(selectedFilters);
                            dialog.dismiss();
                            chipPrice.setText(min + "-" + max);
                        } else {
                            Snackbar.make(((MainActivity) getActivity()).drawerLayout, "Please choose a range", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }
                });
            }
        });

        clearChip();
    }

    public ArrayList<String> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }


    private void initBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        btmRecyclerview = bottomsheetLayout.findViewById(R.id.recyclerview);
        btmRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false));

        FloatingActionButton fabCancel = bottomsheetLayout.findViewById(R.id.fab_cancel);
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        mSpecsSearch = bottomsheetLayout.findViewById(R.id.et_search);
        mSpecsSearch.setVisibility(View.GONE);
        bottomsheetLayout.findViewById(R.id.btn_add).setVisibility(View.GONE);
        bottomsheetLayout.findViewById(R.id.progress_circle).setVisibility(View.INVISIBLE);

        bottomSheetBehavior.setPeekHeight(0);
        //set your sheet hideable or not
        bottomSheetBehavior.setHideable(false);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {

                    bottomSheetBehavior.setHideable(false);

                    if (btmSheetAdapter.getSelectedItem() != null) {

                        String type = btmSheetAdapter.getSelectedItem().getItem();

                        if (mColleges.contains(type)) {
                            chipCollege.setText(type);
                            editor.putString(PREFS_COLLEGE, type);
                            editor.apply();
                        } else if (mCategories.contains(type)) {
                            chipCategory.setText(type);
                            editor.putString(PREFS_CATEGORY, type);
                            editor.apply();
                        }
                    }
                    String prefsCollege = prefs.getString(PREFS_COLLEGE, "none");
                    String prefsCategory = prefs.getString(PREFS_CATEGORY, "none");

                    selectedFilters.clear();

                    if (!prefsCollege.contains("none")) {
                        selectedFilters.add(prefsCollege);
                    }
                    if (!prefsCategory.contains("none")) {
                        selectedFilters.add(prefsCategory);
                    }


                    checkSelectedFilters(selectedFilters);
                } else {

                    bottomSheetBehavior.setHideable(true);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }


    private void setAdapter(ArrayList<Item> list) {

        btmSheetAdapter = new BtmSheetAdapter((Context) getActivity(), bottomSheetBehavior, list);
        SlideInBottomAnimationAdapter alphaInAnimationAdapter = new SlideInBottomAnimationAdapter(btmSheetAdapter);
        alphaInAnimationAdapter.setDuration(1000);
        alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        alphaInAnimationAdapter.setFirstOnly(false);

        btmRecyclerview.setAdapter(alphaInAnimationAdapter);
        btmSheetAdapter.notifyDataSetChanged();
    }

    private void clearChip() {

        chipCollege.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                String typeChip = prefs.getString(PREFS_COLLEGE, "none");
                if (!chipCollege.getText().toString().equals("University")) {
                    chipCollege.setText("University");
                    editor.putString(PREFS_COLLEGE, "none");
                    editor.apply();
                    selectedFilters.remove(typeChip);
                    checkSelectedFilters(selectedFilters);
                }

                return true;
            }
        });
        chipCategory.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                String typeChip = prefs.getString(PREFS_CATEGORY, "none");
                if (!chipCategory.getText().toString().equals("Category")) {
                    chipCategory.setText("Category");
                    editor.putString(PREFS_CATEGORY, "none");
                    editor.apply();
                    selectedFilters.remove(typeChip);
                    checkSelectedFilters(selectedFilters);
                }

                return true;
            }
        });
        chipPrice.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                maxPrice = -1;
                minPrice = -1;
                checkSelectedFilters(selectedFilters);
                chipPrice.setText("Price");
                return true;
            }
        });
    }

    public ArrayList<Item> setLists(String parameter) {

        if (parameter.equals("University") || mColleges.contains(parameter)) {
            for (String name : mColleges) {
                paramlist.add(new Item(name, false));
            }
            return paramlist;
        } else if (parameter.equals("Category") || mCategories.contains(parameter)) {
            for (String name : mCategories) {
                paramlist.add(new Item(name, false));
            }
            return paramlist;
        }
        return null;
    }

    private void checkSelectedFilters(ArrayList<String> filters) {

        ArrayList<ServiceItem> filteredList = new ArrayList<>();
        if (minPrice != -1) {
            for (ServiceItem item : items) {
                Listing listing = (Listing) item.getObject();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (listing.getSpecs().containsAll(filters) && (ValueRange.of(minPrice, maxPrice).isValidIntValue(listing.getPrice()))) {
                        filteredList.add(item);
                    }
                }
            }
        } else {
            for (ServiceItem item : items) {
                Listing listing = (Listing) item.getObject();

                if (listing.getSpecs().containsAll(filters)) {
                    filteredList.add(item);
                }
            }
        }

        onLoadListings(filteredList);
    }

    private void onLoadListings(ArrayList<ServiceItem> list) {
        mAdapter = new ServiceAdapter(getActivity(), list, ServicesPosts.this,ServicesPosts.this);
        mAdapter.calling = "ServicesPosts";

        SlideInRightAnimationAdapter alphaInAnimationAdapter = new SlideInRightAnimationAdapter(mAdapter);
        alphaInAnimationAdapter.setDuration(700);
        alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        alphaInAnimationAdapter.setFirstOnly(false);

        recyclerView.setAdapter(alphaInAnimationAdapter);
        progressCircle.setVisibility(View.INVISIBLE);
        mAdapter.notifyDataSetChanged();

    }

    private void setItems() {
        items = new ArrayList<>();

        mCategories = getArrayList("service");
        mColleges = getArrayList("colleges");

        if (mCategories == null) {
            mCategories = new ArrayList<>();
            mColleges = new ArrayList<>();
        }

        if (mCategories.size() != 0) {
            chipCategory.setVisibility(View.VISIBLE);
        }
        if (mColleges.size() != 0) {
            chipCollege.setVisibility(View.VISIBLE);
        }

        mServicesRef = database.getReference("Listings").child("Services");

        mServicesRef.keepSynced(true);

        editor = getActivity().getSharedPreferences("SPECSPARAM", MODE_PRIVATE).edit();
        prefs = getActivity().getSharedPreferences("SPECSPARAM", MODE_PRIVATE);

        String prefsCollege = prefs.getString(PREFS_COLLEGE, "none");
        String prefCategory = prefs.getString(PREFS_CATEGORY, "none");

        if (!prefsCollege.contains("none")) {
            editor.putString(PREFS_COLLEGE, "none");
            editor.apply();
        }
        if (!prefCategory.contains("none")) {
            editor.putString(PREFS_CATEGORY, "none");
            editor.apply();
        }
    }

    private void fetchServices() {

        mServicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot collegeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot keySnapshot : collegeSnapshot.getChildren()) {

                        Listing listing = keySnapshot.child("Details").getValue(Listing.class);

                        if (listing != null) {
                            ArrayList<String> album = new ArrayList<>();
                            for (DataSnapshot imageKey : keySnapshot.child("Album").getChildren()) {
                                String image = String.valueOf(imageKey.child("imageUrl").getValue());
                                album.add(image);
                            }
                            ArrayList<String> specs = new ArrayList<>();

                            specs.add(listing.getCategory());
                            specs.add(listing.getLocation());

                            listing.setSpecs(specs);
                            if (listing.getImageUrl().equals("")) {

                                items.add(new ServiceItem(listing, 0));
                            } else if (!TextUtils.isEmpty(listing.getImageUrl())) {
                                listing.setAlbum(album);
                                items.add(new ServiceItem(listing, 1));

                            }
                        }
                    }
                }
//                Collections.reverse(items);
                onLoadListings(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initWidgets(View view) {
        bottomsheetLayout = view.findViewById(R.id.bottom_sheet_recycler);

        mSearch = view.findViewById(R.id.et_search);
        ivSearch = view.findViewById(R.id.iv_search);
        fabClear = view.findViewById(R.id.fab_clear);
        chipCollege = view.findViewById(R.id.chip_college);
        chipCategory = view.findViewById(R.id.chip_category);
        chipPrice = view.findViewById(R.id.chip_price);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        progressCircle = view.findViewById(R.id.progress_circle);
    }

    @Override
    public void onLoadText(String text) {
        if (mColleges.contains(text)) {
            chipCollege.setText(text);
            editor.putString(PREFS_COLLEGE, text);
            editor.apply();
        } else if (mCategories.contains(text)) {
            chipCategory.setText(text);
            editor.putString(PREFS_CATEGORY, text);
            editor.apply();
        }

        String prefsCollege = prefs.getString(PREFS_COLLEGE, "none");
        String prefsCategory = prefs.getString(PREFS_CATEGORY, "none");

        selectedFilters.clear();

        if (!prefsCollege.contains("none")) {
            selectedFilters.add(prefsCollege);
        }
        if (!prefsCategory.contains("none")) {
            selectedFilters.add(prefsCategory);
        }

        checkSelectedFilters(selectedFilters);
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

                    mRatingBar.setRating((float) average);
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
            Toast.makeText(getActivity(), "" + e, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loadColleges(ArrayList<String> list) {
        mColleges = list;
        chipCollege.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadProductCategories(ArrayList<String> list) {

    }

    @Override
    public void loadServiceCategories(ArrayList<String> list) {
        mCategories = list;
        chipCategory.setVisibility(View.VISIBLE);

    }

    @Override
    public String toString() {
        String title = "Services";
        return title;
    }

    @Override
    public void callBack(int position) {
        Listing listing = (Listing) items.get(position).getObject();
        if (listing.getImageUrl().isEmpty()) {

            SavedServices.saved.trade = "Service";
            SavedServices.saved.dialogEvent(listing, getActivity());
        }
    }

    @Override
    public void onDelete(int position) {

    }
}