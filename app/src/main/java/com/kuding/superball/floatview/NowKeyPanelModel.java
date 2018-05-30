package com.kuding.superball.floatview;

import android.content.Context;
import android.util.Log;

import com.kuding.superball.NowKeyApplication;
import com.kuding.superball.Utils.BaseModelComparator;
import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.JsonParser;
import com.kuding.superball.Utils.NowKeyParser;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.Utils.Utils;
import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by user on 17-1-12.
 */

public class NowKeyPanelModel {
    private static final String TAG = "NowKeyPanelModel";

    private static NowKeyPanelModel sInstance;

    private ArrayList<NowKeyModelCallback> mCallbacks;

    private Context mContext;

    private ArrayList<BaseItemInfo> mDataPage1;         // 普通模式第一页的数据
    private ArrayList<BaseItemInfo> mDataPage2;         // 普通模式第二页的数据
    private ArrayList<BaseItemInfo> mDataMini;          // 悬浮球 迷你模式 的数据
    private ArrayList<String> mExtraDatas;


    private NowKeyPanelModel() {
        mContext = NowKeyApplication.getInstance().getApplicationContext();
    }

    public static final NowKeyPanelModel getInstance() {
        if (sInstance == null) {
            sInstance = new NowKeyPanelModel();
        }
        return sInstance;
    }

    public interface NowKeyModelCallback {
        void onNowKeyItemDelete(BaseItemInfo item, int page);

        void onNowKeyItemAdd(BaseItemInfo item, int page, boolean external);

        void onNowKeyItemReplace(BaseItemInfo item, int page, boolean external);

        void onNowKeyItemUpdate(BaseItemInfo item, int page);

        void onNowKeyItemUpdatePosition(ArrayList<BaseItemInfo> items, int page);
    }

