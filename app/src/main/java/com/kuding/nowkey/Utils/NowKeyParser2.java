package com.kuding.nowkey.Utils;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.kuding.nowkey.info.BaseItemInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by user on 17-2-8.
 */

public class NowKeyParser2 {
    private static final String TAG_KEY_WORD = "key_word";
    private static final String DEFAULT_NOWKEY_TAG = "defaultnowkey";

    public static  ArrayList<String> parseDefaultNowKey(Context context, int xmlId)
            throws XmlPullParserException, IOException {
        XmlResourceParser parser = context.getResources().getXml(xmlId);
        beginDocument(parser, DEFAULT_NOWKEY_TAG);
        final int depth = parser.getDepth();
        int type;

        ArrayList<String> mDataList = new ArrayList<>();

        String[] data;
        String key_word;
        BaseItemInfo info;
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            data = addDefaultNowKey(parser);
            key_word = data[0];
            info = FunctionUtils.functionFilter(context, key_word);
            if (info != null) {
                mDataList.add(key_word);
            }
        }
        return mDataList;
    }

    private static String[] addDefaultNowKey(XmlResourceParser parser) {
        String key_word = getAttributeValue(parser, TAG_KEY_WORD);
        String[] result = {key_word};
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
                "http://schemas.android.com/apk/res-auto/com.kuding.nowkey", attribute);
        if (value == null) {
            value = parser.getAttributeValue(null, attribute);
        }
        return value;
    }
}
