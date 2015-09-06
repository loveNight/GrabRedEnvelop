package com.kinglearnjava.grabredenvelop;

import java.util.List;

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
 * 对照 https://github.com/lendylongli/qianghongbao 所写，有改进
 */
public class QiangHongBaoService extends AccessibilityService {
    
    // 调试用TAG
    private static final String TAG = "QiangHongBao";
    private static final String TAG_EVENT_TEST = "事件测试";
    private static final String TAG_NODE = "AccessibilityNodeInfo测试";
    
    // 微信包名
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    
    // 通知栏红包消息关键字
    private static final String HONGBAO_TEXT_KEY = "[微信红包]";
    
    // 拆红包界面的按钮关键字
    private static final String GET_HONGBAO_KEY = "拆红包";
    
    // 聊天界面别人发的红包的关键字
    private static final String OPEN_OTHERS_HONGBAO_KEY = "领取红包";
    
    // 聊天界面自己发的红包的关键字
    private static final String OPEN_SELF_HONGBAO_KEY = "查看红包";
    
    // 上一个界面是红包详情
    // 退出红包详情界面，进入聊天界面，触发TYPE_WINDOW_STATE_CHANGED事件，有两种情况：
    // 一、界面中有红包，如果此标记为true，则不领取，同时把此标记改为false
    // 二、界面中无红包，则把此标记改为false
    private boolean lastWindowHongBaoDetail = false;
    
    private Handler handler = new Handler();

