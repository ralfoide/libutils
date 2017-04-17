package com.alflabs.utils;

import android.util.Log;

import com.alflabs.annotations.Null;
import com.alflabs.libutils.BuildConfig;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public abstract class NetworkUtils {
    public static final String TAG = Utils.class.getSimpleName();
    public static final boolean DEBUG = BuildConfig.DEBUG;

    @Null
    public static String getWifiAndroidDeviceName() {
        try {
            // obvious shortcut is to try wlan0 first
            NetworkInterface ni = NetworkInterface.getByName("wlan0");
            Enumeration<NetworkInterface> it_ni = NetworkInterface.getNetworkInterfaces();
            do {
                if (ni != null) {
                    Enumeration<InetAddress> it_ia = ni.getInetAddresses();
                    if (it_ia.hasMoreElements()) {
                        while (it_ia.hasMoreElements()) {
                            InetAddress ia = it_ia.nextElement();
                            String ia_name = ia.getCanonicalHostName();
                            if (ia_name != null && !ia_name.equals(ia.getHostAddress())) {
                                return ia_name;
                            }
                        }
                    }
                }
                if (it_ni.hasMoreElements()) {
                    ni = it_ni.nextElement();
                } else {
                    break;
                }
            } while (ni != null);
        } catch (SocketException e) {
            if (DEBUG) Log.d(TAG, "getWifiAndroidDeviceName:  " + e);
        }
        return null;
    }
}
