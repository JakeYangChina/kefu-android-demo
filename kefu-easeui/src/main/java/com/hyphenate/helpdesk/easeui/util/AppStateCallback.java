package com.hyphenate.helpdesk.easeui.util;

import android.app.Application;


public class AppStateCallback implements AppStateEaseUiCallback.IAppStateEaseUiCallback {
    private static AppStateCallback sAppStateCallback;
    public static AppStateCallback getAppStateCallback() {
        if (sAppStateCallback == null){
            synchronized (AppStateCallback.class){
                if (sAppStateCallback == null){
                    sAppStateCallback = new AppStateCallback();
                }
            }
        }
        return sAppStateCallback;
    }


    public void init(Application context){
        AppStateEaseUiCallback.init(context);
        AppStateEaseUiCallback.getAppStateEaseUiCallback().registerIAppStateEaseUiCallback(this);
    }


    public void onDestroy(){
        AppStateEaseUiCallback.getAppStateEaseUiCallback().unRegisterIAppStateEaseUiCallback(this);
    }


    @Override
    public void onAppForeground() {
        EaseUiReportDataUtils.getEaseUiReportDataUtils().onPageForegroundReport();
    }

    @Override
    public void onAppBackground() {
        EaseUiReportDataUtils.getEaseUiReportDataUtils().onPageBackgroundReport();
    }
}
