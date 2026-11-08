package com.clj.fastble.permission;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;


import com.clj.fastble.R;
import com.clj.fastble.permission.listener.BLEInitListener;
import com.clj.fastble.permission.listener.WZPPermissionInterface;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;


/**
 * Copyright 2023
 *
 * @author KA业务板块-IOT及可视化服务产品部-平台开发组-可视化组-> wzp_vicky
 * <p>
 * 一句话功能简述
 * 功能详细描述
 * @version 1.0 2023/12/19 10:21
 * @contact wangzhipeng@e6yun.com
 */
public class PermissionRequestActivity extends Activity {
    private static WZPPermissionInterface mPermissionListener;
    public static BLEInitListener mInitListener;
    private String[] mPermissions;
    private static final String PERMISSION_KEY = "permission_key";
    private static final String REQUEST_CODE = "request_code";
    private int mRequestCode;

    public static void getPermissionsRequest(Context context, int requestCode, WZPPermissionInterface iPermission, String... permissions) {
        mPermissionListener = iPermission;


        Intent intent = new Intent(context, PermissionRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putStringArray(PERMISSION_KEY, permissions);
        bundle.putInt(REQUEST_CODE, requestCode);
        intent.putExtras(bundle);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }


    public static void enableBluetooth(Activity context, BLEInitListener initListener) {
        mInitListener = initListener;
        Intent intent = new Intent(context, PermissionRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putBoolean("is2EnableBluetooth", true);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void openLocationSwitch(Activity context,BLEInitListener initListener) {
        mInitListener = initListener;
        Intent intent = new Intent(context, PermissionRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putBoolean("is2OpenLocationSwitch", true);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_permission);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getBoolean("is2EnableBluetooth", false)) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0x01);
            return;
        }

        if (bundle != null && bundle.getBoolean("is2OpenLocationSwitch", false)) {
            Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(it, 0x02);
            return;
        }


        if (!WZPPermissionUtils.isOverMarshmallow()) {
            finish();
            overridePendingTransition(0, 0);
        }

        if (bundle != null) {
            mPermissions = bundle.getStringArray(PERMISSION_KEY);
            mRequestCode = bundle.getInt(REQUEST_CODE, 0);
        }
        if (mPermissions == null || mPermissions.length <= 0) {
            finish();
            return;
        }
        requestPermission(mPermissions);
    }

    //    ---------------------------------------------------------------------
    private void requestPermission(String[] permissions) {

        if (WZPPermissionUtils.hasSelfPermissions(this, permissions)) {
            //all permissions granted
            if (mPermissionListener != null) {
                mPermissionListener.PermissionGranted();
                mPermissionListener = null;
            }
            finish();
            overridePendingTransition(0, 0);
        } else {
            //request permissions
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);//去申请权限
        }
    }


    //**************************************权限申请的那个回调方法******************************************
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (WZPPermissionUtils.verifyPermissions(grantResults)) {
            //所有权限都同意
            if (mPermissionListener != null) {
                mPermissionListener.PermissionGranted();
            }
        } else {
            if (!WZPPermissionUtils.shouldShowRequestPermissionRationale(this, permissions)) {
                //权限被拒绝并且选中不再提示
                if (permissions.length != grantResults.length) return;
                List<String> denyList = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == -1) {
                        denyList.add(permissions[i]);
                    }
                }
                if (mPermissionListener != null) {
                    mPermissionListener.PermissionDenied(requestCode, denyList);
                }
            } else {
                //权限被取消
                if (mPermissionListener != null) {
                    mPermissionListener.PermissionCanceled(requestCode);
                }
            }

        }
        mPermissionListener = null;
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int code = -1;
        String msg = "";

        /**
         * ACTION_LOCATION_SOURCE_SETTINGS  即使打开了  需要自己关闭设置页面  就导致resultCode永远是0   针对这情况  我统一传106回去   然后走的也是inspectFail    这个不处理就行  下次点击还会走这个校验
         */
        if (requestCode == 0x02) {
//            code = resultCode == RESULT_OK ? 106 : 102;
//            msg = resultCode == RESULT_OK ? "未打开位置服务" : "位置服务打开了";
            code=106;
            msg ="进入了设置页面，只能自己查看是否开启成功";
        } else {
            code = resultCode == RESULT_OK ? 100 : 101;
            msg = resultCode == RESULT_OK ? "手机检查成功" : "蓝牙未打开";
        }
        if(mInitListener==null)return;
        if (resultCode == RESULT_OK) {
//            Toast.makeText(this, "蓝牙已启用", Toast.LENGTH_SHORT).show();
            mInitListener.inspectSuccess(code, msg);
        } else {
//            Toast.makeText(this, "蓝牙未启用", Toast.LENGTH_SHORT).show();
            mInitListener.inspectFail(code, msg);

        }

        finish();
        overridePendingTransition(0, 0);

    }
}
