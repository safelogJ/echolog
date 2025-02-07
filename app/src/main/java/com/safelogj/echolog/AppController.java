package com.safelogj.echolog;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.nativeads.NativeAd;
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener;
import com.yandex.mobile.ads.nativeads.NativeAdLoader;
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration;

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AppController extends Application {
    private static final String TEXT_SIZE_KEY = "textSizeKey";
    private static final String TEXT_COLOR_KEY = "textColorKey";
    private static final String FIELD_COLOR_KEY = "fieldColorKey";
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private File voskDir;
    private String text = "Говорите удерживая микрофон.";
    private String mErrorText = "";
    private float textSize;
    private NativeAd mNativeAd;
    private NativeAdLoader mNativeAdLoader;
    private File mFileDir;
    private Recognizer mRecognizer;
    private Model mModel;
    private boolean mInit;
    private boolean mError;
    private ColorsPalette textColor;
    private ColorsPalette fieldColor;

    public ColorsPalette getTextColor() {
        return textColor;
    }

    public void setTextColor(ColorsPalette textColor) {
        this.textColor = textColor;
        writeSetting();
    }

    public ColorsPalette getFieldColor() {
        return fieldColor;
    }

    public void setFieldColor(ColorsPalette fieldColor) {
        this.fieldColor = fieldColor;
        writeSetting();
    }

    public boolean ismInit() {
        return mInit;
    }

    public String getErrorText() {
        return mErrorText;
    }

    public boolean ismError() {
        return mError;
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

    public Recognizer getRecognizer() {
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
        MobileAds.setAgeRestrictedUser(true);
        MobileAds.initialize(this, () -> {
        });
        mExecutor.execute(this::loadNativeAd);
        if (!voskDir.exists()) {
            mExecutor.execute(this::unzipAsset);
        } else {
            initRecognizer();
        }
        readSettings();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (!mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
        if (mRecognizer != null) {
            mRecognizer.close();
            mRecognizer = null;
        }
        if (mModel != null) {
            mModel.close();
            mModel = null;
        }
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
            initRecognizer();
        } catch (Exception e) {
            mError = true;
            mErrorText = getString(R.string.error_unzip);
        }

    }

    private void loadNativeAd() {
        if (mNativeAdLoader != null) {
            mNativeAdLoader.loadAd(new NativeAdRequestConfiguration.Builder("R-M-13943864-2").build());
        }
    }

    public void writeSetting() {
        JSONObject settingsJson = new JSONObject();
        try {
            settingsJson.put(TEXT_SIZE_KEY, textSize);
            settingsJson.put(FIELD_COLOR_KEY, fieldColor.ordinal());
            settingsJson.put(TEXT_COLOR_KEY, textColor.ordinal());
            File settingsDir = new File(mFileDir, "settings");
            if (!settingsDir.exists()) {
                settingsDir.mkdirs();
            }
            File settingsFile = new File(settingsDir, "size.txt");
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                fos.write(settingsJson.toString().getBytes());
                fos.flush();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_write), Toast.LENGTH_LONG).show();
        }

    }

    private void readSettings() {
        File settingsDir = new File(mFileDir, "settings");
        File settingsFile = new File(settingsDir, "size.txt");

        if (settingsFile.exists() && settingsFile.isFile()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                byte[] data = new byte[(int) settingsFile.length()];
                int bytesRead;
                int totalRead = 0;

                while (totalRead < data.length &&
                        (bytesRead = fis.read(data, totalRead, data.length - totalRead)) != -1) {
                    totalRead += bytesRead;
                }
                String jsonStr = new String(data, 0, totalRead);
                JSONObject settingsJson = new JSONObject(jsonStr);
                textSize = (float) settingsJson.optDouble(TEXT_SIZE_KEY, 48f);
                fieldColor = ColorsPalette.values()[settingsJson.optInt(FIELD_COLOR_KEY, ColorsPalette.BLACK.ordinal())];
                textColor = ColorsPalette.values()[settingsJson.optInt(TEXT_COLOR_KEY, ColorsPalette.WHITE.ordinal())];
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.error_read), Toast.LENGTH_LONG).show();
                setDefaultSettings();
            }
        } else {
            setDefaultSettings();
        }
    }

    private void initRecognizer() {
        try {
            mModel = new Model(voskDir.getPath());
            mRecognizer = new Recognizer(mModel, 16000);
            mInit = true;
        } catch (Exception e) {
            mError = true;
            mInit = false;
            mErrorText = getString(R.string.error_init);
        }

    }

    private void setDefaultSettings() {
        textSize = 48f;
        fieldColor = ColorsPalette.BLACK;
        textColor = ColorsPalette.WHITE;
    }
}

