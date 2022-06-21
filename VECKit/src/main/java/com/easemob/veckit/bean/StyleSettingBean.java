package com.easemob.veckit.bean;

import android.os.Parcel;

public class StyleSettingBean{
    // 初始页提示语
    private String waitingPrompt;
    // 初始页背景图片
    private String waitingBackgroundPic;
    // 呼叫页提示语
    private String callingPrompt;
    private String callingBackgroundPic;
    // 等待页提示语
    private String queuingPrompt;
    private String queuingBackgroundPic;
    private String endingPrompt;
    private String endingBackgroundPic;

    public StyleSettingBean(){

    }

    public static StyleSettingBean create(){
        return new StyleSettingBean(
                "您好，有什么需要帮助，可以发起视频通话进行咨询呦！", "",
                "你好，您正在向环信发起视频通话进行咨询！", "",
                "您好！您前面还有4人等待，客服人员正在马不停蹄的赶过来，请您耐心等待！！", "",
                "感谢您的来电，祝您生活愉快！", "");
    }

    public StyleSettingBean(String initPrompt, String initBackgroundPic,
                            String callingPrompt, String callingBackgroundPic,
                            String queuingPrompt, String queuingBackgroundPic,
                            String endingPrompt, String endingBackgroundPic){
        this.waitingPrompt = initPrompt;
        this.waitingBackgroundPic = initBackgroundPic;
        this.callingPrompt = callingPrompt;
        this.callingBackgroundPic = callingBackgroundPic;
        this.queuingPrompt = queuingPrompt;
        this.queuingBackgroundPic = queuingBackgroundPic;
        this.endingPrompt = endingPrompt;
        this.endingBackgroundPic = endingBackgroundPic;
    }

    protected StyleSettingBean(Parcel in) {
        waitingPrompt = in.readString();
        waitingBackgroundPic = in.readString();
        callingPrompt = in.readString();
        callingBackgroundPic = in.readString();
        queuingPrompt = in.readString();
        queuingBackgroundPic = in.readString();
        endingPrompt = in.readString();
        endingBackgroundPic = in.readString();
    }

    public String getWaitingPrompt() {
        return waitingPrompt;
    }

    public void setWaitingPrompt(String waitingPrompt) {
        this.waitingPrompt = waitingPrompt;
    }

    public String getWaitingBackgroundPic() {
        return waitingBackgroundPic;
    }

    public void setWaitingBackgroundPic(String waitingBackgroundPic) {
        this.waitingBackgroundPic = waitingBackgroundPic;
    }

    public String getCallingPrompt() {
        return callingPrompt;
    }

    public void setCallingPrompt(String callingPrompt) {
        this.callingPrompt = callingPrompt;
    }

    public String getCallingBackgroundPic() {
        return callingBackgroundPic;
    }

    public void setCallingBackgroundPic(String callingBackgroundPic) {
        this.callingBackgroundPic = callingBackgroundPic;
    }

    public String getQueuingPrompt() {
        return queuingPrompt;
    }

    public void setQueuingPrompt(String queuingPrompt) {
        this.queuingPrompt = queuingPrompt;
    }

    public String getQueuingBackgroundPic() {
        return queuingBackgroundPic;
    }

    public void setQueuingBackgroundPic(String queuingBackgroundPic) {
        this.queuingBackgroundPic = queuingBackgroundPic;
    }

    public String getEndingPrompt() {
        return endingPrompt;
    }

    public void setEndingPrompt(String endingPrompt) {
        this.endingPrompt = endingPrompt;
    }

    public String getEndingBackgroundPic() {
        return endingBackgroundPic;
    }

    public void setEndingBackgroundPic(String endingBackgroundPic) {
        this.endingBackgroundPic = endingBackgroundPic;
    }

    @Override
    public String toString() {
        return "{" +
                "waitingPrompt='" + waitingPrompt + '\'' +
                ", waitingBackgroundPic='" + waitingBackgroundPic + '\'' +
                ", callingPrompt='" + callingPrompt + '\'' +
                ", callingBackgroundPic='" + callingBackgroundPic + '\'' +
                ", queuingPrompt='" + queuingPrompt + '\'' +
                ", queuingBackgroundPic='" + queuingBackgroundPic + '\'' +
                ", endingPrompt='" + endingPrompt + '\'' +
                ", endingBackgroundPic='" + endingBackgroundPic + '\'' +
                '}';
    }
}
