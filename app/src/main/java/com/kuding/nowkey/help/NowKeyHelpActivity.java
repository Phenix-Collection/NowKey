package com.kuding.nowkey.help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.kuding.nowkey.R;

/**
 * Created by user on 17-3-20.
 */

public class NowKeyHelpActivity extends AppCompatActivity {

    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_key_help_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.now_key_option_help_title);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_layout, new HelpListFragment());
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if (mCurrentFragment != null) {
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setTitle(R.string.now_key_option_help_title);
                    }
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slide_to_right);
                    transaction.replace(R.id.fragment_layout, new HelpListFragment());
                    transaction.commit();
                    mCurrentFragment = null;
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment != null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.now_key_option_help_title);
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slide_to_right);
            transaction.replace(R.id.fragment_layout, new HelpListFragment());
            transaction.commit();
            mCurrentFragment = null;
        } else {
            finish();
        }
    }

    public void gotoDetail(Fragment fragment) {
        mCurrentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
        transaction.replace(R.id.fragment_layout, mCurrentFragment);
        transaction.addToBackStack(mCurrentFragment.toString());
        transaction.commit();
    }

}
