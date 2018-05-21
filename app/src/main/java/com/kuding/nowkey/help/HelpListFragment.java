package com.kuding.nowkey.help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kuding.nowkey.R;

/**
 * Created by user on 17-3-20.
 */

public class HelpListFragment extends Fragment implements View.OnClickListener {

    private LinearLayout mHelpGestureLayout;
    private LinearLayout mHelpEditLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.now_key_help_list_fragment, container, false);
        mHelpGestureLayout = (LinearLayout) content.findViewById(R.id.now_key_help_gesure_layout);
        mHelpGestureLayout.setOnClickListener(this);
        mHelpEditLayout = (LinearLayout) content.findViewById(R.id.now_key_help_edit_layout);
        mHelpEditLayout.setOnClickListener(this);
        return content;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.now_key_help_gesure_layout:
                ((NowKeyHelpActivity) getActivity()).gotoDetail(new HelpGestureFragment());
                break;
            case R.id.now_key_help_edit_layout:
                ((NowKeyHelpActivity) getActivity()).gotoDetail(new HelpEditorFragment());
                break;
            default:
                break;
        }
    }
}
