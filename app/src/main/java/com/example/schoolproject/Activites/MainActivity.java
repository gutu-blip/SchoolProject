package com.example.schoolproject.Activites;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schoolproject.Adapters.ProfilesAdapter;
import com.example.schoolproject.Fragments.HomeFragment;
import com.example.schoolproject.Fragments.MyListings;
import com.example.schoolproject.Fragments.SavedFragment;
import com.example.schoolproject.Fragments.SettingsFragment;
import com.example.schoolproject.Interface.CallBackListener;
import com.example.schoolproject.Models.Seller;
import com.example.schoolproject.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CallBackListener {

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 3 * 1000;
    public DuoDrawerLayout drawerLayout;
    DuoDrawerToggle drawerToggle;
    private TextView mDisplayName, mUserName;
    GoogleSignInAccount signInAccount;
    Activity context = MainActivity.this;
    CircleImageView mProfilePhoto;
    ArrayList<Seller> mShops;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String currentUser = mUser.getUid();
    Button btnPost;
    Toolbar toolbar;
    ShimmerFrameLayout shimmerShops;
    RecyclerView recyclerView;
    int i = 0;
    Dialog dialog;
    LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInAccount = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        initWidgets();
        setItems();
        fetchShops();
        tapTarget();
        checkVersion();
        fetchLinks();


    }

    private void fetchLinks() {
        SharedPreferences.Editor editor = getSharedPreferences("LINKS_PREFS", MODE_PRIVATE).edit();

        DatabaseReference mLinksRefs = database.getReference("Links");
        mLinksRefs.keepSynced(true);

        mLinksRefs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String app = snapshot.child("app").getValue(String.class);
                String privacy = snapshot.child("privacy").getValue(String.class);
                String terms = snapshot.child("terms").getValue(String.class);

                if (app != null && terms != null && privacy != null) {
                    editor.putString("appLink", app);
                    editor.putString("termsLink", terms);
                    editor.putString("privacyLink", privacy);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        if (dialog.isShowing()) {
            dialog.setCancelable(false);
        } else {
            super.onBackPressed();
        }
    }

    private void checkVersion() {
        dialog = new Dialog(MainActivity.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.dialog_update);

        dialog.setCanceledOnTouchOutside(false);
        Button btnUpdate = dialog.findViewById(R.id.btn_update);
        dialog.setCancelable(false);

        DatabaseReference mVersionRef = database.getReference("Versions");
        mVersionRef.keepSynced(true);

        mVersionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String version = snapshot.child("Update").getValue(String.class);
                String APKVERSION = "nkjefh78y43rujwlkd43y";

                if (version != null) {

                    if (!version.equals(APKVERSION)) {

                        dialog.show();
                        btnUpdate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.lilac.clade");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
                    }else{
                        dialog.hide();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void tapTarget() {

        SharedPreferences read_prefs = getSharedPreferences("DRAWER_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences("DRAWER_PREFS", MODE_PRIVATE).edit();

        String toggle = read_prefs.getString("firstStart", "");

        if (toggle.equals("on")) {
            TapTargetView.showFor(MainActivity.this,
                    TapTarget.forView(linearLayout, "More Options", "Tap or slide screen to the right to view more options")
                            .outerCircleColor(R.color.teal_200)
                            .outerCircleAlpha(0.96f)
                            .targetCircleColor(R.color.white)
                            .titleTextSize(25)
                            .titleTextColor(R.color.white)
                            .descriptionTextSize(16)
                            .descriptionTextColor(R.color.black)
                            .textColor(R.color.black)
                            .textTypeface(Typeface.SANS_SERIF)
                            .dimColor(R.color.black)
                            .drawShadow(true)
                            .cancelable(true)
                            .tintTarget(true)
                            .transparentTarget(true)
                            .targetRadius(25),
                    new TapTargetView.Listener() {

                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);

                            editor.putString("firstStart", "off");
                            editor.apply();

                        }
                    });
        }
    }

    private void setItems() {

//        setSupportActionBar(toolbar);

        drawerToggle = new DuoDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        View menuView = drawerLayout.getMenuView();

        LinearLayout ll_Home = menuView.findViewById(R.id.ll_Home);
        LinearLayout ll_Saved = menuView.findViewById(R.id.ll_Savedlistings);
        LinearLayout ll_MyListings = menuView.findViewById(R.id.ll_Mylistings);
        LinearLayout ll_Feedback = menuView.findViewById(R.id.ll_Feedback);
        LinearLayout ll_Feature = menuView.findViewById(R.id.ll_Feature);
        LinearLayout ll_Logout = menuView.findViewById(R.id.ll_Logout);
        LinearLayout ll_Settings = menuView.findViewById(R.id.ll_Setting);

        ll_Home.setOnClickListener(this);
        ll_Feature.setOnClickListener(this);
        ll_Feedback.setOnClickListener(this);
        ll_Saved.setOnClickListener(this);
        ll_MyListings.setOnClickListener(this);
        ll_Logout.setOnClickListener(this);
        ll_Settings.setOnClickListener(this);

        replace(new HomeFragment());

        mDisplayName.setText(signInAccount.getDisplayName());
        mUserName.setText("acer/" + signInAccount.getEmail().replace("@gmail.com", ""));
        Picasso.get()
                .load(signInAccount.getPhotoUrl())
                .placeholder(R.drawable.person)
                .into(mProfilePhoto);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(context, btnPost);

                popupMenu.getMenu().add(Menu.NONE, 1, 1, "PRODUCT");
                popupMenu.getMenu().add(Menu.NONE, 2, 2, "SERVICE");
                popupMenu.getMenu().add(Menu.NONE, 3, 3, "GIG");

                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        if (id == 1) {

                            Intent intent = new Intent(context, ProductListing.class);
                            intent.putExtra("listingSource", "individual");
                            startActivity(intent);

                        } else if (id == 2) {

                            Intent intent = new Intent(context, ServiceListing.class);
                            intent.putExtra("listingSource", "individual");
                            startActivity(intent);

                        } else if (id == 3) {

                            Intent intent = new Intent(context, GigListing.class);
                            startActivity(intent);

                        }
                        return true;
                    }
                });
            }
        });
    }

    private void fetchShops() {
        mShops = new ArrayList<>();
        DatabaseReference mShopRef = database.getReference("Profiles").child(currentUser).child("Shops");
        mShopRef.keepSynced(true);

        mShopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot keySnapshot : snapshot.getChildren()) {

                    Seller shop = keySnapshot.child("Profile").getValue(Seller.class);
                    if (i > 0) {
                        mShops.add(shop);
                    }
                    i++;
                }

                shimmerShops.stopShimmer();
                shimmerShops.setVisibility(View.GONE);

                ProfilesAdapter mAdapter = new ProfilesAdapter(context, mShops, MainActivity.this);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initWidgets() {

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        linearLayout = findViewById(R.id.linearlayout0);
        mProfilePhoto = findViewById(R.id.profile_photo);
        mDisplayName = findViewById(R.id.displayName);
        mUserName = findViewById(R.id.username);
        shimmerShops = findViewById(R.id.shimmer_shops);
        btnPost = findViewById(R.id.btn_post);
        drawerLayout = (DuoDrawerLayout) findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ll_Home) {
            replace(new HomeFragment(), "Home");
        } else if (view.getId() == R.id.ll_Mylistings) {
            replace(new MyListings(), "MyListings");
        } else if (view.getId() == R.id.ll_Savedlistings) {
            replace(new SavedFragment(), "Saved");
        } else if (view.getId() == R.id.ll_Setting) {
            replace(new SettingsFragment(), "Settings");
        } else if (view.getId() == R.id.ll_Feature) {
            if (isAppInstalled(context, "com.whatsapp.w4b")) {
                openWhatsApp("+254706463654");
            } else if (isAppInstalled(context, "com.whatsapp")) {
                openWhatsApp("+254706463654");
            } else {
                Toast.makeText(context, "Whatsapp not installed in your device", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.ll_Feedback) {
            if (isAppInstalled(context, "com.whatsapp.w4b")) {
                openWhatsApp("+254706463654");
            } else if (isAppInstalled(context, "com.whatsapp")) {
                openWhatsApp("+254706463654");
            } else {
                Toast.makeText(context, "Whatsapp not installed on your device", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.ll_Logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        }
        drawerLayout.closeDrawer();
    }

    private void openWhatsApp(String number) {
        try {
            number = number.replace(" ", "").replace("+", "");

            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(number) + "@s.whatsapp.net");
            startActivity(sendIntent);

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

    private void replace(Fragment fragment, String s) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.addToBackStack(s);
        transaction.commit();
    }

    private void replace(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    @Override
    public void callBack(int position) {

        Seller shop = mShops.get(position);

        Intent intent = new Intent(context, ProfileShopActivity.class);
        intent.putExtra("shop", shop);
        startActivity(intent);
    }
    @Override
    protected void onResume() {

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                //do something
                new CheckInternetAsyncTask(getApplicationContext()).execute();
                handler.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }
}