package com.easemob.veckit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easemob.veckit.bean.EntityBean;
import com.easemob.veckit.bean.VideoStyleBean;
import com.easemob.veckit.floating.FloatWindowManager;
import com.hyphenate.agora.IEndCallback;
import com.hyphenate.chat.AgoraMessage;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.VecConfig;
import com.hyphenate.helpdesk.callback.ValueCallBack;
import com.hyphenate.helpdesk.util.Log;
import com.hyphenate.util.EMLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class CallVideoActivity extends BaseActivity implements View.OnClickListener, IEndCallback {
    private final static String TAG = CallVideoActivity.class.getSimpleName();
    public final static String DIALOG_TYPE_KEY = "dialog_type_key";
    public final static String LOAD_LOCAL_STYLE = "load_local_style";
    // 数据key
    public final static String VIDEO_STYLE_KEY = "video_style_key";
    private final static String JSON_KEY = "json_key_%s";
    private final static int CLOSE_CALL_TIMEOUT = 20 * 60 * 1000;// 未接听，1分钟后超时关闭
    // 无，被动请求
    public final static int DIALOG_TYPE_NO = 0;
    // 发起视频之前
    public final static int DIALOG_TYPE_DEFAULT = 1;
    // 开始发起视频
    public final static int DIALOG_TYPE_SEND = 2;
    // 发起视频，等待人数
    public final static int DIALOG_TYPE_WAIT = 3;
    // 重新发起
    public final static int DIALOG_TYPE_RETRY = 4;
    // 座席端发起视频
    public final static int DIALOG_TYPE_PASSIVE = 5;
    // 当前dialog类型
    private int mCurrentDialogType;

    private TextView mNameTv;
    private TextView mContentTv;
    private ImageView mTypeIv;
    private TextView mTypeTv;
    private View mCloseTv;
    private String mToChatUserName;
    private VideoStyleBean mVideoStyleBean;
    private SharedPreferences mSharedPreferences;
    private static String sToChatUserName;
    private boolean mIsCreate;
    private boolean mIsHavPermission;
    private boolean mClickRequestPermission;
    private View mContent;
    private WindowManager mWm;
    private Point mPoint;
    private int mNavHeight;
    private ImageView mWaitingIV;
    private ImageView mCallingIV;
    private ImageView mQueuingIV;
    private ImageView mEndingIV;
    private ImageView mHangupIv;
    private View mPassiveLlt;
    private ImageView mAcceptIv;

    // 主动
    public static void callingRequest(Context context, String toChatUserName){
        Intent intent = new Intent(context, CallVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(DIALOG_TYPE_KEY, DIALOG_TYPE_DEFAULT);
        // 主动
        intent.putExtra(VideoCallWindowService.INTENT_CALLING_TAG, VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE);
        if (TextUtils.isEmpty(toChatUserName)){
            toChatUserName = AgoraMessage.newAgoraMessage().getCurrentChatUsername();
        }
        intent.putExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME, toChatUserName);
        context.startActivity(intent);
    }

    // 主动
    public static void callingRequest(Context context, String toChatUserName, String jsonStyle){
        sToChatUserName = toChatUserName;
        Intent intent = new Intent(context, CallVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(DIALOG_TYPE_KEY, DIALOG_TYPE_DEFAULT);
        // 主动
        intent.putExtra(VideoCallWindowService.INTENT_CALLING_TAG, VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE);
        intent.putExtra(VIDEO_STYLE_KEY, jsonStyle);
        if (TextUtils.isEmpty(toChatUserName)){
            toChatUserName = AgoraMessage.newAgoraMessage().getCurrentChatUsername();
        }
        intent.putExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME, toChatUserName);
        context.startActivity(intent);
    }

    static void startDialogTypeRetry(Context context, String toChatUserName){
        Intent intent = new Intent(context, CallVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(DIALOG_TYPE_KEY, DIALOG_TYPE_RETRY);
        intent.putExtra(LOAD_LOCAL_STYLE, true);
        // 主动
        intent.putExtra(VideoCallWindowService.INTENT_CALLING_TAG, VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE);
        if (TextUtils.isEmpty(toChatUserName)){
            toChatUserName = AgoraMessage.newAgoraMessage().getCurrentChatUsername();
        }
        intent.putExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME, toChatUserName);
        context.startActivity(intent);
    }


    // 被动
    public static void callingResponse(Context context, Intent intent){
        Intent i = new Intent(context, CallVideoActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(DIALOG_TYPE_KEY, DIALOG_TYPE_NO);

        i.putExtra("type", intent.getStringExtra("type"));
        i.putExtra("appid", intent.getStringExtra("appid"));
        Parcelable zuoXiSendRequestObj = intent.getParcelableExtra("zuoXiSendRequestObj");
        i.putExtra("zuoXiSendRequestObj", zuoXiSendRequestObj);
        i.putExtra("to", intent.getStringExtra("to"));
        i.putExtra("from", intent.getStringExtra("from"));

        // 被动
        i.putExtra(VideoCallWindowService.INTENT_CALLING_TAG, VideoCallWindowService.INTENT_CALLING_TAG_PASSIVE_VALUE);
        i.putExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME, intent.getStringExtra("from"));
        context.startActivity(i);

    }

    // 被动
    public static void callingResponse(Context context, Intent intent, String jsonStyle){
        Intent i = new Intent(context, CallVideoActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(DIALOG_TYPE_KEY, DIALOG_TYPE_NO);
        intent.putExtra(VIDEO_STYLE_KEY, jsonStyle);

        i.putExtra("type", intent.getStringExtra("type"));
        i.putExtra("appid", intent.getStringExtra("appid"));
        Parcelable zuoXiSendRequestObj = intent.getParcelableExtra("zuoXiSendRequestObj");
        i.putExtra("zuoXiSendRequestObj", zuoXiSendRequestObj);
        i.putExtra("to", intent.getStringExtra("to"));
        i.putExtra("from", intent.getStringExtra("from"));

        // 被动
        i.putExtra(VideoCallWindowService.INTENT_CALLING_TAG, VideoCallWindowService.INTENT_CALLING_TAG_PASSIVE_VALUE);
        i.putExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME, intent.getStringExtra("from"));
        context.startActivity(i);

    }

    private final ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            mNavHeight = getNav(mWm, mContent, mPoint);
            mContent.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    };

    @Override
    public int getLayoutResId() {
        return R.layout.activity_vec_call_video;
    }

    @Override
    public void handleMessage(Message msg) {
        dialogType(msg.what);
    }

    @Override
    public boolean isLoadLayoutRes(Intent intent) {
        // 判断是否为主动
        int isActive = intent.getIntExtra(VideoCallWindowService.INTENT_CALLING_TAG,
                VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE);
        return isActive == VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE;
    }

    @Override
    public void initView(@NonNull Intent intent, @Nullable Bundle savedInstanceState) {
        AgoraMessage.newAgoraMessage().registerIEndCallback(getClass().getSimpleName(), this);
        mSharedPreferences = getSharedPreferences("video_style", MODE_PRIVATE);
        // 判断是否为主动
        int isActive = intent.getIntExtra(VideoCallWindowService.INTENT_CALLING_TAG,
                VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE);
        try {
            initPassiveView();
            initStyle(intent, isActive == VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isActive == VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE){
            checkPermission();
            mToChatUserName = intent.getStringExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME);
            initView();
            int dialogType = intent.getIntExtra(DIALOG_TYPE_KEY, DIALOG_TYPE_DEFAULT);
            if (dialogType == DIALOG_TYPE_DEFAULT){
                dialogType(mVideoStyleBean.getFunctionSettings().isSkipWaitingPage() ? DIALOG_TYPE_SEND : DIALOG_TYPE_DEFAULT);
                if (mCurrentDialogType == DIALOG_TYPE_SEND){
                    activeVideo(mIsHavPermission);
                }
            }else if (dialogType == DIALOG_TYPE_SEND){
                dialogType(DIALOG_TYPE_SEND);
            }else if (dialogType == DIALOG_TYPE_RETRY){
                dialogType(DIALOG_TYPE_RETRY);
            }
        }else {
            // 被动 坐席 --> 访客端
            // 检测是否有悬浮权限
            passVideo(FloatWindowManager.getInstance().checkPermission(this), intent);
        }
    }

    // 坐席主动发视频邀请
    private void initPassiveView() {
        mPassiveLlt = $(R.id.passiveLlt);
        mHangupIv = $(R.id.hangupIv);
        mAcceptIv = $(R.id.acceptIv);
        mHangupIv.setOnClickListener(this);
        mAcceptIv.setOnClickListener(this);
    }

    private void request(Callback callback) {
        String tenantId = ChatClient.getInstance().tenantId();// "77556"
        String configId = ChatClient.getInstance().getConfigId();
        if (TextUtils.isEmpty(configId)){
            return;
        }
        AgoraMessage.asyncInitStyle(tenantId, configId, new ValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                try {
                    Log.e("aaaaaaaaaa","style = "+value);
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
                    if (!getLocalData().equals(json)){
                        // 改变数据
                        runOnUiThread(() -> {
                            try {
                                // 保存本地
                                saveLocalData(json);
                                callback.run(json);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                Log.e("aaaaaaaaaaa","error = "+error+"， errorMsg = "+errorMsg);
            }
        });
    }

    private void saveLocalData(/*JSONObject entity*/String entity){
        // 保存本地
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(String.format(JSON_KEY, sToChatUserName), entity);
        edit.apply();
    }

    private String getLocalData(){
        return mSharedPreferences.getString(String.format(JSON_KEY, sToChatUserName),"");
    }

    private int getNav(WindowManager wm, View content, Point point){
        Display display = wm.getDefaultDisplay();
        display.getRealSize(point);
        if (content.getBottom() == 0){
            return 0;
        }
        return point.y - content.getBottom();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        EMLog.e(TAG, "onNewIntent");
        Log.e("aaaaaaaaaaaaaaa","onNewIntent");
        int isActive = intent.getIntExtra(VideoCallWindowService.INTENT_CALLING_TAG,
                VideoCallWindowService.INTENT_CALLING_TAG_ACTIVE_VALUE);

        if (mCurrentDialogType == DIALOG_TYPE_SEND || mCurrentDialogType == DIALOG_TYPE_WAIT){
            if (isActive == VideoCallWindowService.INTENT_CALLING_TAG_PASSIVE_VALUE){
                String toChatUserName = intent.getStringExtra(VideoCallWindowService.CURRENT_CHAT_USER_NAME);
                if (!TextUtils.isEmpty(toChatUserName)
                        && !TextUtils.isEmpty(mToChatUserName)
                        && mToChatUserName.equals(toChatUserName)){
                    if (!mIsCreate){
                        mIsCreate = true;
                        activeVideoResponse(mIsHavPermission, intent);
                    }
                }
            }
        }else {
            // 被动 正在显示默认或重新发送页面，坐席端发送过来请求
            if (isActive == VideoCallWindowService.INTENT_CALLING_TAG_PASSIVE_VALUE){
                if (!mIsCreate){
                    mIsCreate = true;
                    passVideo(FloatWindowManager.getInstance().checkPermission(this), intent);
                }
            }
        }
    }

    // 被动发起视频
    private void passVideo(boolean checkPermission, Intent intent) {
        EMLog.e(TAG, "被动 正在通话 座席端 -- 访客端");

        if (checkPermission){
            intent.putExtra("nav_height",mNavHeight);
            VideoCallWindowService.show(this, intent);
        }else {
            CallActivity.show(this, intent);
        }

        finishPage();
    }

    private void activeVideo(boolean checkPermission) {
        EMLog.e(TAG, "主动发起请求 是否有悬浮权限 = "+checkPermission);
        VecConfig.newVecConfig().setVecVideo(true);
        sendCmd();
        startTimerOut();
    }

    private void activeVideoResponse(boolean isHavPermission, Intent intent) {
        EMLog.e(TAG, "主动发起请求 获取到座席端响应");
        stopTimerOut();
        if (isHavPermission){
            intent.putExtra("nav_height",mNavHeight);
            VideoCallWindowService.show(this, mToChatUserName, intent);
        }else {
            CallActivity.show(this, mToChatUserName, intent);
        }

        finishPage();
    }

    // 发送请求建立视频
    private void sendCmd() {
        ChatClient.getInstance().callManager().callVecVideo("邀请客服进行实时视频", mToChatUserName);
    }

    private Runnable mCloseTimerOut;
    private void startTimerOut() {
        if (mCloseTimerOut == null){
            mCloseTimerOut = () -> {
                Log.e("ooooooooooo","挂断");
                // 超时拒接
                EMLog.e(TAG, "主动发起请求，坐席端超时，挂断");
                mIsCreate = false;
                // ChatClient.getInstance().callManager().endCall(0, true);
                VECKitCalling.endCallFromOff();
                VecConfig.newVecConfig().setVecVideo(false);
                // 回复状态
                dialogType(DIALOG_TYPE_DEFAULT);
            };
        }
        removeRunnable(mCloseTimerOut);
        postDelayed(mCloseTimerOut, CLOSE_CALL_TIMEOUT);
    }

    private void stopTimerOut(){
        if (mCloseTimerOut != null){
            removeRunnable(mCloseTimerOut);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.typeIv){
            // 发起视频之前
            if (DIALOG_TYPE_DEFAULT == mCurrentDialogType){
                dialogType(DIALOG_TYPE_SEND);
                activeVideo(mIsHavPermission);
            }else if (DIALOG_TYPE_RETRY == mCurrentDialogType){
                dialogType(DIALOG_TYPE_SEND);
                activeVideo(mIsHavPermission);
            }else if (DIALOG_TYPE_WAIT == mCurrentDialogType){
                Log.e("ooooooooo","wait");
            }else if (DIALOG_TYPE_SEND == mCurrentDialogType){
                // 挂断
                // ChatClient.getInstance().callManager().endVecCall(0, true);
                VECKitCalling.endCallFromOff();
                mIsCreate = false;
                finishPage();
            }
        }else if (id == R.id.closeTv){
            // 关闭
            clear();
            finish();
        }else if (id == R.id.hangupIv){
            // 坐席主动发视频邀请，拒接按钮

        }else if (id == R.id.acceptIv){
            // 坐席主动发视频邀请，接听按钮

        }

    }

    private void checkPermission() {
        if (!FloatWindowManager.getInstance().checkPermission(this)){
            mIsHavPermission = false;
            FloatWindowManager.getInstance().applyPermission(this, confirm -> mClickRequestPermission = confirm);
        }else {
            mIsHavPermission = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mClickRequestPermission){
            mIsHavPermission = FloatWindowManager.getInstance().checkPermission(this);
        }
    }

    private void dialogType(int type) {
        mCurrentDialogType = type;
        if (type == DIALOG_TYPE_DEFAULT){
            // 发起视频之前
            showBackground(mWaitingIV);
            dialogTypeDefault();
        }else if (type == DIALOG_TYPE_SEND){
            // 开始发起视频
            showBackground(mCallingIV);
            dialogTypeSend();
        }else if (type == DIALOG_TYPE_WAIT){
            // 发起视频，等待人数
            showBackground(mQueuingIV);
            dialogTypeWait();
        }else if (type == DIALOG_TYPE_RETRY){
            // 重新发起
            showBackground(mEndingIV);
            dialogTypeRetry();
        }else if (type == DIALOG_TYPE_PASSIVE){
            // 座席端发起视频
            dialogTypePassive();
        }
    }

    private void showBackground(ImageView imageView){
        showAndHidden(mWaitingIV, mWaitingIV == imageView);
        showAndHidden(mCallingIV, mCallingIV == imageView);
        showAndHidden(mEndingIV, mEndingIV == imageView);
        showAndHidden(mQueuingIV, mQueuingIV == imageView);
    }

    private void netWork(){
        request(json -> {
            EntityBean entityBean = new EntityBean(json);
            mVideoStyleBean = entityBean.getVideoStyleBean();
            changeBackgroundImage();
            Log.e("aaaaaaaaaa","请求网络成功 = "+json);
        });
    }

    private void changeBackgroundImage() {
        String path = getCacheDir().toString();
        VecConfig.newVecConfig().setCameraState(mVideoStyleBean.getFunctionSettings().isVisitorCameraOff());
        loadImage(mWaitingIV, path, mVideoStyleBean.getStyleSettings().getWaitingBackgroundPic());
        loadImage(mCallingIV, path, mVideoStyleBean.getStyleSettings().getCallingBackgroundPic());
        loadImage(mQueuingIV, path, mVideoStyleBean.getStyleSettings().getQueuingBackgroundPic());
        loadImage(mEndingIV, path, mVideoStyleBean.getStyleSettings().getEndingBackgroundPic());
    }

    private void loadImage(ImageView imageView, String saveLocalPath, String url){
        if (imageView == null){
            return;
        }

        if (!TextUtils.isEmpty(url)){
            saveLocalPath = saveLocalPath.concat("/vec");
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            File file = new File(saveLocalPath, fileName);
            if (file.exists()){
                // 加载本地图片
                if (isFinishing()){
                    return;
                }
                /*Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
                bitmap.recycle();*/
                imageView.setImageURI(Uri.fromFile(file));
            }else {
                File search = new File(saveLocalPath);
                if (!search.exists()){
                    //noinspection ResultOfMethodCallIgnored
                    search.mkdirs();
                }

                if (isFinishing()){
                    return;
                }
                // 请求网络，保存本地图片
                NetWork.loadImage(file.getPath(), url, new NetWork.CallBack() {
                    @Override
                    public void ok(String url) {
                        if (isFinishing()){
                            return;
                        }
                        runOnUiThread(() -> imageView.setImageURI(Uri.fromFile(new File(url))));
                    }

                    @Override
                    public void fail(int code, String error) {
                    }
                });
            }
        }else {
            imageView.setImageResource(R.drawable.dialog_corners_bg);
        }
    }

    private void initStyle(@NonNull Intent intent, boolean isActive) throws JSONException {
        if (isActive){
            mWaitingIV = $(R.id.waitingIV);
            clipToOutline(mWaitingIV);
            mCallingIV = $(R.id.callingIV);
            clipToOutline(mCallingIV);
            mQueuingIV = $(R.id.queuingIV);
            clipToOutline(mQueuingIV);
            mEndingIV = $(R.id.endingIV);
            clipToOutline(mEndingIV);
        }

        boolean loadLocalStyle = intent.getBooleanExtra(LOAD_LOCAL_STYLE, false);

        // 本地取值
        String localData = getLocalData();
        if (!TextUtils.isEmpty(localData)){
            EntityBean entityBean = new EntityBean(localData);
            mVideoStyleBean = entityBean.getVideoStyleBean();
            changeBackgroundImage();
        }else {
            mVideoStyleBean = VideoStyleBean.create();
        }
        String jsonStyle = intent.getStringExtra(VIDEO_STYLE_KEY);
        Log.e("aaaaaaaaaaaa","传递值 = "+jsonStyle);
        if (!TextUtils.isEmpty(jsonStyle)){
            initStyleFromIntent(jsonStyle);
        }else {
            if (!loadLocalStyle){
                netWork();
            }else {
                Log.e("aaaaaaaaaa","加载本地样式");
            }
        }
    }

    private void clipToOutline(View surfaceView){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            surfaceView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);
                    int leftMargin = 0;
                    int topMargin = 0;
                    Rect selfRect = new Rect(leftMargin, topMargin,
                            rect.right - rect.left - leftMargin,
                            rect.bottom - rect.top - topMargin);
                    outline.setRoundRect(selfRect, dp2px(16));
                }
            });
            surfaceView.setClipToOutline(true);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void initStyleFromIntent(String jsonStyle) {
        try {
            // 整体数据
            if (!getLocalData().equals(jsonStyle)){
                Log.e("aaaaaaaaaaaa","数据不等 = "+jsonStyle);
                // 改变数据
                saveLocalData(jsonStyle);
                EntityBean entityBean = new EntityBean(jsonStyle);
                mVideoStyleBean = entityBean.getVideoStyleBean();
                changeBackgroundImage();
            }else {
                Log.e("aaaaaaaaaaaa","数据相等");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clip($(R.id.photoIv), 50);
        }
        mCloseTv = $(R.id.closeTv);
        mNameTv = $(R.id.nameTv);
        mContentTv = $(R.id.contentTv);
        mTypeIv = $(R.id.typeIv);
        mTypeTv = $(R.id.typeTv);
        mCloseTv.setOnClickListener(this);
        mTypeIv.setOnClickListener(this);

        mContent = getWindow().getDecorView().findViewById(android.R.id.content);
        mWm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        mPoint = new Point();
        mContent.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    private void dialogTypePassive(){
        showAndHidden(mCloseTv, false);
        showAndHidden(mTypeIv, false);
        showAndHidden(mPassiveLlt, true);

        mNameTv.setText(TextUtils.isEmpty(VecConfig.newVecConfig().getUserName()) ? "环信" : VecConfig.newVecConfig().getUserName());
        mContentTv.setText(mVideoStyleBean.getStyleSettings().getCallingPrompt());
        mTypeTv.setText("挂断");
    }

    private void dialogTypeRetry() {
        showAndHidden(mPassiveLlt, false);
        showAndHidden(mTypeIv, true);
        showAndHidden(mCloseTv, true);
        // mNameTv.setText("环信");
        mNameTv.setText(TextUtils.isEmpty(VecConfig.newVecConfig().getUserName()) ? "环信" : VecConfig.newVecConfig().getUserName());
        mContentTv.setText(mVideoStyleBean.getStyleSettings().getEndingPrompt());
        mTypeIv.setImageResource(R.drawable.em_icon_call_accept);
        mTypeTv.setText("重新发起");
    }

    private void dialogTypeWait() {
        showAndHidden(mPassiveLlt, false);
        showAndHidden(mTypeIv, true);
        showAndHidden(mCloseTv, false);
        // mNameTv.setText("客服花花");
        mNameTv.setText(TextUtils.isEmpty(VecConfig.newVecConfig().getUserName()) ? "客服花花" : VecConfig.newVecConfig().getUserName());
        mContentTv.setText(mVideoStyleBean.getStyleSettings().getQueuingPrompt());
        mTypeIv.setImageResource(R.drawable.em_icon_call_hangup);
        mTypeTv.setText("挂断");
    }

    private void dialogTypeSend() {
        showAndHidden(mPassiveLlt, false);
        showAndHidden(mTypeIv, true);
        showAndHidden(mCloseTv, false);
        // mNameTv.setText("环信");
        mNameTv.setText(TextUtils.isEmpty(VecConfig.newVecConfig().getUserName()) ? "环信" : VecConfig.newVecConfig().getUserName());
        mContentTv.setText(mVideoStyleBean.getStyleSettings().getCallingPrompt());
        mTypeIv.setImageResource(R.drawable.em_icon_call_hangup);
        mTypeTv.setText("挂断");
    }

    private void dialogTypeDefault() {
        showAndHidden(mPassiveLlt, false);
        showAndHidden(mTypeIv, true);
        showAndHidden(mCloseTv, true);
        // mNameTv.setText("环信");
        mNameTv.setText(TextUtils.isEmpty(VecConfig.newVecConfig().getUserName()) ? "环信" : VecConfig.newVecConfig().getUserName());
        mContentTv.setText(mVideoStyleBean.getStyleSettings().getWaitingPrompt());
        mTypeIv.setImageResource(R.drawable.em_icon_call_accept);
        mTypeTv.setText("发起");
    }

    @Override
    public void onBackPressed() {
        if (mCurrentDialogType == DIALOG_TYPE_SEND
                || mCurrentDialogType == DIALOG_TYPE_WAIT){
            return;
        }
        super.onBackPressed();
        clear();
    }

    private void finishPage(){
        postDelayed(() -> {
            clear();
            finish();
        }, 100);
    }



    private void clear(){
        stopTimerOut();
        AgoraMessage.newAgoraMessage().unRegisterIEndCallback(getClass().getSimpleName());
        mToChatUserName = null;
        mIsCreate = false;
        mClickRequestPermission = false;
    }

    @Override
    public void onVecZuoXiToBreakOff() {
        finishPage();
    }

    interface Callback{
        void run(String json) throws JSONException;
    }
}
