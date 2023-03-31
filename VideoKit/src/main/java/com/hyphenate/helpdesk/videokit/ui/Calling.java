package com.hyphenate.helpdesk.videokit.ui;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hyphenate.agora.FunctionIconItem;
import com.hyphenate.chat.AgoraMessage;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.helpdesk.callback.ValueCallBack;
import com.hyphenate.helpdesk.easeui.util.FlatFunctionUtils;
import com.hyphenate.helpdesk.videokit.R;
import com.hyphenate.helpdesk.videokit.permission.FloatWindowManager;

import java.util.List;

public class Calling {
    // 主动
    public static void callingRequest(Context context, String cecImServiceNumber){
        if (TextUtils.isEmpty(cecImServiceNumber)) {
            throw new RuntimeException("cecImServiceNumber is null.");
        }
        // 获取灰度
        getTenantIdFunctionIcons();
        // 发送视频邀请命令
        ChatClient.getInstance().callManager().callVideo(context.getString(R.string.em_chat_invite_video_call), cecImServiceNumber);
        if (FloatWindowManager.getInstance().checkPermission(context)){
            VideoCallWindowService.show(context, cecImServiceNumber);
        }else {
            CallActivity.show(context, cecImServiceNumber);
        }
    }


    // 被动
    public static void callingResponse(Context context, Intent intent){
        getTenantIdFunctionIcons();
        if (FloatWindowManager.getInstance().checkPermission(context)){
            VideoCallWindowService.show(context, intent);
        }else {
            CallActivity.show(context, intent);
        }
    }

    public static void getTenantIdFunctionIcons(){
        // 动态获取功能按钮，在视频页面使用到
        AgoraMessage.asyncGetTenantIdFunctionIcons(ChatClient.getInstance().tenantId(), new ValueCallBack<List<FunctionIconItem>>() {
            @Override
            public void onSuccess(List<FunctionIconItem> value) {
                FlatFunctionUtils.get().setIconItems(value);
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }
}