    /**
     * 必须重载的方法
     * 接收系统发来的AccessbilityEvent，已经按照配置文件过滤
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        LogUtil.d(TAG, "onAccessibilityEvent-----现在触发的事件---->" + event);
        
        // 测试用，看看微信中各种动作会触发什么事件
        String eventText = "";
        String eventPackageName = String.valueOf(event.getPackageName());
        List<CharSequence> eventTexts = event.getText(); 
        String eventClassName = String.valueOf(event.getClassName());
        switch (eventType) {  
        case AccessibilityEvent.TYPE_VIEW_CLICKED:  
            eventText = "TYPE_VIEW_CLICKED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_FOCUSED:  
            eventText = "TYPE_VIEW_FOCUSED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:  
            eventText = "TYPE_VIEW_LONG_CLICKED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_SELECTED:  
            eventText = "TYPE_VIEW_SELECTED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:  
            eventText = "TYPE_VIEW_TEXT_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:  
            eventText = "TYPE_WINDOW_STATE_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:  
            eventText = "TYPE_NOTIFICATION_STATE_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:  
            eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_END";  
            break;  
        case AccessibilityEvent.TYPE_ANNOUNCEMENT:  
            eventText = "TYPE_ANNOUNCEMENT";  
            break;  
        case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:  
            eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_START";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:  
            eventText = "TYPE_VIEW_HOVER_ENTER";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:  
            eventText = "TYPE_VIEW_HOVER_EXIT";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_SCROLLED:  
            eventText = "TYPE_VIEW_SCROLLED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:  
            eventText = "TYPE_VIEW_TEXT_SELECTION_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:  
            eventText = "TYPE_WINDOW_CONTENT_CHANGED";  
            break;  
        }  
        LogUtil.i(TAG_EVENT_TEST, "触发事件---->" + eventText);
        LogUtil.i(TAG_EVENT_TEST, "事件的包名---->" + eventPackageName);
        LogUtil.i(TAG_EVENT_TEST, "事件的类名---->" + eventClassName);
        LogUtil.i(TAG_EVENT_TEST, "事件的文本---->");
        for (CharSequence s : eventTexts) {
            LogUtil.i(TAG_EVENT_TEST,  String.valueOf(s));
        }
        
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
            // 所监听的APP的Activity改变时，触发AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
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
    
    /**
     * 发送通知栏信息，模拟微信红包信息。
     */
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
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            // 拆红包界面
            getPacket();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            // 拆完红包后看详细的纪录界面
            LogUtil.e(TAG, "红包详情界面，改变lastWindowHongBaoDetail，此时值为" + lastWindowHongBaoDetail);
            lastWindowHongBaoDetail = true;
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName()) ) {
            // 聊天界面
            openPacket();
//            // 测试用，输出当前界面AccessibilityNodeInfo对象的所有信息
//            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//            recycle(nodeInfo);
        } 
    }
    
    /**
     * 在拆红包界面中打开红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            LogUtil.w(TAG, "getPacket()------rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(GET_HONGBAO_KEY);
        for (AccessibilityNodeInfo n :list) {
            LogUtil.v(TAG + "拆红包", "getPacket()-------->微信红包--------->" + n);
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }
    
    /**
     * 在聊天界面中打开红包
     * 由AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED触发 
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        LogUtil.d(TAG, "RootWindow的ClassName:" + nodeInfo.getClassName());
        if (nodeInfo == null) {
            LogUtil.w(TAG + "聊天界面", "openPacket()-------->rootWindow为空");
            return;
        }
        // 找到领取红包的点击事件，别人发的红包
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(OPEN_OTHERS_HONGBAO_KEY);
        // 可能是自己发的红包
        if (list.isEmpty()) {            
            list = nodeInfo.findAccessibilityNodeInfosByText(OPEN_SELF_HONGBAO_KEY);
        } 
        // 仍没有红包或刚领完红包返回，则不领取，并改变标记
        if (list.isEmpty() || lastWindowHongBaoDetail) {
            LogUtil.e(TAG, "聊天界面，改变lastWindowHongBaoDetail，此时值为" + lastWindowHongBaoDetail);
            lastWindowHongBaoDetail = false;
            return;
        } else {
            // 当前界面可能有多个红包，只领最新的。
            for (int i = list.size() - 1 ; i >= 0; i--) {
                // 通过调试可知[领取红包]是text，本身不可被点击，用getParent()获取可被点击的对象
                AccessibilityNodeInfo parent = list.get(i).getParent();
                // 加个判断，以免空指针
                if ( parent != null) {
                    LogUtil.e(TAG, "点击红包：" + getOriginToString(parent));
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break; // 只领最新的一个红包
                }
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
    
    /**
     * 遍历AccessibilityNodeInfo并输出
     * @param nodeInfo
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void recycle(AccessibilityNodeInfo nodeInfo) {
        LogUtil.i(TAG_NODE, "------------------------------------------");
        LogUtil.i(TAG_NODE, "nodeInfo.getChildCount()----->" + nodeInfo.getChildCount());
        LogUtil.i(TAG_NODE, "nodeInfo.getWindowId()----->" + nodeInfo.getWindowId());
        LogUtil.i(TAG_NODE, "nodeInfo.getClassName()----->" + nodeInfo.getClassName());
        LogUtil.i(TAG_NODE, "nodeInfo.getContentDescription()----->" + nodeInfo.getContentDescription());
        LogUtil.i(TAG_NODE, "nodeInfo.getPackageName()----->" + nodeInfo.getPackageName());
        LogUtil.i(TAG_NODE, "nodeInfo.getText()----->" + nodeInfo.getText());
//        LogUtil.i(TAG_NODE, "nodeInfo.getViewIdResourceName()----->" + nodeInfo.getViewIdResourceName());
//        LogUtil.i(TAG_NODE, "nodeInfo.getActions()----->" + nodeInfo.getActions());
//        LogUtil.i(TAG_NODE, "nodeInfo.getMovementGranularities()----->" + nodeInfo.getMovementGranularities());
//        LogUtil.i(TAG_NODE, "nodeInfo.getgetTextSelectionEnd()----->" + nodeInfo.getTextSelectionEnd());
//        LogUtil.i(TAG_NODE, "nodeInfo.getTextSelectionStart()----->" + nodeInfo.getTextSelectionStart());
//        LogUtil.i(TAG_NODE, "nodeInfo.getViewIdResourceName()----->" + nodeInfo.getViewIdResourceName());
//        LogUtil.i(TAG_NODE, "nodeInfo.getLabeledBy()----->" + nodeInfo.getLabeledBy());
//        LogUtil.i(TAG_NODE, "nodeInfo.getLabelFor()----->" + nodeInfo.getLabelFor());
        if (nodeInfo.getChildCount() > 0) {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                recycle(nodeInfo.getChild(i));
            }
        }
    }

}
