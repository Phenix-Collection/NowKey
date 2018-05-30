package com.kuding.superball.Utils;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.kuding.superball.info.BaseItemInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.kuding.superball.FunctionActivity.GROUP_ALCATEL_ID;
import static com.kuding.superball.FunctionActivity.GROUP_APP_ID;
import static com.kuding.superball.FunctionActivity.GROUP_FUNCTION_ID;
import static com.kuding.superball.FunctionActivity.GROUP_OPERATION_ID;
import static com.kuding.superball.FunctionActivity.GROUP_TOOL_ID;

/**
 * Created by user on 17-2-8.
 */

public class NowKeyParser {
    private static final String TAG_GROUP = "group";
    private static final String TAG_KEY_WORD = "key_word";
    private static final String TAG_INDEX = "index";
    private static final String TAG_TYPE = "type";
    private static final String TAG_KEY_WORD_PAGE = "key_word_page";

    private static final String DEFAULT_NOWKEY_TAG = "defaultnowkey";
    private static final String DEFAULT_NOWKEY_PAGE_TAG = "defaultpage";


    public static HashMap<Integer, ArrayList<String>> parseDefaultNowKey(Context context, int xmlId)
            throws XmlPullParserException, IOException {
        XmlResourceParser parser = context.getResources().getXml(xmlId);
        beginDocument(parser, DEFAULT_NOWKEY_TAG);
        final int depth = parser.getDepth();
        int type;
//        public static final int GROUP_APP_ID = 0;
//        public static final int GROUP_OPERATION_ID = 1;
//        public static final int GROUP_FUNCTION_ID = 2;
//        public static final int GROUP_TOOL_ID = 3;
//        public static final int GROUP_PHONE_ID = 4;
//        public static final int GROUP_ALCATEL_ID = 5;
        HashMap<Integer, ArrayList<String>> datas = new HashMap<>();
        ArrayList<String> groupApps = new ArrayList<>();
        ArrayList<String> groupOperations = new ArrayList<>();
        ArrayList<String> groupFunctions = new ArrayList<>();
        ArrayList<String> groupTools = new ArrayList<>();
        ArrayList<String> groupPhones = new ArrayList<>();
        ArrayList<String> groupAlcatels = new ArrayList<>();

        String[] data;
        int groupId;
        String key_word;
        BaseItemInfo info;
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            data = addDefaultNowKey(parser);
            groupId = Integer.parseInt(data[0]);
            key_word = data[1];
            info = FunctionUtils.functionFilter(context, key_word);
            if (info != null) {
                if (groupId == GROUP_APP_ID) {
                    groupApps.add(key_word);
                } else if (groupId == GROUP_OPERATION_ID) {
                    groupOperations.add(key_word);
                } else if (groupId == GROUP_FUNCTION_ID) {
                    groupFunctions.add(key_word);
                } else if (groupId == GROUP_TOOL_ID) {
                    groupTools.add(key_word);
                }
//                else if (groupId == GROUP_PHONE_ID) {
//                    groupPhones.add(key_word);
//                }
                else if (groupId == GROUP_ALCATEL_ID) {
                    groupAlcatels.add(key_word);
                }
            }
        }


