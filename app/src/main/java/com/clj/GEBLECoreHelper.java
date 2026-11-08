package com.clj;

import android.app.Activity;

import com.clj.fastble.BleManager;
import com.clj.fastble.permission.WZPBLEHelperInit;
import com.clj.fastble.utils.BleLog;

/**
 * Copyright 2024
 *
 * @author KA业务板块-IOT及可视化服务产品部-平台开发组-可视化组-> wzp_vicky
 * <p>
 * 一句话功能简述
 * 功能详细描述
 * @version 1.0 2024/1/12 14:52
 * @contact wangzhipeng@e6yun.com
 */
public class GEBLECoreHelper extends WZPBLEHelperInit {
    private static GEBLECoreHelper mBLECore;


    private GEBLECoreHelper(Activity activity) {
        super(activity);
    }

    @Override
    protected void initBleSdk() {
        mBLEManager.enableLog(isDebug) .logTag("蓝崶4.x-新->").setReConnectCount(1, 5000)//重连次数和重连间隔（毫秒）
                .setOperateTimeout(5000)// 设置操作超时时间，单位毫秒
                .setConnectOverTime(15000)//连接超时时间（毫秒）
//                .setSplitWriteNum(20)
                .init(mActivity.getApplication());
    }

    @Override
    public void start2Scan() {
        BleLog.i("准备开始搜索了");
    }

    public static GEBLECoreHelper getInstance(Activity activity) {

        if (mBLECore == null) {
            mBLECore = new GEBLECoreHelper(activity);
        }
        return mBLECore;
    }

//    public GEBLECoreHelper setBLECoreListener(BLEOperateListener listener) {
//        this.mOperateListener = listener;
//        mTTLockUtils = new G7E6TTDevice(mOperateListener);
//        return mBLECore;
//    }
}
