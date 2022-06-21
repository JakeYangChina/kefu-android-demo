package com.easemob.helpdeskdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.easemob.helpdeskdemo.utils.Calling;
import com.easemob.veckit.VECKitCalling;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.VecConfig;

import io.agora.rtc.RtcEngine;

/**
 * Created by liyuzhao on 11/01/2017.
 */

public class CallReceiver extends BroadcastReceiver {
    boolean mIsOnLine;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ChatClient.getInstance().isLoggedInBefore()){
            return;
        }
        Log.e("aaaaaaaaaaaa","onReceive");
        String action = intent.getAction();
        if ("calling.state".equals(action)){
            // 防止正在通话中，又新发来视频请求，isOnLine代表是否接通通话中
            mIsOnLine = intent.getBooleanExtra("state", false);
        }else {
            //call type
            String type = intent.getStringExtra("type");
            //call to
            // Parcelable zuoXiSendRequestObj = intent.getParcelableExtra("zuoXiSendRequestObj");

            if ("video".equals(type)){// video call
                if (!mIsOnLine){
                    /*context.startActivity(new Intent(context, CallActivity.class)
                            .putExtra("zuoXiSendRequestObj", zuoXiSendRequestObj)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));*/

                    //Calling.callingResponse(context, intent);
                    if (VecConfig.newVecConfig().isVecVideo()){
                        VECKitCalling.callingResponse(context, intent);
                    }else {
                        Calling.callingResponse(context, intent);
                    }

                    String to = intent.getStringExtra("to");
                    String from = intent.getStringExtra("from");
                    Log.e("yyyyyyyyy","to = "+to);
                    Log.e("yyyyyyyyy","from = "+from);
                }
            }

        }

    }
}
