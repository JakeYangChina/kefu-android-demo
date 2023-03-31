package com.easemob.veckit.utils;


import android.os.SystemClock;

import com.hyphenate.helpdesk.easeui.util.EaseUiReportDataUtils;
import com.hyphenate.helpdesk.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VecKitReportDataUtils {
    private final static String TAG = "ReportDataUtils";
    private static VecKitReportDataUtils sVecKitReportDataUtils;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private volatile boolean mIsStart = false;
    private volatile int mTime = 1000 * 5;
    private VecKitReportDataUtils(){}

    public static VecKitReportDataUtils getVecKitReportDataUtils() {
        if (sVecKitReportDataUtils == null){
            synchronized (EaseUiReportDataUtils.class){
                if (sVecKitReportDataUtils == null){
                    sVecKitReportDataUtils = new VecKitReportDataUtils();
                }
            }
        }
        return sVecKitReportDataUtils;
    }


    public void startReport(){
        if (mIsStart){
            return;
        }
        mIsStart = true;
        mIsNeedReport = true;
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                while (mIsStart){
                    // 调用接口
                    Log.e(TAG,"vec调用接口上报");
                    SystemClock.sleep(mTime);
                }
            }
        });
        Log.e(TAG,"vec startReport");
    }


    public void stopReport(){
        mIsStart = false;
        Log.e(TAG,"vec stopReport");
    }

    public void destroy(){
        mIsStart = false;
        mIsNeedReport = false;
        Log.e(TAG,"vec destroy");
    }


    private volatile boolean mIsNeedReport;
    // 前台上报
    public void onPageForegroundReport(){
        Log.e(TAG,"onPageForegroundReport");
        if (mIsNeedReport){
            startReport();
        }
    }

    // 后台上报
    public void onPageBackgroundReport(){
        Log.e(TAG,"onPageBackgroundReport");
        if (mIsNeedReport){
            mIsStart = false;
        }
    }
}
