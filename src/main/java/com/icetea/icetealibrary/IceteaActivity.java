package com.icetea.icetealibrary;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.icetea.icetealibrary.fragments.MFragment;

public class IceteaActivity extends AppCompatActivity {

    protected String currentFragmentTag;
    protected FragmentManager fragmentManager;
    protected static final String CURRENT_TAG_KEY = "current_tag";
    private int holder = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null)
            currentFragmentTag = savedInstanceState.getString(CURRENT_TAG_KEY);
    }

    protected void replaceBackgroundFragment(MFragment mf, String tag, boolean addBackStack) {
        if (holder != -1 && mf != null && (currentFragmentTag == null || !currentFragmentTag.equals(tag))) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right);
            ft.replace(holder, mf, tag);
            if (addBackStack) {
                mf.setCanBack(true);
                ft.addToBackStack(tag);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_TAG_KEY, currentFragmentTag);
    }

    public void setHolder(int resId){
        holder = resId;
    }
}
