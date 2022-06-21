package com.easemob.veckit.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Utils {
    private static Gson gson = new Gson();

    public static File getVecPath(Context context, String fileName){
        String path = context.getCacheDir().toString().concat("/vec");
        File search = new File(path);
        if (!search.exists()){
            //noinspection ResultOfMethodCallIgnored
            search.mkdirs();
        }
        return new File(path, fileName);
    }

    public static File saveImage(Context context, String fileName, Bitmap bitmap){
        File vecPath = getVecPath(context, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(vecPath);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } else {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return vecPath;
    }

    public static int getThemePrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static boolean isDarkMode(Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isPhone(Context context) {
        return !isTablet(context);
    }

    public static boolean isTablet(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        return gson.fromJson(json, typeOfT);
    }

    public static int getStateHeight(Context context) {
        int stateHeight = 0;
        Resources resources = context.getApplicationContext().getResources();
        int identifierState = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (identifierState > 0) {
            stateHeight = resources.getDimensionPixelSize(identifierState);
        }
        if (stateHeight == 0){
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, resources.getDisplayMetrics());
        }

        return stateHeight;
    }

    /**
     * 说明: AES256加密
     *
     * @param stringToEncode
     *            明文
     * @param keyString
     *            密钥
     * @return Bses64格式密文
     */
    public static String AES256Encode(String stringToEncode, String keyString)
            throws NullPointerException {
        if (keyString.length() == 0 || keyString == null) {
            return null;
        }
        if (stringToEncode.length() == 0 || stringToEncode == null) {
            return null;
        }
        try {
            SecretKeySpec skeySpec = getKey(keyString);
            byte[] data = stringToEncode.getBytes("UTF8");
            final byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0x00);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
            String encrypedValue = Base64.encodeToString(cipher.doFinal(data),
                    Base64.DEFAULT);
            return encrypedValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     *
     * 说明 :AES256解密
     *
     * @param text
     *            Base64格式密文
     * @param keyString
     *            密钥
     * @return String格式明文
     */
    public static String AES256Decrypt(String text, String keyString) {
        // byte[] rawKey = getRawKey(key);
        if (keyString.length() == 0 || keyString == null) {
            return null;
        }
        if (text.length() == 0 || text == null) {
            return null;
        }
        try {
            SecretKey key = getKey(keyString);
            final byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0x00);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            byte[] data = Base64.decode(text, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            byte[] decrypedValueBytes = (cipher.doFinal(data));
            String decrypedValue = new String(decrypedValueBytes, "UTF-8");
            return decrypedValue;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    /**
     *
     * 说明 :将密钥转行成SecretKeySpec格式
     *
     * @param password
     *            16进制密钥
     * @return SecretKeySpec格式密钥
     */
    private static SecretKeySpec getKey(String password)
            throws UnsupportedEncodingException {
        // 如果为128将长度改为128即可
        int keyLength = 256;
        byte[] keyBytes = new byte[keyLength / 8];
        // explicitly fill with zeros
        Arrays.fill(keyBytes, (byte) 0x0);
        byte[] passwordBytes = toByte(password);
        int length = passwordBytes.length < keyBytes.length ? passwordBytes.length
                : keyBytes.length;
        System.arraycopy(passwordBytes, 0, keyBytes, 0, length);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        return key;
    }

    /**
     * byte数组转换为16进制字符串
     *
     * @param bts
     *            数据源
     * @return 16进制字符串
     */
    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

    /**
     * 将16进制转换为byte数组
     *
     * @param hexString
     *            16进制字符串
     * @return byte数组
     */
    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }
}
