package com.hyphenate.helpdesk.easeui.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AppStateEaseUiCallback implements Application.ActivityLifecycleCallbacks {
    private volatile static AppStateEaseUiCallback APP_STATE_CALLBACK;
    private Map<String, Integer> mActivityMap = new HashMap<>();
    private List<IAppStateEaseUiCallback> mIAppStateEaseUiCallbacks = new ArrayList<>();
    private volatile boolean mIsBackground;
    private AppStateEaseUiCallback(){}

    public static void init(Application context){
        if (APP_STATE_CALLBACK == null){
            synchronized (AppStateEaseUiCallback.class){
                if (APP_STATE_CALLBACK == null){
                    APP_STATE_CALLBACK = new AppStateEaseUiCallback(context);
                }
            }
        }
    }

    public static AppStateEaseUiCallback getAppStateEaseUiCallback() {
        return APP_STATE_CALLBACK;
    }

    @SuppressLint("ObsoleteSdkInt")
    private AppStateEaseUiCallback(Application context){
        // isSdk14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            context.registerActivityLifecycleCallbacks(this);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        mActivityMap.put(activity.getClass().getName(), 1);
        if(mActivityMap.size() == 1) {
            mIsBackground = false;
            if (mIAppStateEaseUiCallbacks != null){
                for (IAppStateEaseUiCallback iAppStateVecCallback : mIAppStateEaseUiCallbacks){
                    iAppStateVecCallback.onAppForeground();
                }
            }
        }

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        mActivityMap.remove(activity.getClass().getName());
        if (mActivityMap.isEmpty()) {
            mIsBackground = true;
            if (mIAppStateEaseUiCallbacks != null){
                for (IAppStateEaseUiCallback iAppStateVecCallback : mIAppStateEaseUiCallbacks){
                    iAppStateVecCallback.onAppBackground();
                }
            }

        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public void registerIAppStateEaseUiCallback(IAppStateEaseUiCallback callback){
        synchronized (AppStateEaseUiCallback.class){
            mIAppStateEaseUiCallbacks.add(callback);
        }
    }

    public void unRegisterIAppStateEaseUiCallback(IAppStateEaseUiCallback callback){
        synchronized (AppStateEaseUiCallback.class){
            mIAppStateEaseUiCallbacks.remove(callback);
        }
    }

    // 是否后台运行
    public boolean isBackground() {
        return mIsBackground;
    }

    public interface IAppStateEaseUiCallback {
        void onAppForeground();
        void onAppBackground();
    }

}
