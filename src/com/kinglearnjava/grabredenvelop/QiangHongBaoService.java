package com.kinglearnjava.grabredenvelop;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * 抢红包外挂服务
 * 对照 https://github.com/lendylongli/qianghongbao 所写
 */
public class QiangHongBaoService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

}
