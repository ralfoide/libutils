package com.alflabs.utils;

import android.annotation.SuppressLint;
import android.view.Menu;
import android.widget.PopupMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MenuHelper {

    /**
     * PopupMenu.setForceShowIcon() is only publicly available in API 29.
     * It's available before via the internal PopupMenuHelper.
     * TODO move to LibUtils ApiHelper
     */
    public static void PopupMenu_setForceShowIcon(PopupMenu popupMenu, boolean forceShowIcon) {
        // 1- Call API 29 if available (this app is still API 28 max for now)
        // https://developer.android.com/reference/android/widget/PopupMenu#setForceShowIcon(boolean)
        try {
            Method setter = popupMenu.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);  // NLS
            setter.setAccessible(true);
            setter.invoke(popupMenu, forceShowIcon);
            return;
        } catch (Exception ignore) {
            // if (DEBUG) Log.d(TAG, "PopupMenu_setForceShowIcon - API 29: ", ignore);
        }

        // 2- If that fails, use the legacy internal helper
        try {
            // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/widget/PopupMenu.java
            // get "MenuPopupHelper mPopup" field from instance popupMenu;
            @SuppressLint("DiscouragedPrivateApi")
            Field field_mPopup = PopupMenu.class.getDeclaredField("mPopup");    // NLS
            field_mPopup.setAccessible(true);
            Object instance_mPopup = field_mPopup.get(popupMenu);
            // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/com/android/internal/view/menu/MenuPopupHelper.java
            Method setter = instance_mPopup.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);    // NLS
            setter.setAccessible(true);
            setter.invoke(instance_mPopup, forceShowIcon);

        } catch (Exception ignore) {
            // if (DEBUG) Log.d(TAG, "PopupMenu_setForceShowIcon - API <29: ", ignore);
        }
    }

    @SuppressLint("RestrictedApi")
    public static void Menu_setOptionalIconsVisible(Menu menu, boolean iconsVisible) {
        try {
            // Note: we don't want an import dependency on androidx in LibUtilsAndroid
            // when compiling on legacy SDKs. Let it fail with ClassNotFoundException.
            Class<?> menuBuilderClazz = Class.forName("androidx.appcompat.view.menu.MenuBuilder");

            if (menuBuilderClazz.isInstance(menu)) {
                Method setter = menuBuilderClazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);    // NLS
                setter.setAccessible(true);
                setter.invoke(menu, iconsVisible);
            }
        } catch (Exception ignore) {
            // if (DEBUG) Log.d(TAG, "Menu_setOptionalIconsVisible", ignore);
        }
    }

}
