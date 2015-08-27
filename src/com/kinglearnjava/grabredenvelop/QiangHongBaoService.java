package com.kinglearnjava.grabredenvelop;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;

/**
 * 抢红包外挂服务
 * 对照 https://github.com/lendylongli/qianghongbao 所写
 */
public class QiangHongBaoService extends AccessibilityService {
    
    private static final String TAG = "QiangHongBao";
    
    // 微信包名
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    
    // 红包消息关键字
    private static final String HONGBAO_TEXT_KEY = "[微信红包]";
    
    private Handler handler = new Handler();

    /**
     * 必须重载的方法
     * 接收系统发来的AccessbilityEvent，已经按照配置文件过滤
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        
    }

    @Override
    public void onInterrupt() {
    }

}
