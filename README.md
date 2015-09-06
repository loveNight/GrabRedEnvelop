

##存在的问题
- 无法判断一个红包是否已经被领取过，更深层次的原因是切换界面时，无法区别不同的红包。所以目前只能做到领取最新红包。
1.使用AccessibilityNodeInfo获取的红包消息中不包含时间，无法使用时间来区别。
2.这个AccessibilityNodeInfo对象又会被重复用在不同的红包上，无法使用对象的ClassName+hashCode来区别。
    <1>同一个聊天界面中同时显示的不同红包，用不同的AccessibilityNodeInfo对象表示，但是每次切换到该界面，这些对象代表的红包都会变化
    <1>同一个聊天界面中不能同时显示的两个不同红包，用相同的AccessibilityNodeInfo对象表示
    
- 当前聊天界面收到红包时，不会被触发。
尝试用AccessibilityEvent.TYPE_VIEW_CLICKED事件来检查新红包，但是在红包详情界面一样会触发此事件，涉及红包重复判断的逻辑，暂时取消此事件的监听

##事件测试
####在聊天界面收到当前好友的一条消息时：
触发事件---->TYPE_WINDOW_CONTENT_CHANGED
事件的包名---->com.tencent.mm
事件的类名---->android.widget.ListView
事件的文本---->
触发事件---->TYPE_VIEW_SCROLLED
事件的包名---->com.tencent.mm
事件的类名---->android.widget.ListView
事件的文本---->
触发事件---->TYPE_WINDOW_CONTENT_CHANGED
事件的包名---->com.tencent.mm
事件的类名---->android.widget.ListView
事件的文本---->
触发事件---->TYPE_WINDOW_CONTENT_CHANGED
事件的包名---->com.tencent.mm
事件的类名---->android.widget.ListView
事件的文本---->
触发事件---->TYPE_WINDOW_CONTENT_CHANGED
事件的包名---->com.tencent.mm
事件的类名---->android.widget.ListView
事件的文本---->

####在聊天界面收到其他好友的一条消息时：
触发事件---->TYPE_WINDOW_CONTENT_CHANGED
事件的包名---->com.tencent.mm
事件的类名---->android.widget.ListView
事件的文本---->
触发事件---->TYPE_NOTIFICATION_STATE_CHANGED
事件的包名---->com.tencent.mm
事件的类名---->android.app.Notification
事件的文本---->好友昵称: 测试消息

####换了Activity，才会触发AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED。而从微信的消息列表界面到聊天界面，并不触发此事件，说明没有切换Activity
