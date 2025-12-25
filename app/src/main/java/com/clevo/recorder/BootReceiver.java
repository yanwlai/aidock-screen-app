package com.clevo.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "AIdockScreenAppBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 先检查是否完成了引导设置
        if (!MyApp.isDeviceProvisioned(context)) {
            Log.w(TAG, "Device setup not done. Service will not start yet.");
            return;
        }
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "Locked boot completed. Starting Recording Service...");
            startRecordingService(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "Boot completed. Starting Recording Service...");
            startRecordingService(context);
        }
    }

    private void startRecordingService(Context context) {
        Intent serviceIntent = new Intent(context, RecordingService.class);
        context.startService(serviceIntent);
    }
}
