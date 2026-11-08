package com.clj.fastble.permission.listener;


/**
 * Copyright 2023
 *
 * @author KA业务板块-IOT及可视化服务产品部-平台开发组-可视化组-> wzp_vicky
 * <p>
 * 一句话功能简述
 * 功能详细描述
 * @version 1.0 2023/12/22 14:05
 * @contact wangzhipeng@e6yun.com
 */
public interface BLEInitListener {
    void inspectFail(int code, String errorMsg);

    void inspectSuccess(int code, String message);
}
