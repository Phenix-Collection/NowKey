package com.kuding.superball;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.R;
import com.kuding.superball.floatview.CustomDialog;
import com.kuding.superball.floatview.NowKeyPanelModel;

/**
 * Created by user on 17-1-26.
 */

public class WarningActivity extends Activity {
    public static final String TITLE = "title";
    public static final String PAGE = "page";
    public static final String CONTENT = "content";
    public static final String INFO = "info";
    private boolean hasRun = false;

    private CustomDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_layout);
        if (!hasRun) {
            hasRun = true;
            showDialog(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!hasRun) {
            hasRun = true;
            showDialog(getIntent());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hasRun = false;
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mDialog = null;
    }

    private void showDialog(Intent intent) {
        if (intent == null) finish();
        final BaseItemInfo info = intent.getParcelableExtra(INFO);
        if (info == null) finish();
        final int page = intent.getIntExtra(PAGE, -1);
        if (page == -1) finish();
        String title = intent.getStringExtra(TITLE);
        String content = intent.getStringExtra(CONTENT);
        CustomDialog.Builder dialog = new CustomDialog.Builder(this);
        if (title != null) {
            dialog.setTitle(title);
        }
        if (content != null) {
            dialog.setMessage(content);
        }
        mDialog = dialog.setPositiveButton(R.string.now_key_item_delete_warning_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NowKeyPanelModel.getInstance().deleteItem(info, page);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(R.string.now_key_item_delete_warning_no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        mDialog.show();
    }
}
