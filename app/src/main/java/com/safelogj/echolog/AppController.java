package com.safelogj.echolog;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.nativeads.NativeAd;
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener;
import com.yandex.mobile.ads.nativeads.NativeAdLoader;
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AppController extends Application {
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private File voskDir;
    private String text = "Говорите удерживая микрофон.";
    private float textSize;
    private NativeAd mNativeAd;
    private NativeAdLoader mNativeAdLoader;
    private File mFileDir;
    private Recognizer mRecognizer;
    private Model mModel;
    private boolean mInit;

    public boolean ismInit() {
        return mInit;
    }

    public NativeAd getNativeAd() {
        mExecutor.execute(this::loadNativeAd);
        return mNativeAd;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        writeSetting();
    }

    public File getVoskDir() {
        return voskDir;
    }

    public Recognizer getmRecognizer() {
        return mRecognizer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFileDir = getFilesDir();
        voskDir = new File(mFileDir, "vosk-model-small-ru-0.22");
        mNativeAdLoader = new NativeAdLoader(this);
        mNativeAdLoader.setNativeAdLoadListener(new NativeAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull final NativeAd nativeAd) {
                mainHandler.post(() -> mNativeAd = nativeAd);
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError error) {
                //
            }
        });
        mExecutor.execute(this::loadNativeAd);
        if (!voskDir.exists()) {
            mExecutor.execute(this::unzipAsset);
        } else {
            initRec();
        }
        readSettings();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (!mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
        if (mModel != null) mModel.close();
        if (mRecognizer != null) mRecognizer.close();
    }

    public void unzipAsset() {
        try (InputStream is = getAssets().open("voskru.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File file = new File(mFileDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            //
        }
        initRec();
    }

    private void loadNativeAd() {
        if (mNativeAdLoader != null) {
            mNativeAdLoader.loadAd(new NativeAdRequestConfiguration.Builder("demo-native-app-yandex9").build());
        }
    }
    public void writeSetting() {
        JSONObject settingsJson = new JSONObject();
        try {
            settingsJson.put("mTextSize", textSize);
            File settingsDir = new File(mFileDir, "settings");
            if (!settingsDir.exists()) {
                settingsDir.mkdirs();
            }
            File settingsFile = new File(settingsDir, "size.txt");
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                fos.write(settingsJson.toString().getBytes());
                fos.flush();
            }
            Log.e("MyApplication", "сохранении настроек: ");
        } catch (JSONException | IOException e) {
            Log.e("MyApplication", "Ошибка при сохранении настроек: " + e.getMessage());
        }

    }

    private void readSettings() {
        File settingsDir = new File(mFileDir, "settings");
        File settingsFile = new File(settingsDir, "size.txt");

        if (settingsFile.exists() && settingsFile.isFile()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                byte[] data = new byte[(int) settingsFile.length()];
                fis.read(data);
                String jsonStr = new String(data);
                JSONObject settingsJson = new JSONObject(jsonStr);
                textSize = (float) settingsJson.optDouble("mTextSize", 48f);
            } catch (IOException | JSONException e) {
                Log.e("MyApplication", "Ошибка при загрузке настроек: " + e.getMessage());
                textSize = 48f;
            }
        } else {
            textSize = 48f;
        }
    }
  private void initRec() {
      mModel = new Model(voskDir.getPath());
      mRecognizer = new Recognizer(mModel, 16000);
      mInit = true;
  }
}

