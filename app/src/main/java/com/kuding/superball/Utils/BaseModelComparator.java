package com.kuding.superball.Utils;

import com.kuding.superball.info.BaseItemInfo;

import java.util.Comparator;

/**
 * Created by user on 17-1-12.
 */

public class BaseModelComparator implements Comparator<BaseItemInfo> {
    @Override
    public int compare(BaseItemInfo baseItemInfo, BaseItemInfo t1) {
        int index1 = baseItemInfo.getIndex();
        int index2 = t1.getIndex();
        if (index1 < index2) return  -1;
        if (index1 > index2) return  1;
        return 0;
    }
}
