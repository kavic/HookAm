package com.kv.hooksample;

import android.app.Application;

import me.weishu.reflection.Reflection;

/**
 * Created by tanjunzhao on 5/4/21.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Reflection.unseal(this);
    }
}
