package com.kuding.nowkey.info;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 17-1-12.
 */

public class BaseItemInfo implements Parcelable {

    protected int index;
    protected int type;
    protected String key_word;
    protected int angle;//角度，子菜单在转盘中的角度

    public BaseItemInfo() {
    }

    public BaseItemInfo(int i, int t, String keyword) {
        index = i;
        type = t;
        key_word = keyword;
    }

    public BaseItemInfo(int i, int t, String keyword,int angle) {
        index = i;
        type = t;
        key_word = keyword;
        this.angle = angle;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey_word() {
        return key_word;
    }

    public void setKey_word(String key_word) {
        this.key_word = key_word;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "BaseItemInfo{" +
                "index=" + index +
                ", type=" + type +
                ", key_word='" + key_word + '\'' +
                ", angle=" + angle +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        BaseItemInfo info = (BaseItemInfo) o;
        return type == info.type &&
                key_word.equals(info.key_word);
    }

    @Override
    public int hashCode() {
        return key_word.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(index);
        parcel.writeInt(type);
        parcel.writeInt(angle);
        parcel.writeString(key_word);
    }

    public static final Parcelable.Creator<BaseItemInfo> CREATOR = new Creator<BaseItemInfo>() {

        @Override
        public BaseItemInfo createFromParcel(Parcel source) {
            return new BaseItemInfo(source.readInt(), source.readInt(),source.readString(),source.readInt());
        }

        @Override
        public BaseItemInfo[] newArray(int size) {
            return new BaseItemInfo[size];
        }
    };
}
