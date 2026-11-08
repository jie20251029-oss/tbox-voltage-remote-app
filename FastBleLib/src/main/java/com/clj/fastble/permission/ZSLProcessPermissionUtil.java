package com.clj.fastble.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.clj.fastble.R;
import com.clj.fastble.permission.listener.BLEInitListener;


/**
 * Copyright 2017
 * <p>
 * 一句话功能简述
 * 功能详细描述
 *
 * @author wangzhipeng
 * @version 0.1 2017/8/11 14:58
 */
public class ZSLProcessPermissionUtil implements View.OnClickListener {
    private Context mContext;
    private Activity mActivity;
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案
    private String mPackageName;
    private ZSLCenterDialog mDialog;
    private BLEInitListener mInitListener;

    public ZSLProcessPermissionUtil(Context context, Activity activity) {
        mDialog = new ZSLCenterDialog(R.layout.dialog_permission, context);
        this.mContext = context;
        this.mActivity = activity;
    }

    public void showDialog(String type, String... pcakageName) {
        this.mPackageName = pcakageName[0];
        TextView tv = (TextView) mDialog.findViewById(R.id.dialog_txt_1);
        TextView tvEntify = (TextView) mDialog.findViewById(R.id.report);
        mDialog.findViewById(R.id.cancle).setOnClickListener(this);
        tvEntify.setOnClickListener(this);
        tvEntify.setText("设置");
        String msg = "由于您拒绝" + getAppName() + "的" + type + "权限,导致该功能无法正常使用。\n\r\n\r请点击\"设置\"-\"权限\"-打开所需权限。\n\r\n\r最后点击两次后退按钮，即可返回。";
        if (pcakageName.length > 1) {
            msg = "由于您拒绝" + getAppName() + "的" + type + "权限,导致" + pcakageName[1] + "\n\r\n\r请点击\"设置\"-\"权限\"-打开所需权限。\n\r\n\r最后点击两次后退按钮，即可返回。";
        }
        tv.setText(msg);
        mDialog.show();
    }

    public void showDialog(BLEInitListener initListener) {
        mInitListener=initListener;
        TextView tv = (TextView) mDialog.findViewById(R.id.dialog_txt_1);
        TextView tvEntify = (TextView) mDialog.findViewById(R.id.report);
        mDialog.findViewById(R.id.cancle).setOnClickListener(this);
        tvEntify.setOnClickListener(this);
        tvEntify.setText("去开启");
        tv.setText("由于您没有打开手机的位置信息开关，导致该功能无法正常使用"+"\n\r\n\r请点击\"去开启\"-打开访问我的位置信息。");
        mDialog.show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.cancle) {
            mDialog.cancel();
        } else if (id == R.id.report) {
            if(((TextView)view).getText().toString().equals("去开启")){
                PermissionRequestActivity.openLocationSwitch(mActivity,mInitListener);
            }else{
                startAppSettings();
            }
            mDialog.dismiss();
        }
    }

    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + mPackageName));
        mContext.startActivity(intent);
    }

    private synchronized String getAppName() {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    mContext.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return mContext.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}