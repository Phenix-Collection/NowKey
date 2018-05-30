package com.kuding.superball.help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuding.superball.R;

/**
 * Created by user on 17-3-20.
 */

public class HelpEditorFragment extends Fragment {
    private GifView mEditGif;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = ((NowKeyHelpActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.help_edit_title);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.now_key_help_editor_fragment, container, false);
        mEditGif = (GifView) content.findViewById(R.id.help_edit_gif);
        initGif();
        return content;
    }

    private void initGif() {
        //mEditGif.setMovieResource(R.raw.help_access_to_edit);
    }
}
