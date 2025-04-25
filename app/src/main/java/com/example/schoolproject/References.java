package com.example.schoolproject;

import android.os.Build;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

public class References extends AppCompatActivity {

    CoordinatorLayout view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Snackbar.make(getWindow().getDecorView().getRootView(), " following no one yet", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        //close keyboard when edittext is focused
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vto.removeOnGlobalLayoutListener(this);
                Toast.makeText(References.this, "Action", Toast.LENGTH_SHORT).show();
            }
        });

        //Set statusbar color dynamically
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

            Window window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.colorDarkSemiTransparent));
        }
/*
                   ChipGroup

            for (String shoe : outfit.getGarments()) {
                Chip chip = new Chip(context);
                chip.setText(shoe);
                chipGroup.addView(chip);
            }

                         Sending & Fetching List to different Activity

                                     //Sending
        Intent intent = new Intent().setClass(mActivity, ProfileShopActivity.class);
        intent.putParcelableArrayListExtra("shops", shopList);
        intent.putExtra("position", position);

        mActivity.startActivity(intent);

                                     //Fetching
            mApparels = getIntent().getParcelableArrayListExtra("apparels");
            position = getIntent().getIntExtra("position", 8);
            album = getIntent().getStringArrayListExtra("album");
            image = getIntent().getExtras().getString("image");
            uid = getIntent().getExtras().getString("uid");
*/

/*
                           //FAB
                        <com.google.android.material.floatingactionbutton.FloatingActionButton
                            android:id="@+id/fab_add"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_centerVertical="true"
                            android:layout_marginEnd="5dp"
                            android:layout_marginRight="16dp"
                            android:backgroundTint="@color/accent_blue"
                            android:baselineAlignBottom="false"
                            android:clickable="true"
                            android:scaleType="centerCrop"
                            android:src="@drawable/ic_add"
                            android:tint="@color/white"
                            app:fabCustomSize="40dp" />
       */

//                            Glide

//        Glide.with(mActivity)
//                .load(apparel.getmImageUrl())
//                .centerCrop()
//                .fitCenter()
//                .placeholder(R.drawable.ic_launcher_foreground)
//                .into(proImageView);

        // Displaying a list of texts on a Toast

           /* StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < selectedSpecialties.size(); i++) {

                    stringBuilder.append(selectedSpecialties.get(i));
                    stringBuilder.append("\n");
                }
                Toast.makeText(getApplicationContext(), stringBuilder.toString().trim(), Toast.LENGTH_SHORT).show();
           */

//        Fragment fragment = new OutfitTypes();
//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        ft.replace(R.id.id_genre_types, fragment);
//        ft.commit();


        //SharedPreferences

//set
//        SharedPreferences.Editor editor = mActivity.getSharedPreferences("prefskey",MODE_PRIVATE).edit();
//        editor.putString("key",outfit.getKey());
//        editor.apply();

//retrieve
//        SharedPreferences prefs = getSharedPreferences("prefskey", MODE_PRIVATE);
//        postKey = prefs.getString("key", "none");
    }


    //SliderView

  /*          ArrayList<String> album = new ArrayList<>();

            for (String image : apparel.getmAlbum()) {
                album.add(image);
            }

            if (apparel.getmUploadType().equals("singles")) {
                ArrayList<String> image = new ArrayList<>();

                image.add(apparel.getmImageUrl());
                sliderView.setSliderAdapter(new RequestsMemberAdapter(context, image,position));

            } else {
                sliderView.setSliderAdapter(new RequestsMemberAdapter(context, apparel.getmAlbum(),
                        position));
            }

            sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
            sliderView.setScrollTimeInSec(4);
            sliderView.setSliderAnimationDuration(ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED);
            sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
            sliderView.startAutoCycle();*/

//        Calendar cdate = Calendar.getInstance();
//        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMMM-yyyy");
//        final String savedate = currentdate.format(cdate.getTime());
//
//        Calendar ctime = Calendar.getInstance();
//        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
//        final String savetime = currenttime.format(ctime.getTime());
//        String time = savedate + ":" + savetime;

}
