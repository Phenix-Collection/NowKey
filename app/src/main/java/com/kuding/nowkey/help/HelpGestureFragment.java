package com.kuding.nowkey.help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuding.nowkey.R;

/**
 * Created by user on 17-3-20.
 */

public class HelpGestureFragment extends Fragment {
    private GifView mDoubleGif;
    private GifView mLongGif;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = ((NowKeyHelpActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.help_gesutre_title);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.now_key_help_gesture_fragment, container, false);
        mDoubleGif = (GifView) content.findViewById(R.id.help_double_click_gif);
        mLongGif = (GifView) content.findViewById(R.id.help_long_click_gif);
        initGif();
        return content;
    }

    private void initGif() {
        //mDoubleGif.setMovieResource(R.raw.help_gesture_double_click);
        //mLongGif.setMovieResource(R.raw.help_gesture_long_click);
    }
}
