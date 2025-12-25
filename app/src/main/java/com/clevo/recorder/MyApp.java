package com.clevo.recorder;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

public class MyApp extends Application {
    private static final String TAG = "AIdockScreenApp";
    private ContentObserver provisionObserver;

    @Override
    public void onCreate() {
        super.onCreate();

        // 检查设备是否已经完成初始化设置 (0 = 未完成, 1 = 已完成)
        boolean isProvisioned = isDeviceProvisioned(this);

        if (!isProvisioned) {
            Log.i(TAG, "Device not provisioned yet. Waiting for Setup Wizard to finish...");
            registerProvisioningObserver();
        } else {
            Log.i(TAG, "Device already provisioned.");
        }
    }

    // 辅助方法：检查设置标记
    public static boolean isDeviceProvisioned(Context context) {
        try {
            return Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.DEVICE_PROVISIONED,
                    0
            ) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    // 注册监听器，监控 DEVICE_PROVISIONED 值的变化
    private void registerProvisioningObserver() {
        provisionObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (isDeviceProvisioned(MyApp.this)) {
                    Log.i(TAG, "Setup Wizard finished! Launching app now.");

                    // 设置完成了，启动 Service
                    startRecordingService();

                    // 注销监听，防止重复触发
                    getContentResolver().unregisterContentObserver(this);
                    provisionObserver = null;
                }
            }
        };

        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.DEVICE_PROVISIONED),
                false,
                provisionObserver
        );
    }

    public void startRecordingService() {
        Intent serviceIntent = new Intent(this, RecordingService.class);
        // 对于系统应用，即便是从后台启动 Service 也是允许的，
        // 特别是 persistent 应用。但为了兼容性，建议使用 startService
        startService(serviceIntent);
    }
}
