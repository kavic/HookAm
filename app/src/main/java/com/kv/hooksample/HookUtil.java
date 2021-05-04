package com.kv.hooksample;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by tanjunzhao on 5/4/21.
 */
public class HookUtil {

    private static final String TAG = "HookUtil";

    public static void hook() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        Field field;
        Class<?> targetClass;

        //android 10
        if (Build.VERSION.SDK_INT > 28) {
            Class<?> amnClass = Class.forName("android.app.ActivityTaskManager");
            field = amnClass.getDeclaredField("IActivityTaskManagerSingleton");
            targetClass = Class.forName("android.app.IActivityTaskManager");
        }

        //android 8.0 - 9.0
        else if (Build.VERSION.SDK_INT > 25) {
            Class<?> amnClass = Class.forName("android.app.ActivityManager");
            field = amnClass.getDeclaredField("IActivityManagerSingleton");
            targetClass = Class.forName("android.app.IActivityManager");
        }

        else {
            Class<?> amnClass = Class.forName("android.app.ActivityManagerNative");
            field = amnClass.getDeclaredField("gDefault");
            targetClass = Class.forName("android.app.IActivityManager");
        }

        field.setAccessible(true);

        Object gDefault = field.get(null);

        //反射Singleton
        Class<?> singletonClass = Class.forName("android.util.Singleton");
        Field mInstanceField = singletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Object instance = mInstanceField.get(gDefault);

        ActivityManagerHandler activityManagerHandler = new ActivityManagerHandler(instance);
        Object amProxy = Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{targetClass},
                activityManagerHandler);

        mInstanceField.set(gDefault, amProxy);
    }

    private static class ActivityManagerHandler implements InvocationHandler {
        private Object am;

        public ActivityManagerHandler(Object am) {
            this.am = am;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if ("startActivity".equals(method.getName())) {
                for (Object arg : args) {
                    if (arg instanceof Intent) {
                        Intent intent = (Intent) arg;

                        Log.i(TAG, "action:" + intent.getAction());
                        Log.i(TAG, "data:" + intent.getDataString());

                        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                            //拦截activity
                        }

                        Class<?> clazz = intent.getClass();
                        Field field = clazz.getDeclaredField("mData");
                        field.setAccessible(true);
                        field.set(intent, Uri.parse("https://www.taobao.com"));

                    }
                }
            }
            return method.invoke(am, args);
        }
    }

}