        if (!groupApps.isEmpty()) {
            datas.put(GROUP_APP_ID, groupApps);
        }
        if (!groupOperations.isEmpty()) {
            datas.put(GROUP_OPERATION_ID, groupOperations);
        }
        if (!groupFunctions.isEmpty()) {
            datas.put(GROUP_FUNCTION_ID, groupFunctions);
        }
        if (!groupTools.isEmpty()) {
            datas.put(GROUP_TOOL_ID, groupTools);
        }
//        if (!groupPhones.isEmpty()) {
//            datas.put(GROUP_PHONE_ID, groupPhones);
//        }
        if (!groupAlcatels.isEmpty()) {
            datas.put(GROUP_ALCATEL_ID, groupAlcatels);
        }
        return datas;
    }

    public static ArrayList<String> parseDefaultNowKeyToList(Context context, int xmlId)
            throws XmlPullParserException, IOException {
        XmlResourceParser parser = context.getResources().getXml(xmlId);
        beginDocument(parser, DEFAULT_NOWKEY_TAG);
        final int depth = parser.getDepth();
        int type;
//        public static final int GROUP_APP_ID = 0;
//        public static final int GROUP_OPERATION_ID = 1;
//        public static final int GROUP_FUNCTION_ID = 2;
//        public static final int GROUP_TOOL_ID = 3;
//        public static final int GROUP_PHONE_ID = 4;
//        public static final int GROUP_ALCATEL_ID = 5;
        ArrayList<String> datas = new ArrayList<>();
        ArrayList<String> groupApps = new ArrayList<>();
        ArrayList<String> groupOperations = new ArrayList<>();
        ArrayList<String> groupFunctions = new ArrayList<>();
        ArrayList<String> groupTools = new ArrayList<>();
        ArrayList<String> groupPhones = new ArrayList<>();
        ArrayList<String> groupAlcatels = new ArrayList<>();

        String[] data;
        int groupId;
        String key_word;
        BaseItemInfo info;
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            data = addDefaultNowKey(parser);
            groupId = Integer.parseInt(data[0]);
            key_word = data[1];
            info = FunctionUtils.functionFilter(context, key_word);
            if (info != null) {
                if (groupId == GROUP_APP_ID) {
                    groupApps.add(key_word);
                } else if (groupId == GROUP_OPERATION_ID) {
                    groupOperations.add(key_word);
                } else if (groupId == GROUP_FUNCTION_ID) {
                    groupFunctions.add(key_word);
                } else if (groupId == GROUP_TOOL_ID) {
                    groupTools.add(key_word);
                }
//                else if (groupId == GROUP_PHONE_ID) {
//                    groupPhones.add(key_word);
//                }
                else if (groupId == GROUP_ALCATEL_ID) {
                    groupAlcatels.add(key_word);
                }
            }
        }


        if (!groupApps.isEmpty()) {
            datas.addAll(groupApps);
        }
        if (!groupOperations.isEmpty()) {
            datas.addAll(groupOperations);
        }
        if (!groupFunctions.isEmpty()) {
            datas.addAll(groupFunctions);
        }
        if (!groupTools.isEmpty()) {
            datas.addAll(groupTools);
        }
//        if (!groupPhones.isEmpty()) {
//            datas.put(GROUP_PHONE_ID, groupPhones);
//        }
        if (!groupAlcatels.isEmpty()) {
            datas.addAll(groupAlcatels);
        }
        return datas;
    }

    private static String[] addDefaultNowKey(XmlResourceParser parser) {
        String group = getAttributeValue(parser, TAG_GROUP);
        String key_word = getAttributeValue(parser, TAG_KEY_WORD);
        String[] result = {group, key_word};
        return result;
    }


    public static ArrayList<BaseItemInfo> parseDefaultPage(Context context, int xmlId)
            throws XmlPullParserException, IOException {
        XmlResourceParser parser = context.getResources().getXml(xmlId);
        beginDocument(parser, DEFAULT_NOWKEY_PAGE_TAG);
        final int depth = parser.getDepth();
        int type;

        ArrayList<BaseItemInfo> result = new ArrayList<>();
        String[] data;
        int index;
        int itemType;
        String keyword;
        BaseItemInfo bInfo;
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            data = addDefaultPage(parser);
            index = Integer.parseInt(data[0]);
            itemType = Integer.parseInt(data[1]);
            keyword = data[2];
            if (FunctionUtils.functionFilter(context, keyword) != null) {
                bInfo = new BaseItemInfo();
                bInfo.setIndex(index);
                bInfo.setType(itemType);
                bInfo.setKey_word(keyword);
                result.add(bInfo);
            }

        }

        return result;
    }

    private static String[] addDefaultPage(XmlResourceParser parser) {
//        private static final String TAG_INDEX = "index";
//        private static final String TAG_TYPE = "type";
//        private static final String TAG_KEY_WORD_PAGE = "key_word_page";
        String index = getAttributeValue(parser, TAG_INDEX);
        String type = getAttributeValue(parser, TAG_TYPE);
        String key_word = getAttributeValue(parser, TAG_KEY_WORD_PAGE);
        String[] result = {index, type, key_word};
        return result;
    }


    private static final void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) ;

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    protected static String getAttributeValue(XmlResourceParser parser, String attribute) {
        String value = parser.getAttributeValue(
                "http://schemas.android.com/apk/res-auto/com.kuding.superball", attribute);
        if (value == null) {
            value = parser.getAttributeValue(null, attribute);
        }
        return value;
    }
}
