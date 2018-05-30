package com.kuding.superball.applist;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

import java.util.Objects;

/**
 * Created by user on 17-1-11.
 */

public class AppInfoModel {

    private String appName;
    private String sortLetters;  //显示数据拼音的首字母
    private Drawable appIcon;
    private ComponentName componentName;
    private String packageName;
    private boolean isDuplicate = false;    //用于判断用户是否已经选择了该app作为快捷方式，如果是，则置灰，并且不能选择

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public ComponentName getComponentName() {
        return componentName;
    }

    public void setComponentName(ComponentName componentName) {
        this.componentName = componentName;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppInfoModel that = (AppInfoModel) o;
        return Objects.equals(appName, that.appName) &&
                Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName);
    }
}
