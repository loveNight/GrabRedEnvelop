package com.kinglearnjava.grabredenvelop;

import android.util.Log;

public class LogUtils {
    
    // 消息等级
    private static final int VERBOSE = 0;
    private static final int DEBUG = 1;
    private static final int INFO = 2;
    private static final int WARN = 3;
    private static final int ERROR = 4;
    private static final int ASSERT = 5;
    private static final int CANEL_LOG = 6;
    
    // 用来设置显示哪些等级的Log信息
    private static final int LEVEL = VERBOSE;
    
    public static void v(String tag, String msg){
        if (LEVEL >= VERBOSE) {
            Log.v(tag, msg);
        }
    }
    
    
}
