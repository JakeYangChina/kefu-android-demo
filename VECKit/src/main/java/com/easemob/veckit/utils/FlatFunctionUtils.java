package com.easemob.veckit.utils;

import com.hyphenate.agora.FunctionIconItem;

import java.util.ArrayList;
import java.util.List;

public class FlatFunctionUtils {
    private static FlatFunctionUtils sFlatFunctionUtils;
    private List<FunctionIconItem> mIconItems = new ArrayList<>();
    private FlatFunctionUtils(){

    }

    public static FlatFunctionUtils get(){
        if (sFlatFunctionUtils == null){
            sFlatFunctionUtils = new FlatFunctionUtils();
        }
        return sFlatFunctionUtils;
    }

    public void setIconItems(List<FunctionIconItem> iconItems){
        if (mIconItems != null){
            mIconItems.clear();
            mIconItems.addAll(iconItems);
        }
    }

    public List<FunctionIconItem> getIconItems() {
        return mIconItems;
    }

    public void clear(){
        mIconItems.clear();
    }
}
