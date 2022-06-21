package com.easemob.veckit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hyphenate.chat.AgoraMessage;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.ChatManager;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.Message;
import com.hyphenate.chat.VecConfig;
import com.hyphenate.helpdesk.callback.ValueCallBack;
import com.hyphenate.helpdesk.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class VECKitCalling extends Activity {

    private String mToChatUserName;
    private ProgressDialog mDialog;

    // 主动请求
    public static void callingRequest(Context context, String toChatUserName){
        Intent intent = new Intent(context, VECKitCalling.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (TextUtils.isEmpty(toChatUserName)){
            toChatUserName = AgoraMessage.newAgoraMessage().getCurrentChatUsername();
        }
        intent.putExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME, toChatUserName);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }

    // 被动请求
    public static void callingResponse(Context context, Intent intent){
        CallVideoActivity.callingResponse(context, intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mToChatUserName = intent.getStringExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME);
        mDialog = new ProgressDialog(this);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.setMessage("正在加载...");
        mDialog.show();
        request();
    }

    private void request() {
        String tenantId = ChatClient.getInstance().tenantId();// "77556"
        String configId = ChatClient.getInstance().getConfigId();
        if (TextUtils.isEmpty(configId)){
            mDialog.dismiss();
            Toast.makeText(this, "请先到设置页面进行扫码关联用户获取configId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        AgoraMessage.asyncInitStyle(tenantId, configId, new ValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                try {
                    Log.e("aaaaaaaaaa","value = "+value);
                    JSONObject jsonObject = new JSONObject(value);
                    if (!jsonObject.has("status")){
                        return;
                    }
                    String status = jsonObject.getString("status");
                    if (!"ok".equalsIgnoreCase(status)){
                        return;
                    }

                    // 存起来
                    JSONObject entity = jsonObject.getJSONObject("entity");
                    if (isFinishing()){
                        return;
                    }

                    String json = entity.toString();
                    to(json);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(int error, String errorMsg) {
                Log.e("aaaaaaaaaaa","error = "+error+"， errorMsg = "+errorMsg);
                to("");
            }
        });
    }

    private void to(String json){
        runOnUiThread(() -> {
            mDialog.dismiss();
            CallVideoActivity.callingRequest(getApplicationContext(), mToChatUserName, json);
            finish();
        });
    }

    // 接通挂断
    public static void endCallFromOn(ValueCallBack<String> callBack){
        Log.e("rrrrrrrrrrrr","aaaa visitorId = "+VecConfig.newVecConfig().getVisitorId());
        AgoraMessage.closeVec(ChatClient.getInstance().tenantId(), VecConfig.newVecConfig().getSessionId(),
                VecConfig.newVecConfig().getVisitorId(), callBack);
    }

    // 未接通挂断
    public static void endCallFromOff(){
        String to = AgoraMessage.newAgoraMessage().getCurrentChatUsername();
        Message message = requestOnAndOffMessage(0, to);
        ChatManager.getInstance().sendMessage(message);
        VecConfig.newVecConfig().setIsOnLine(false);
    }

    // 未接通挂断
    /*public static void endCallResponseFromOff(int callId){
        String to = AgoraMessage.newAgoraMessage().getCurrentChatUsername();
        Message message = responseOffMessage(callId, to);
        ChatManager.getInstance().sendMessage(message);
    }*/

    // 接通，挂断，或者未接通挂断
    @SuppressWarnings("SameParameterValue")
    private static Message requestOnAndOffMessage(int callId, String toUserName){
        Message message = Message.createSendMessage(Message.Type.CMD);
        message.setBody(new EMCmdMessageBody(""));
        message.setTo(toUserName);

        try {
            JSONObject object = new JSONObject();
            JSONObject call = new JSONObject();
            call.put("callId",((callId > 0) ? String.valueOf(callId) : "null"));
            object.put("visitorCancelInvitation",call);

            message.setAttribute("type","agorartcmedia/video");
            message.setAttribute("msgtype",object);
            message.setAttribute("targetSystem", "kefurtc");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return message;
    }

    // 座席端请求视频，未接通，挂断
    /*private static Message responseOffMessage(int callId, String toUserName){
        Message message = Message.createSendMessage(Message.Type.CMD);
        message.setBody(new EMCmdMessageBody(""));
        message.setTo(toUserName);

        try {
            JSONObject object = new JSONObject();
            JSONObject call = new JSONObject();
            call.put("callId",((callId > 0) ? String.valueOf(callId):"null"));
            object.put("visitorRejectInvitation",call);

            message.setAttribute("type","agorartcmedia/video");
            message.setAttribute("msgtype",object);
            message.setAttribute("targetSystem", "kefurtc");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }*/
}
