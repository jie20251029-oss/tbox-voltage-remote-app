package com.clj.fastble.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

import com.clj.fastble.BleManager;
import com.clj.fastble.permission.listener.BLEInitListener;
import com.clj.fastble.permission.listener.WZPPermissionInterface;
import com.clj.fastble.utils.BleLog;

import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * Copyright 2024
 *
 * @author KA业务板块-IOT及可视化服务产品部-平台开发组-可视化组-> wzp_vicky
 * <p>
 * 一句话功能简述
 * 功能详细描述
 * @version 1.0 2024/1/12 14:10
 * @contact wangzhipeng@e6yun.com
 */
public abstract class WZPBLEHelperInit {
    public Activity mActivity;
    public boolean isDebug = true;

    private static WZPBLEHelperInit mBLECore;


    private final int REQUEST_PERMISSION_LOCATION = 1001;
    public BleManager mBLEManager;
    private boolean mIsNeedImmediateCheckDevice = false;//类似于蓝葑，在第一个页面不需要立即检查蓝牙
    private boolean mIsPermissionDeniedTip = true;

    public BLEInitListener mInitListener;
    private ZSLProcessPermissionUtil mTipDialog;


    //
    public WZPBLEHelperInit(Activity activity) {
        this.mActivity = activity;

        mBLEManager = BleManager.getInstance();
    }

    // 父类工厂
    public static WZPBLEHelperInit createAnimal(Class<? extends WZPBLEHelperInit> clazz) throws IllegalAccessException, InstantiationException {
        mBLECore = clazz.newInstance();
        return mBLECore;
    }

    public WZPBLEHelperInit isNeedImmediateCheckDevice(boolean isNeed) {
        this.mIsNeedImmediateCheckDevice = isNeed;
        return this;
    }

    public WZPBLEHelperInit isPermissionDeniedTip(boolean isNeed) {
        this.mIsPermissionDeniedTip = isNeed;
        return this;
    }

    public WZPBLEHelperInit setBLEInitListener(BLEInitListener listener) {
        this.mInitListener = listener;
        return this;
    }

    public WZPBLEHelperInit enableLog(boolean enable) {
        isDebug = enable;
        return this;
    }

    public void checkPermission(Object object) {
        Context context = null;
        if (object instanceof Context) {
            context = (Context) object;
        } else if (object instanceof android.app.Fragment) {
            context = ((android.app.Fragment) object).getActivity();
        } else if (object instanceof Fragment) {
            context = ((Fragment) object).getActivity();
        }
        if (context == null) return;

        Context finalContext = context;

        if (!isLocationEnabled(context)) {
            if (mInitListener != null) {
                mInitListener.inspectFail(102, "未打开位置服务开关");
                if (mTipDialog == null) {
                    mTipDialog = new ZSLProcessPermissionUtil(finalContext, mActivity);
                }
                mTipDialog.showDialog(mInitListener);
            }
            return;
        }

        String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (WZPPermissionUtils.hasSelfPermissions(context, locationPermission)) {
            if (mInitListener != null) {
                mInitListener.inspectSuccess(600, "位置授权成功");
            }
            BleLog.i("位置权限给到了--》是否立即检查手机蓝牙" + mIsNeedImmediateCheckDevice);
            if (mIsNeedImmediateCheckDevice) {
                check2OpenBluetooth();
            }
            return;
        }

        PermissionRequestActivity.getPermissionsRequest(context, REQUEST_PERMISSION_LOCATION, new WZPPermissionInterface() {
            @Override
            public void PermissionGranted() {
                if (mInitListener != null) {
                    mInitListener.inspectSuccess(600, "位置授权成功");
                }
                BleLog.i("位置权限给到了--》是否立即检查手机蓝牙" + mIsNeedImmediateCheckDevice);
                if (mIsNeedImmediateCheckDevice) {
                    check2OpenBluetooth();
                }
            }

            @Override
            public void PermissionDenied(int requestCode, List<String> denyList) {
                if (mIsPermissionDeniedTip) {

                    if (mTipDialog == null) {
                        mTipDialog = new ZSLProcessPermissionUtil(finalContext, mActivity);
                    }
                    mTipDialog.showDialog("位置", finalContext.getPackageName());

                }
                if (mInitListener != null) {
                    mInitListener.inspectFail(605, "拒绝位置授权");
                }

            }

            @Override
            public void PermissionCanceled(int requestCode) {
                if (mInitListener != null) {
                    mInitListener.inspectFail(604, "取消位置授权");
                }


            }
        }, locationPermission);


    }

    /**
     * 检查蓝牙开关
     * <p>
     * 只有在Android 4.3版本以上才可以使用BLE功能
     */
    public void check2OpenBluetooth() {
        initBleSdk();
        int code = -1;
        int successCode = -1;
        String msg = "";
        if (mBLEManager.getBluetoothAdapter() == null) {
            code = 104;
            msg = "您的手机不支持蓝牙功能";

        } else if (!mBLEManager.isSupportBle()) {
            code = 105;
            msg = "不支持低功耗蓝牙";
        } else if (!mBLEManager.isBlueEnable()) {
            PermissionRequestActivity.enableBluetooth(mActivity, mInitListener);
        } else {
            successCode = 100;

        }


        if (mInitListener != null) {
            if (code != -1) {
                mInitListener.inspectFail(code, msg);
            }
            if (successCode != -1) {
                mInitListener.inspectSuccess(successCode, "手机检查成功");
                if (mIsNeedImmediateCheckDevice) {
                    start2Scan();
                }
            }
        }
    }

    protected abstract void initBleSdk();

    protected abstract void start2Scan();

    /**
     * 检查手机是否开启位置信息服务
     *
     * @return
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }
}
