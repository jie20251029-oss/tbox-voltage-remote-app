package com.clj.fastble.permission;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import com.clj.fastble.R;


/**
 * Copyright 2017
 * <p>
 * 一句话功能简述
 * 功能详细描述
 *
 * @author wangzhipeng
 * @version 0.1 2017/7/3 8:51
 */
public class ZSLCenterDialog extends Dialog {
    private boolean mIsRound = false;
    private Context mContext;

    public ZSLCenterDialog( Context context) {
        super(context);
        this.mContext = context;
    }

    public ZSLCenterDialog( Context context,  int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    protected ZSLCenterDialog( Context context, boolean cancelable,  OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }


    public ZSLCenterDialog(int layout, Context context) {
        super(context);
        __initDialog(layout);
    }

    public ZSLCenterDialog(int layout, Context context, boolean isRound) {
        super(context, R.style.MyDialogStyle);
        this.mIsRound = isRound;
        __initDialog(layout);
    }

    private void __initDialog(int layout) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layout);
        Window window = getWindow();
        if (!mIsRound) {
            setCanceledOnTouchOutside(true);
            window.setBackgroundDrawableResource(R.drawable.dialog_bg_circle);
        } else {
            setCanceledOnTouchOutside(false);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }
}
