package com.example.schoolproject.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.example.schoolproject.R;

public class SettingsFragment extends Fragment {

    View view;
    RelativeLayout rlPrivacy,rlTerms,rlShare,parentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       view = inflater.inflate(R.layout.fragment_settings, container, false);

       initWidgets(view);

       return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListeners();

    }

    private void setListeners() {
        SharedPreferences readPrefs = getActivity().getSharedPreferences("LINKS_PREFS", MODE_PRIVATE);

        rlShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String link = readPrefs.getString("appLink","");

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "You,ve got to try this app. \n\n " + link);

                if(!link.equals("")) {
                    startActivity(Intent.createChooser(intent, "ShareVia"));
                }else{
                    Snackbar.make(parentView, "Link unavailable", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        rlPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String link = readPrefs.getString("privacyLink","");

                if(!link.equals("")) {
                    Uri uri = Uri.parse(link);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }else{
                    Snackbar.make(parentView, "Link unavailable", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            }
        });

        rlTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link = readPrefs.getString("termsLink","");

                if(!link.equals("")) {
                    Uri uri = Uri.parse(link);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }else{
                    Snackbar.make(parentView, "Link unavailable", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }


    private void initWidgets(View view){

        rlPrivacy = view.findViewById(R.id.rl_privacy);
        rlTerms = view.findViewById(R.id.rl_terms);
        rlShare = view.findViewById(R.id.rl_share);
        parentView = view.findViewById(R.id.parent);

    }
}