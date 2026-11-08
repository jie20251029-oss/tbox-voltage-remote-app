package com.clj.fastble.permission.listener;

import java.util.List;

/**
 * Copyright  wzp (2018)
 * <p>
 * 一句话功能简述
 * 功能详细描述
 *
 * @author 王智鹏 623301600@qq.com
 * @version 0.1 2018/8/7 1:07
 */
public interface WZPPermissionInterface {
    //同意权限
    void PermissionGranted();

    //拒绝权限并且选中不再提示
    void PermissionDenied(int requestCode, List<String> denyList);

    //取消权限
    void PermissionCanceled(int requestCode);



}
