package android.util;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

public class SpoofConfig {
    private static final String CONFIG_PATH = "/data/system/spoof_config.json";
    private static final SpoofMap<String, Object> mCache = new SpoofMap<>();
    private static long mLastLoadTime = 0;

    private static void reload() {
        try {
            File file = new File(CONFIG_PATH);
            if (!file.exists()) return;
            if (file.lastModified() == mLastLoadTime) return;

            FileInputStream is = new FileInputStream(file);
            byte[] size = new byte[is.available()];
            is.read(size);
            is.close();

            String jsonStr = new String(size, "UTF-8");
            JSONObject json = new JSONObject(jsonStr);
            mCache.clear();
            mCache.put("network_country_iso", json.optString("network_country_iso"));
            mCache.put("network_operator_name", json.optString("network_operator_name"));

            mCache.put("product_manufacturer", json.optString("product_manufacturer"));
            mCache.put("product_brand", json.optString("product_brand"));
            mCache.put("product_model", json.optString("product_model"));
            mCache.put("product_board", json.optString("product_board"));
            mCache.put("product_device", json.optString("product_device"));
            mCache.put("hardware", json.optString("hardware"));
            mCache.put("product_name", json.optString("product_name"));
            mCache.put("build_version_release", json.optString("build_version_release"));
            mCache.put("build_host", json.optString("build_host"));
            mCache.put("build_id", json.optString("build_id"));
            mCache.put("build_version_incremental", json.optString("build_version_incremental"));

            mCache.put("available_processors", json.optInt("available_processors"));
            mCache.put("cpu_abilist", json.optString("cpu_abilist"));

            mCache.put("gles_version", json.optString("gles_version"));

            mCache.put("width", json.optInt("width"));
            mCache.put("height", json.optInt("height"));

            mLastLoadTime = file.lastModified();
            Log.d("SpoofConfig", "Config loaded successfully");
        } catch (Exception e) {
            Log.e("SpoofConfig", "Error loading config");
        }
    }

    public static boolean isConfigFileExist() {
        File file = new File(CONFIG_PATH);
        return file.exists();
    }

    public static <T> T get(String key, T defValue) {
        reload();
        if (mCache.containsKey(key)) {
            return mCache.get(key, defValue);
        }
        return defValue;
    }

    private static class SpoofMap<K, V> extends HashMap<K, V> {
        public <T> T get(Object key, T defValue) {
            V v = super.get(key);
            if (v != null) {
                return (T) v;
            }
            return defValue;
        }
    }
}
