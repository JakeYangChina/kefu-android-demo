package com.hyphenate.helpdesk.easeui.util;


import android.os.SystemClock;

import com.hyphenate.helpdesk.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EaseUiReportDataUtils {
    private final static String TAG = "ReportDataUtils";
    private static EaseUiReportDataUtils sEaseUiReportDataUtils;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private volatile boolean mIsStart = false;
    private volatile int mTime = 1000 * 5;
    private EaseUiReportDataUtils(){}

    public static EaseUiReportDataUtils getEaseUiReportDataUtils() {
        if (sEaseUiReportDataUtils == null){
            synchronized (EaseUiReportDataUtils.class){
                if (sEaseUiReportDataUtils == null){
                    sEaseUiReportDataUtils = new EaseUiReportDataUtils();
                }
            }
        }
        return sEaseUiReportDataUtils;
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
                    Log.e(TAG,"调用接口上报");
                    SystemClock.sleep(mTime);
                }
            }
        });
        Log.e(TAG,"startReport");
    }


    public void stopReport(){
        mIsStart = false;
        Log.e(TAG,"stopReport");
    }

    public void destroy(){
        mIsStart = false;
        mIsNeedReport = false;
        Log.e(TAG,"destroy");
    }


    private volatile boolean mIsNeedReport;
    // 前台上报
    public void onPageForegroundReport(){
        //Log.e(TAG,"onPageForegroundReport");
        if (mIsNeedReport){
            startReport();
        }
    }

    // 后台上报
    public void onPageBackgroundReport(){
        //Log.e(TAG,"onPageBackgroundReport");
        if (mIsNeedReport){
            mIsStart = false;
        }
    }
}