    public void setNowkeyModelCallback(NowKeyModelCallback callback) {
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<>();
        }
        if (!mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    public void removeNowKeyModelCallback(NowKeyModelCallback callback) {
        if (mCallbacks == null) return;
        if (mCallbacks.contains(callback)) {
            mCallbacks.remove(callback);
        }
    }

    public void initExtraData() {
        if (mExtraDatas == null) {
            mExtraDatas = new ArrayList<>();
        }
        mExtraDatas.clear();
        for (int i = 0; i < Constant.DEFAULT_EXTRA_LIST.length; i++) {
            mExtraDatas.add(Constant.DEFAULT_EXTRA_LIST[i]);
        }
    }

    /**
     * 获得 额外补充 的数据
     *
     * @return
     */
    private ArrayList<String> getExtraData() {
        ArrayList<String> extraDatas = new ArrayList<String>();
        for (int i = 0; i < Constant.DEFAULT_EXTRA_LIST.length; i++) {
            extraDatas.add(Constant.DEFAULT_EXTRA_LIST[i]);
        }
        return extraDatas;
    }

    public ArrayList<BaseItemInfo> loadData(int page) {
        ArrayList<Integer> emptyIndexes = null;
        if (page == 1) {
            if (mDataPage1 == null) mDataPage1 = new ArrayList<>();
            mDataPage1.clear();
            String dataPage1 = PreferenceUtils.getNormalData1("");
            if ("".equals(dataPage1)) {
                try {
                    mDataPage1 = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_page1);
                    //JsonParser.loadItems(mContext, "nowkeyitem/now_key_item_page1.json");
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Collections.sort(mDataPage1, new BaseModelComparator());
                fillEmpty(mDataPage1, mExtraDatas);
                //PreferenceUtils.setNormalMenuItemCount1(mContext, mDataPage1.size());
                PreferenceUtils.setNormalData1(JsonParser.getJsonStringFromObject(mDataPage1));
            } else {
                mDataPage1 = JsonParser.loadItemsFromString(dataPage1);
                emptyIndexes = Utils.filterBaseItemInfos(mContext, mDataPage1);
                // 如果 emptyIndexes 有数据，说明解析出来的 mDataPage1 中，存在非法数据，需要进行填充处理
                if (emptyIndexes != null && !emptyIndexes.isEmpty()) {
                    fillInvalidItem(emptyIndexes, mDataPage1, 1);
                    PreferenceUtils.setNormalMenuItemCount1(mDataPage1.size());
                    PreferenceUtils.setNormalData1(JsonParser.getJsonStringFromObject(mDataPage1));
                }
            }
        } else if (page == 2) {
            if (mDataPage2 == null) mDataPage2 = new ArrayList<>();
            mDataPage2.clear();
            String dataPage2 = PreferenceUtils.getNormalData2("");

            if ("".equals(dataPage2)) {
                try {
                    mDataPage2 = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_page2);
                    //JsonParser.loadItems(mContext, "nowkeyitem/now_key_item_page2.json");
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Collections.sort(mDataPage2, new BaseModelComparator());
                fillEmpty(mDataPage2, mExtraDatas);
                PreferenceUtils.setNormalMenuItemCount2(mDataPage2.size());
                PreferenceUtils.setNormalData2(JsonParser.getJsonStringFromObject(mDataPage2));
            } else {
                mDataPage2 = JsonParser.loadItemsFromString(dataPage2);
                emptyIndexes = Utils.filterBaseItemInfos(mContext, mDataPage2);
                // 如果 emptyIndexes 有数据，说明解析出来的 mDataPage2 中，存在非法数据，需要进行填充处理
                if (emptyIndexes != null && !emptyIndexes.isEmpty()) {
                    fillInvalidItem(emptyIndexes, mDataPage2, 1);
                    PreferenceUtils.setNormalMenuItemCount2(mDataPage2.size());
                    PreferenceUtils.setNormalData2(JsonParser.getJsonStringFromObject(mDataPage2));
                }
            }
        }
        if (page == 1) return mDataPage1;
        if (page == 2) return mDataPage2;
        return null;
    }

    /**
     * 重置所有数据
     *
     * @return
     */
    public void resetAlldata() {
        resetNormalData();
        resetMinidata();
    }

    /**
     * 重置 普通模式的 数据
     */
    public void resetNormalData() {
        // 重置普通模式 第一页数据
        if (mDataPage1 == null) mDataPage1 = new ArrayList<>();
        mDataPage1.clear();

        try {
            mDataPage1 = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_page1);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(mDataPage1, new BaseModelComparator());
        fillEmpty(mDataPage1, mExtraDatas);
        PreferenceUtils.setNormalData1(JsonParser.getJsonStringFromObject(mDataPage1));
        PreferenceUtils.setNormalMenuItemCount1(mDataPage1.size());


        // 重置普通模式第二页数据
        if (mDataPage2 == null) mDataPage2 = new ArrayList<>();
        mDataPage2.clear();

        try {
            mDataPage2 = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_page2);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(mDataPage2, new BaseModelComparator());
        fillEmpty(mDataPage2, mExtraDatas);
        PreferenceUtils.setNormalMenuItemCount2(mDataPage2.size());
        PreferenceUtils.setNormalData2(JsonParser.getJsonStringFromObject(mDataPage2));
    }

    /**
     * 重置 迷你模式的 数据
     *
     * @return
     */
    public void resetMinidata() {
        // 重置迷你模式的数据
        if (mDataMini == null) mDataMini = new ArrayList<>();
        mDataMini.clear();
        try {
            mDataMini = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_mini_data);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(mDataMini, new BaseModelComparator());
        fillEmpty(mDataMini, mExtraDatas);
        PreferenceUtils.setMiniDataWithNumber(JsonParser.getJsonStringFromObject(mDataMini), 8);
    }

    public ArrayList<BaseItemInfo> loadXMLData(int page) {
        ArrayList<BaseItemInfo> datas = null;
        if (page == 1) {
            try {
                datas = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_page1);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Collections.sort(datas, new BaseModelComparator());
            fillEmpty(datas, mExtraDatas);
        } else if (page == 2) {
            try {
                datas = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_page2);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Collections.sort(datas, new BaseModelComparator());
            fillEmpty(datas, mExtraDatas);
        }
        return datas;
    }

    /**
     * 获取迷你模式的 xml 原始数据
     *
     * @return
     */
    public ArrayList<BaseItemInfo> loadMiniXMLData() {
        if (mDataMini == null) mDataMini = new ArrayList<>();
        mDataMini.clear();
        try {
            mDataMini = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_mini_data);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(mDataMini, new BaseModelComparator());
        fillEmpty(mDataMini, mExtraDatas);
        return mDataMini;
    }


    /**
     * 加载 迷你模式 悬浮球 的数据
     *
     * @return
     */
    public ArrayList<BaseItemInfo> loadMiniData() {
        ArrayList<Integer> emptyIndexes = null;
        if (mDataMini == null) mDataMini = new ArrayList<>();
        mDataMini.clear();
        String data = PreferenceUtils.getMiniData("");
        Log.d(TAG, "showinfos data:" + data);
        if ("".equals(data)) {
            try {
                mDataMini = NowKeyParser.parseDefaultPage(mContext, R.xml.defalut_nowkey_mini_data);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Collections.sort(mDataMini, new BaseModelComparator());
            fillEmpty(mDataMini, mExtraDatas);
            //PreferenceUtils.setMiniMenuItemCount(mContext, mDataMini.size());
            PreferenceUtils.setMiniDataWithNumber(JsonParser.getJsonStringFromObject(mDataMini), 8);
        } else {
            mDataMini = JsonParser.loadItemsFromString(data);
            emptyIndexes = Utils.filterBaseItemInfos(mContext, mDataMini);
            // 如果 emptyIndexes 有数据，说明解析出来的 mDataMini 中，存在非法数据，需要进行填充处理
            if (emptyIndexes != null && !emptyIndexes.isEmpty()) {
                fillInvalidItem(emptyIndexes, mDataMini, 0);
                PreferenceUtils.setMiniDataWithNumber(JsonParser.getJsonStringFromObject(mDataMini), mDataMini.size());
            }
        }
        return mDataMini;
    }

    /**
     * 对已经无效的item进行填充
     *
     * @param invalidIndexs
     * @param items
     * @param mode          当前的模式 0:Mini模式   1:Normal模式
     */
    private void fillInvalidItem(ArrayList<Integer> invalidIndexs, ArrayList<BaseItemInfo> items, int mode) {

        ArrayList<String> itemKeywords = new ArrayList<String>();
        ArrayList<String> extraDatas = getExtraData();

        if (items == null) return;
        if (invalidIndexs.size() <= 0) return;
        if (extraDatas == null || extraDatas.size() == 0) return;

        // 获得 目前菜单的keywords，用于从extra中筛选出未重复的条目
        for (BaseItemInfo item : items) {
            itemKeywords.add(item.getKey_word());
        }

        // 从 extraDatas 移除掉 目前菜单中已经存在的item 避免重复添加
        for (String itemKeyword : itemKeywords) {
            if (extraDatas.contains(itemKeyword)) {
                extraDatas.remove(itemKeyword);
            }
        }

        // 从 extraDatas 取出可以选用的条目，填补无效的item
        BaseItemInfo newItem = null;
        int currentExtraIndex = 0;
        for (int invalidIndex : invalidIndexs) {
            if (items.size() - 1 < invalidIndex) {
                continue;
            }

            newItem = new BaseItemInfo();
            newItem.setKey_word(extraDatas.get(currentExtraIndex));
            newItem.setIndex(invalidIndex);
            newItem.setType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
            items.add(newItem);

            currentExtraIndex++;
        }

        Collections.sort(items, new BaseModelComparator());
    }

    private void fillEmpty(ArrayList<BaseItemInfo> infos, ArrayList<String> extras) {
        if (extras == null) return;
        if (infos.size() >= 8) return;
        boolean hasAdded;
        ArrayList<BaseItemInfo> extraInfos = new ArrayList<>();
        ArrayList<Integer> emptyIndexes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            hasAdded = false;
            for (BaseItemInfo info : infos) {
                if (info.getIndex() == i) {
                    hasAdded = true;
                    break;
                }
            }
            if (!hasAdded) {
                emptyIndexes.add(i);
            }
        }

        for (int index : emptyIndexes) {
            extraInfos.clear();
            for (String addKey : extras) {
                BaseItemInfo itemInfo = new BaseItemInfo();
                itemInfo.setKey_word(addKey);
                itemInfo.setIndex(index);
                itemInfo.setType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                extraInfos.add(itemInfo);
            }
            for (BaseItemInfo extra : extraInfos) {
                if (infos.size() >= 8) {
                    Collections.sort(infos, new BaseModelComparator());
                    return;
                }
                if (!infos.contains(extra)) {
                    infos.add(extra);
                    extras.remove(extra.getKey_word());
                    break;
                }
            }
        }
    }


    public void addItem(BaseItemInfo added, int page) {
        if (added == null) {
            for (NowKeyModelCallback callback : mCallbacks) {
                if (callback != null) {
                    callback.onNowKeyItemAdd(added, page, false);
                }
            }
            return;
        }
        String items = null;
        if (page == 1) {
            items = PreferenceUtils.getNormalData1("");
        } else if (page == 2) {
            items = PreferenceUtils.getNormalData2("");
        }
        if (items != null && !"".equals(items)) {
            ArrayList<BaseItemInfo> baseInfos = JsonParser.loadItemsFromString(items);

            baseInfos.add(added);
            Collections.sort(baseInfos, new BaseModelComparator());
            String save = JsonParser.getJsonStringFromObject(baseInfos);
            if (page == 1) {
                //PreferenceUtils.setNormalMenuItemCount1(baseInfos.size());
                PreferenceUtils.setNormalData1(save);
            } else if (page == 2) {
                //PreferenceUtils.setNormalMenuItemCount2(baseInfos.size());
                PreferenceUtils.setNormalData2(save);
            }
        }
        for (NowKeyModelCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onNowKeyItemAdd(added, page, false);
            }
        }
    }

