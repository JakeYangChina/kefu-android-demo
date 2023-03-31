package com.hyphenate.helpdesk.easeui.ui;

import com.hyphenate.helpdesk.easeui.util.EaseUiReportDataUtils;

public class AutoReportUtils {

    // TODO 注：当sIsAutoReport设置为false 时此 AutoReportUtils类不生效，不会再自动上报
    //  这时需要用户在自己工程里自行调用如下代码进行开启和关闭上报
    //  EaseUiReportDataUtils.getEaseUiReportDataUtils().startReport();
    //  EaseUiReportDataUtils.getEaseUiReportDataUtils().stopReport();
    public final static boolean sIsAutoReport = true;


    private static AutoReportUtils sAutoReportUtils;
    public static AutoReportUtils getAutoReportUtils() {
        if (sAutoReportUtils == null){
            synchronized (AutoReportUtils.class){
                if (sAutoReportUtils == null){
                    sAutoReportUtils = new AutoReportUtils();
                }
            }
        }
        return sAutoReportUtils;
    }

    public void startReport(){
        if (sIsAutoReport){
            EaseUiReportDataUtils.getEaseUiReportDataUtils().startReport();
        }
    }

    public void stopReport(){
        if (sIsAutoReport){
            EaseUiReportDataUtils.getEaseUiReportDataUtils().stopReport();
        }
    }

    public void destroy(){
        if (sIsAutoReport){
            EaseUiReportDataUtils.getEaseUiReportDataUtils().destroy();
        }
    }
}
