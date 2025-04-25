package com.example.schoolproject.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.schoolproject.Activites.MainActivity;
import com.example.schoolproject.Adapters.ListAdapter;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Interface.LoadText;
import com.example.schoolproject.Models.Item;
import com.example.schoolproject.Models.Listing;
import com.example.schoolproject.R;

import java.lang.reflect.Type;
import java.time.temporal.ValueRange;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;

public class Gigs extends Fragment implements LoadText, HomeFragment.LoadListener, CallBackListener {

    static final String PREFS_COLLEGE = "college";
    static final String PREFS_CATEGORY = "category";

    RecyclerView recyclerView, dialogRecyclerview;
    ProgressBar progressCircle;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mGigsRef;
    ArrayList<Listing> mListings;
    private ListAdapter mAdapter;
    Chip chipCategory, chipCollege, chipPrice;
    private EditText mMaxPrice, mMinPrice;
    int maxPrice = -1, minPrice = -1;
    ArrayList<Item> paramlist = new ArrayList();
    ArrayList<String> selectedFilters = new ArrayList<>();
    private ArrayList<String> mColleges;
    private ArrayList<String> mCategories;
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    RelativeLayout bottomsheetLayout;
    View view;
    AutoCompleteTextView mSearch;
    FloatingActionButton fabClear;
    ImageView ivSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_listings, container, false);

        initWidgets(view);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setItems();
        fetchGigs();
        chipListeners();
        setListeners();

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
        ArrayList<Listing> filteredList = new ArrayList<>();

        if (mAdapter != null) {
            for (Listing listing : mListings) {

                if (listing.getDescription().toLowerCase().contains(name.toLowerCase()) ||
                        listing.getName().toLowerCase().contains(name.toLowerCase()) ||
                        listing.getCategory().toLowerCase().contains(name.toLowerCase()) ||
                        listing.getLocation().toLowerCase().contains(name.toLowerCase()) ||
                        String.valueOf(listing.getPrice()).contains(name.toLowerCase())
                ) {
                    filteredList.add(listing);
                }
                onLoadListings(filteredList);
            }
        }
    }

    private void fetchGigs() {

        mGigsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot collegeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot keySnapshot : collegeSnapshot.getChildren()) {

                        Listing listing = keySnapshot.getValue(Listing.class);

                        ArrayList<String> specs = new ArrayList<>();
                        specs.add(listing.getLocation());
                        specs.add(listing.getCategory());

                        listing.setSpecs(specs);
                        long timeStamp = Long.parseLong(listing.getTime().replaceAll("\\D+", ""));
//                        listing.setTimeStamp(timeStamp);

                        mListings.add(listing);

                    }
                }
//                Collections.sort(mListings,new TimeCompare());
//              Collections.reverse(mListings);

                onLoadListings(mListings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chipListeners() {

        Dialog dialog = new Dialog(getActivity());
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

                ListAdapter listAdapter = new ListAdapter(getActivity(), setLists(param), dialog, Gigs.this);
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

                ListAdapter listAdapter = new ListAdapter(getActivity(), setLists(param), dialog, Gigs.this);
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
                mMinPrice = dialog.findViewById(R.id.et_minprice);
                mMinPrice.setInputType(InputType.TYPE_CLASS_NUMBER);

                mMaxPrice = dialog.findViewById(R.id.et_maxprice);
                mMaxPrice.setInputType(InputType.TYPE_CLASS_NUMBER);


                btnGo.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View view) {

                        String max = mMaxPrice.getText().toString().trim();
                        String min = mMinPrice.getText().toString().trim();

                        if (mMaxPrice.length() > 0 && mMinPrice.length() > 0) {
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

        ArrayList<Listing> filteredList = new ArrayList<>();

        if (minPrice != -1) {
            for (Listing listing : mListings) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (listing.getSpecs().containsAll(filters) && (ValueRange.of(minPrice, maxPrice).isValidIntValue(listing.getPrice()))) {
                        filteredList.add(listing);
                    }
                }
            }
        } else {
            for (Listing listing : mListings) {
                if (listing.getSpecs().containsAll(filters)) {
                    filteredList.add(listing);
                }
            }
        }

        onLoadListings(filteredList);
    }

    private void onLoadListings(ArrayList<Listing> list) {
        mAdapter = new ListAdapter(list, getActivity(), "Gigs", Gigs.this);
        SlideInRightAnimationAdapter alphaInAnimationAdapter = new SlideInRightAnimationAdapter(mAdapter);
        alphaInAnimationAdapter.setDuration(700);
        alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        alphaInAnimationAdapter.setFirstOnly(false);

        recyclerView.setAdapter(alphaInAnimationAdapter);
        progressCircle.setVisibility(View.INVISIBLE);
        mAdapter.notifyDataSetChanged();

    }

    private void setItems() {
        mListings = new ArrayList<>();

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
        mGigsRef = database.getReference("Listings").child("Gigs");

        mGigsRef.keepSynced(true);

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

    public ArrayList<String> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
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
        String title = "Gigs";
        return title;
    }

    @Override
    public void callBack(int position) {
        Listing listing = mListings.get(position);
        SavedServices.saved.trade = "Gigs";
        SavedServices.saved.dialogEvent(listing, getActivity());
    }
}