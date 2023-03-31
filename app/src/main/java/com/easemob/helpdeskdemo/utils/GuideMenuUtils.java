package com.easemob.helpdeskdemo.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.Toast;

import com.easemob.helpdeskdemo.Preferences;
import com.easemob.helpdeskdemo.R;
import com.easemob.veckit.VECKitCalling;
import com.easemob.veckit.utils.Utils;
import com.google.gson.Gson;
import com.hyphenate.chat.AgoraMessage;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.helpdesk.callback.ValueCallBack;
import com.hyphenate.helpdesk.easeui.util.AppStateEaseUiCallback;
import com.hyphenate.helpdesk.easeui.util.EaseUiReportDataUtils;
import com.hyphenate.helpdesk.model.TransferGuideMenuInfo;
import com.hyphenate.helpdesk.util.Log;
import com.hyphenate.helpdesk.videokit.ui.Calling;

public class GuideMenuUtils extends BroadcastReceiver {
    private final static String TAG = "GuideMenuUtils";
    private static GuideMenuUtils sGuideMenuUtils;

    public static GuideMenuUtils getGuideMenuUtils() {
        if (sGuideMenuUtils == null){
            synchronized (GuideMenuUtils.class){
                if (sGuideMenuUtils == null){
                    sGuideMenuUtils = new GuideMenuUtils();
                }
            }
        }
        return sGuideMenuUtils;
    }

    public static void sendBroadcast(Context context, TransferGuideMenuInfo.Item item){
        // TransferGuideMenuInfo.Item
        try {
            Gson gson = new Gson();
            Intent intent = new Intent("guide.menu.item.action");
            intent.putExtra("data",gson.toJson(item));
            context.getApplicationContext().sendBroadcast(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 需要在对应的聊天页面注册广播
    public void registerReceiver(Context context){
        IntentFilter filter = new IntentFilter("guide.menu.item.action");
        context.getApplicationContext().registerReceiver(this, filter);
        Log.e(TAG,"registerReceiver");
    }

    // 需要在对应的聊天页面注销广播
    public void unregisterReceiver(Context context){
        context.getApplicationContext().unregisterReceiver(this);
        Log.e(TAG,"unregisterReceiver");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            String data = intent.getStringExtra("data");
            Gson gson = new Gson();
            TransferGuideMenuInfo.Item item = gson.fromJson(data, TransferGuideMenuInfo.Item.class);
            Log.e(TAG,"data = "+data);
            if (item.getQueueType().equalsIgnoreCase("video")){
                if (TextUtils.isEmpty(ChatClient.getInstance().getConfigId())){
                    Toast.makeText(context, Utils.getString(context, R.string.vec_config_setting), Toast.LENGTH_LONG).show();
                    return;
                }
                closeCec();
                VECKitCalling.callingRequest(context, AgoraMessage.newAgoraMessage().getVecImServiceNumber(), ChatClient.getInstance().getConfigId());
            }else if (item.getQueueType().equalsIgnoreCase("txt")){
                Calling.callingRequest(context, Preferences.getInstance().getCustomerAccount());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void closeCec(){
        String imService = Preferences.getInstance().getCustomerAccount();
        Log.e(TAG,"imService = "+imService);
        ChatClient.getInstance().chatManager().asyncVisitorId(imService, new ValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Log.e(TAG,"asyncVisitorId = "+value);
                String visitorId = value;
                getSessionIdFromMessage(imService, new ValueCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                        Log.e(TAG,"getCurrentSessionId = "+value);
                        String tenantId = ChatClient.getInstance().tenantId();
                        ChatClient.getInstance().chatManager().asyncCecClose(tenantId, visitorId, value, new ValueCallBack<String>() {
                            @Override
                            public void onSuccess(String value) {
                                Log.e(TAG,"getSessionIdFromMessage = "+value);
                            }

                            @Override
                            public void onError(int error, String errorMsg) {
                                Log.e(TAG,"asyncCecClose errorMsg = "+errorMsg);
                            }
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        Log.e(TAG,"getCurrentSessionId errorMsg = "+errorMsg);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                Log.e(TAG,"asyncVisitorId errorMsg = "+errorMsg);
            }
        });


    }

    private void getSessionIdFromMessage(String imService, ValueCallBack<String> callBack){
        ChatClient.getInstance().chatManager().getCurrentSessionId(imService, new ValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Log.e(TAG,"getCurrentSessionId = "+value);
                if (callBack != null){
                    callBack.onSuccess(value);
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                Log.e(TAG,"getCurrentSessionId errorMsg = "+errorMsg);
                if (callBack != null){
                    callBack.onError(error, errorMsg);
                }
            }
        });
    }
}
