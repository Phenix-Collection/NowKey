package com.kuding.superball.Utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kuding.superball.info.BaseItemInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by user on 17-1-12.
 */

public class JsonParser {

    public static final ArrayList<BaseItemInfo> loadItems(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            Gson gson = new Gson();
            ArrayList<BaseItemInfo> itemInfos =
                    gson.fromJson(br, new TypeToken<ArrayList<BaseItemInfo>>() {}.getType());
            return itemInfos;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final ArrayList<BaseItemInfo> loadItemsFromString(String text) {
        Gson gson = new Gson();
        ArrayList<BaseItemInfo> itemInfos =
                gson.fromJson(text, new TypeToken<ArrayList<BaseItemInfo>>() {}.getType());
        return itemInfos;
    }

    public static final String getJsonStringFromObject(ArrayList<BaseItemInfo> infos) {
        Gson gson = new Gson();
        return gson.toJson(infos);
    }
}
