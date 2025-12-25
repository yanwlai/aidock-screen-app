package com.clevo.recorder;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordingService extends Service {
    private static final String TAG = "SystemRecorder";

    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    private boolean isRecording = false;

    // 录屏参数配置
    private static final int VIDEO_WIDTH = 720;  // 根据 Pixel 6a 调整，720p 较省空间
    private static final int VIDEO_HEIGHT = 1600;
    private static final int VIDEO_BITRATE = 6000000; // 6Mbps
    private static final int VIDEO_FRAMERATE = 30;
    private ParcelFileDescriptor mPfd;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");

        // 加锁或者简单的 boolean 检查，防止重入
        synchronized (this) {
            if (isRecording) {
                Log.w(TAG, "Service already recording, ignoring start request.");
                return START_STICKY;
            }
            // 确保 isRecording 只有在真正 start 成功后才置为 true，
            // 但为了防抖，可以在这里先置个标记，或者仅仅依赖单线程模型
            new Thread(this::startSystemRecording).start();
        }
        return START_STICKY;
    }

    @SuppressLint("WrongConstant")
    private void startSystemRecording() {
        Log.i(TAG, "Initializing System Recorder...");

        // 1: 获取 Device Protected Context (DE 存储上下文)
        // 这样获取的路径在解锁前也是可写的
        Context directBootContext = createDeviceProtectedStorageContext();

        // 2. 生成文件对象 (保存在 /data/user_de/0/com.clevo.recorder/files/)
        File outFile = getOutputFile(directBootContext);
        Log.i(TAG, "Saving video to DE storage: " + outFile.getAbsolutePath());

        try {
            // 3. 初始化 MediaRecorder
            mMediaRecorder = new MediaRecorder();

            // --- 音频配置 (系统内录) ---
            // REMOTE_SUBMIX 是关键，它录制“混合后的音频流”，即扬声器发出的声音
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX);

            // --- 视频配置 (Surface) ---
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // --- 编码设置 ---
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioEncodingBitRate(128000);
            mMediaRecorder.setAudioSamplingRate(44100);

            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setVideoEncodingBitRate(VIDEO_BITRATE);
            mMediaRecorder.setVideoFrameRate(VIDEO_FRAMERATE);
            mMediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);

            // 4: 使用 setOutputFile(File) 而不是 String
            // 或者更稳妥的方式：直接传入文件路径，但在 System UID 下，确保父目录存在
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            // 以“读写”模式打开文件
            mPfd = ParcelFileDescriptor.open(outFile, ParcelFileDescriptor.MODE_READ_WRITE);
            // 将文件描述符传给 MediaRecorder
            if (mPfd != null) {
                mMediaRecorder.setOutputFile(mPfd.getFileDescriptor());
            } else {
                Log.e(TAG, "Failed to obtain FileDescriptor");
                return;
            }
            mMediaRecorder.prepare();

            // 5. 创建 VirtualDisplay (核心黑科技)
            DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);

            // 获取屏幕密度
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getRealMetrics(metrics);

            // 关键点：createVirtualDisplay
            // 参数 1: name
            // 参数 2,3,4: 宽, 高, dpi
            // 参数 5: Surface (来自 MediaRecorder)
            // 参数 6: flag (VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR = 16)
            // 这一步因为你是 System App 且有 CAPTURE_VIDEO_OUTPUT，所以不需要 MediaProjection！
            mVirtualDisplay = dm.createVirtualDisplay(
                    "SystemScreenRecorder",
                    VIDEO_WIDTH, VIDEO_HEIGHT, metrics.densityDpi,
                    mMediaRecorder.getSurface(),
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR | DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
            );

            // 6. 开始录制
            mMediaRecorder.start();
            isRecording = true;
            Log.i(TAG, ">>> RECORDING STARTED SILENTLY <<<");

        } catch (IOException e) {
            Log.e(TAG, "MediaRecorder prepare failed", e);
            stopRecording();
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied! Check AndroidManifest and Whitelist", e);
        } catch (Exception e) {
            Log.e(TAG, "Generic error", e);
        }
    }

    // 辅助方法：生成 DE 存储路径下的文件
    private File getOutputFile(Context context) {
        // getFilesDir() 在 createDeviceProtectedStorageContext 下返回的是 DE 路径
        File dir = context.getFilesDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return new File(dir, "SYSREC_" + timeStamp + ".mp4");
    }

    private void stopRecording() {
        isRecording = false;
        try {
            if (mMediaRecorder != null) {
                // 经常会在 stop 时抛出异常（如果录制时间太短或启动失败），try-catch 是必须的
                mMediaRecorder.stop();
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Error stopping recorder (likely too short): " + e.getMessage());
        } finally {
            // 释放 MediaRecorder
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            // 释放 VirtualDisplay
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }
            // 关闭文件描述符
            if (mPfd != null) {
                try {
                    mPfd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mPfd = null;
            }
            Log.i(TAG, "Recording stopped and resources released.");
        }
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