    /**
     * 替换 子菜单中 的某一个菜单
     */
    public void replaceItem(BaseItemInfo added, int page, boolean external) {
        if (added == null) {
            for (NowKeyModelCallback callback : mCallbacks) {
                if (callback != null) {
                    callback.onNowKeyItemReplace(added, page, false);
                }
            }
            return;
        }
        backupData(added, page);
        String items = null;
        if (page == 0) {
            items = PreferenceUtils.getMiniData("");
        } else if (page == 1) {
            items = PreferenceUtils.getNormalData1("");
        } else if (page == 2) {
            items = PreferenceUtils.getNormalData2("");
        }
        if (items != null && !"".equals(items)) {
            ArrayList<BaseItemInfo> baseInfos = JsonParser.loadItemsFromString(items);
            // 根据索引删除旧的，添加新的菜单项
            if (added.getIndex() < baseInfos.size()) {
                baseInfos.remove(added.getIndex());
            } else {
                return;
            }
            baseInfos.add(added);

            Collections.sort(baseInfos, new BaseModelComparator());
            String save = JsonParser.getJsonStringFromObject(baseInfos);
            if (page == 0) {
                PreferenceUtils.setMiniDataWithNumber(save, baseInfos.size());
            } else if (page == 1) {
                PreferenceUtils.setNormalData1(save);
            } else if (page == 2) {
                PreferenceUtils.setNormalData2(save);
            }
        }
        for (NowKeyModelCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onNowKeyItemReplace(added, page, false);
            }
        }
    }

    /**
     * 备份数据 由于 Defect：4881157 所以增加的方法
     *
     * @param added
     * @param page
     */
    private void backupData(BaseItemInfo added, int page) {
        String items = null;
        if (page == 0) {
            items = PreferenceUtils.getMiniDataBackup("");
        } else if (page == 1) {
            items = PreferenceUtils.getNormalData1Backup("");
        } else if (page == 2) {
            items = PreferenceUtils.getNormalData2Backup("");
        }
        if (items != null && !"".equals(items)) {
            ArrayList<BaseItemInfo> baseInfos = JsonParser.loadItemsFromString(items);
            // 根据索引删除旧的，添加新的菜单项
            if (added.getIndex() < baseInfos.size()) {
                baseInfos.remove(added.getIndex());
            } else {
                return;
            }
            baseInfos.add(added);
            Collections.sort(baseInfos, new BaseModelComparator());
            String save = JsonParser.getJsonStringFromObject(baseInfos);
            if (page == 0) {
                PreferenceUtils.setMiniDataBackup(save);
            } else if (page == 1) {
                PreferenceUtils.setNormalData1Backup(save);
            } else if (page == 2) {
                PreferenceUtils.setNormalData2Backup(save);
            }
        }
    }

    public void addItemExternal(BaseItemInfo added, int page) {
        if (added == null) {
            for (NowKeyModelCallback callback : mCallbacks) {
                if (callback != null) {
                    callback.onNowKeyItemAdd(added, page, true);
                }
            }
            return;
        }
        String items = null;
        if (page == 1) {
            items = PreferenceUtils.getNormalData1("");
        } else if (page == 2) {
            items = PreferenceUtils.getNormalData2("");
        }
        if (items != null && !"".equals(items)) {
            ArrayList<BaseItemInfo> baseInfos = JsonParser.loadItemsFromString(items);
            baseInfos.add(added);
            Collections.sort(baseInfos, new BaseModelComparator());
            String save = JsonParser.getJsonStringFromObject(baseInfos);
            if (page == 1) {
                //PreferenceUtils.setNormalMenuItemCount1(baseInfos.size());
                PreferenceUtils.setNormalData1(save);
            } else if (page == 2) {
                PreferenceUtils.setNormalMenuItemCount2(baseInfos.size());
                PreferenceUtils.setNormalData2(save);
            }
        }
        for (NowKeyModelCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onNowKeyItemAdd(added, page, true);
            }
        }
    }

    public void updateItem(BaseItemInfo update, int page) {
        String items = null;
        if (page == 1) {
            items = PreferenceUtils.getNormalData1("");
        } else if (page == 2) {
            items = PreferenceUtils.getNormalData2("");
        }
        if (items != null && !"".equals(items)) {
            ArrayList<BaseItemInfo> baseInfos = JsonParser.loadItemsFromString(items);
            if (baseInfos.contains(update)) {
                if (update.getKey_word().equals("callacontact")) {

                }
            }

        }
        for (NowKeyModelCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onNowKeyItemUpdate(update, page);
            }
        }
    }

    public void updateItemPosition(ArrayList<BaseItemInfo> update, int page) {
        if (page == 1) {
            //PreferenceUtils.setNormalMenuItemCount1(update.size());
            PreferenceUtils.setNormalData1(JsonParser.getJsonStringFromObject(update));
        } else if (page == 2) {
            //PreferenceUtils.setNormalMenuItemCount2(update.size());
            PreferenceUtils.setNormalData2(JsonParser.getJsonStringFromObject(update));
        }
        for (NowKeyModelCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onNowKeyItemUpdatePosition(update, page);
            }
        }
    }

    public void deleteItem(BaseItemInfo delete, int page) {
        String items = null;
        if (page == 1) {
            items = PreferenceUtils.getNormalData1("");
        } else if (page == 2) {
            items = PreferenceUtils.getNormalData2("");
        }
        if (items != null && !"".equals(items)) {
            ArrayList<BaseItemInfo> baseInfos = JsonParser.loadItemsFromString(items);
            baseInfos.remove(delete);
            String save = JsonParser.getJsonStringFromObject(baseInfos);
            if (page == 1) {
                //PreferenceUtils.setNormalMenuItemCount1(baseInfos.size());
                PreferenceUtils.setNormalData1(save);
            } else if (page == 2) {
                //PreferenceUtils.setNormalMenuItemCount2(baseInfos.size());
                PreferenceUtils.setNormalData2(save);
            }
        }
        for (NowKeyModelCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onNowKeyItemDelete(delete, page);
            }
        }
    }

    public void deleteAll(int page) {
        String items = null;
        if (page == 1) {
            items = PreferenceUtils.getNormalData1("");
        } else if (page == 2) {
            items = PreferenceUtils.getNormalData2("");
        }
        if (items != null && !"".equals(items)) {
            ArrayList<BaseItemInfo> baseInfos = JsonParser.loadItemsFromString(items);
            baseInfos.clear();
            String save = JsonParser.getJsonStringFromObject(baseInfos);
            if (page == 1) {
                //PreferenceUtils.setNormalMenuItemCount1(baseInfos.size());
                PreferenceUtils.setNormalData1(save);
            } else if (page == 2) {
                //PreferenceUtils.setNormalMenuItemCount2(baseInfos.size());
                PreferenceUtils.setNormalData2(save);
            }
        }
        for (NowKeyModelCallback callback : mCallbacks) {
            if (callback != null) {
                callback.onNowKeyItemDelete(null, page);
            }
        }
    }

    public void onDestroy() {
        sInstance = null;
        mCallbacks = null;
        mContext = null;
        mDataPage1 = null;
        mDataPage2 = null;
        mExtraDatas = null;
    }
}
