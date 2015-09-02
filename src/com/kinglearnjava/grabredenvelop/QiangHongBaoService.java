package com.kinglearnjava.grabredenvelop;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.os.Build;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

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
    
    // 已领取红包
    private Set<String> robbedBonus = new HashSet<String>();
    
    private Handler handler = new Handler();

    /**
     * 必须重载的方法
     * 接收系统发来的AccessbilityEvent，已经按照配置文件过滤
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        LogUtil.d(TAG, "onAccessibilityEvent-----现在触发的事件---->" + event);
        
        // 通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            // 通知栏出现新信息
            // 获取通知栏信息内容
            List<CharSequence> texts = event.getText();
            // 检查是否有红包信息
            if (!texts.isEmpty()) {
                for (CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if (text.contains(HONGBAO_TEXT_KEY)) {
                        openNotify(event);
                        break;
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // 窗口改变（不一定是微信，下面会判断界面），则调动打开红包方法 
            openHongBao(event);
        }
    }

    
    /**
     * 必须重载的方法 
     * 系统想要中断AccessibilityService返给的响应时会调用
     * 生命周期中会调用多次
     */
    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    
    /**
     * 可选的方法 
     * 系统会在成功连接上服务时候调用这个方法
     * 在这个方法里你可以做一下初始化工作
     * 例如设备的声音震动管理，也可以调用setServiceInfo()进行配置工作。
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
    }
    
    private void sendNotifyEvent() {
        AccessibilityManager manager = (AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setPackageName(WECHAT_PACKAGENAME);
        event.setClassName(Notification.class.getName());
        CharSequence tickerText = HONGBAO_TEXT_KEY;
        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }
    
    /**
     * 打开通知栏消息
     */
    private void openNotify(AccessibilityEvent event){
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        // 将微信的通知栏消息打开
        // 获取Notification对象 
        Notification notification = (Notification) event.getParcelableData();
        // 调用其中的PendingIntent，打开微信界面
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 打开微信后，判断是什么界面，并做相应的动作
     */
    private void openHongBao(AccessibilityEvent event) {
        // 领取红包界面
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            // 领取红包
            getPacket();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            // 拆完红包后看详细的纪录界面
            // 什么都不做
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            // 聊天界面
            openPacket();
        }
    }
    
    /**
     * 拆红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            LogUtil.w(TAG, "getPacket()------rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
        for (AccessibilityNodeInfo n :list) {
            LogUtil.v(TAG + "拆红包", "getPacket()-------->微信红包--------->" + n);
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }
    
    /**
     * 在聊天界面中点红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            LogUtil.w(TAG + "聊天界面", "openPacket()-------->rootWindow为空");
            return;
        }
        // 找到领取红包的点击事件
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
            for(AccessibilityNodeInfo n : list) {
                LogUtil.i(TAG + "聊天界面", "openPacket()-->微信红包:使用HONGBAO_TEXT_KEY------>" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            // 最新的红包领起
            for (int i = list.size() - 1 ; i >= 0; i--) {
                // 通过调试可知[领取红包]是text，本身不可被点击，用getParent()获取可被点击的对象
                AccessibilityNodeInfo parent = list.get(i).getParent();
                LogUtil.i(TAG + "聊天界面", "openPacket()---->遍历红包:输出parent------->" + parent);
                LogUtil.i(TAG + "聊天界面", "openPacket()---->遍历红包:输出list------->" + list.get(i));
//                LogUtil.e(TAG, "输出Set--->" + robbedBonus);
//                // 谷歌重写了toString()方法，不能用它获取ClassName@hashCode串
//                if ( (parent != null) && (!robbedBonus.contains(getOriginToString(parent))) ) {
//                    robbedBonus.add(getOriginToString(parent));
//                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    LogUtil.e(TAG, "点击红包：" + getOriginToString(parent));
//                    LogUtil.e(TAG, "输出Set--->" + robbedBonus);
//                    break; // 只领最新的一个红包
//                }
            }
        }
    }
    
    /**
     * Java默认的toString()方法，用于识别是否同一个对象
     * Android重写了toString()，故自己实现一个
     */
    private String getOriginToString(Object o) {
        return o.getClass().getName() + '@' + Integer.toHexString(o.hashCode());
    }

}
