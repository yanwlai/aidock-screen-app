package com.clevo.recorder;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SystemUtils {
    /**
     * 设置状态栏来文字颜色
     *
     * @param isDark true: 浅色（黑色）文字 false: 深色（白色）文字
     */
    public static void setStatusBarColor(Activity activity, boolean isDark) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(isDark);
        } else {
            View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (isDark) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(flags);
        }
    }

    public static int getStatusBarHeight(Context context) {
        return context.getResources().getSystem().getDimensionPixelSize(
                context.getResources().getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }

    public static String getAddressByLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        String result = "";
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                result = address.getAddressLine(0);
                if (TextUtils.isEmpty(result)) {
                    String province = address.getAdminArea();
                    String city = address.getLocality();
                    String area = address.getSubLocality();
                    String street = address.getThoroughfare();
                    String number = address.getSubThoroughfare();

                    StringBuilder sb = new StringBuilder();
                    if (!TextUtils.isEmpty(province)) {
                        sb.append(province);
                    }
                    if (!TextUtils.isEmpty(city)) {
                        sb.append(city);
                    }
                    if (!TextUtils.isEmpty(area)) {
                        sb.append(area);
                    }
                    if (!TextUtils.isEmpty(street)) {
                        sb.append(street);
                    }
                    if (!TextUtils.isEmpty(number)) {
                        sb.append(number);
                    }
                    result = sb.toString();
                }
            }
        } catch (IOException e) {
            Log.e("geoc", e.toString());
        }
        return result;
    }
}
