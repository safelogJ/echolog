package com.safelogj.echolog;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AppController extends Application {
    public static final String LOG_TAG = "mic";
    private static final String TEXT_SIZE_KEY = "textSizeKey";
    private static final String TEXT_COLOR_KEY = "textColorKey";
    private static final String FIELD_COLOR_KEY = "fieldColorKey";
    private static final float DEFAULT_TEXT_SIZE = 24f;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private StringBuilder stringBuilderFile = new StringBuilder();
    private File voskDir;
    private String text;
    private String mErrorText = "";
    private String mLibVersion = "";
    private float textSize;
    private File mFileDir;
    private Recognizer mRecognizer;
    private Model mModel;
    private boolean mInit;
    private boolean mError;
    private boolean isTablet;
    private ColorsPalette textColor;
    private ColorsPalette fieldColor;
    private WeakReference<Activity> currentActivityRef;

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

    public boolean isTablet() {
        return isTablet;
    }

    public Recognizer getRecognizer() {
        return mRecognizer;
    }

    public String getLibVersion() {
        return mLibVersion;
    }

    public void setLibVersion(String mLibVersion) {
        this.mLibVersion = mLibVersion;
    }

    public void setTextLine(String newLine) {
        if(newLine != null && !newLine.isEmpty() && !newLine.equals(getString(R.string.need_speak))) {
            stringBuilderFile.append(newLine).append("\n");
        }
    }

    public void setTextLines(String longLine) {
        stringBuilderFile = new StringBuilder(longLine);
        stringBuilderFile.append("\n");
    }

    public StringBuilder getStringBuilderFile() {
        return stringBuilderFile;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        regActivityListener();
        mFileDir = getFilesDir();
        voskDir = new File(mFileDir, "vosk-model-small-tr-0.3");
        //  voskDir = new File(mFileDir, "vosk-model-small-ru-0.22");

        mExecutor.execute(this::initUserVoskLib);
        readSettings();
        text = getString(R.string.need_speak);
        isTablet = getResources().getConfiguration().smallestScreenWidthDp >= 600;
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

    private void unzipAsset() {
        try (InputStream is = getAssets().open("vosk.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File file = new File(mFileDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!file.exists() && !file.mkdirs()) {
                        throw new IOException("Failed to create directory: " + file.getAbsolutePath());
                    }

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
            mError = initAppRecognizer();

        } catch (Exception e) {
            mError = true;
            mErrorText = getString(R.string.error_unzip);
        }
        callUnzipActivity(mError);
    }

    public void writeSetting() {
        JSONObject settingsJson = new JSONObject();
        try {
            settingsJson.put(TEXT_SIZE_KEY, textSize);
            settingsJson.put(FIELD_COLOR_KEY, fieldColor.ordinal());
            settingsJson.put(TEXT_COLOR_KEY, textColor.ordinal());
            File settingsDir = new File(mFileDir, "settings");
            if (!settingsDir.exists() && !settingsDir.mkdirs()) {
                return;
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
                textSize = (float) settingsJson.optDouble(TEXT_SIZE_KEY, DEFAULT_TEXT_SIZE);
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

    private boolean initAppRecognizer() {
        try {
            mModel = new Model(voskDir.getPath());
            mRecognizer = new Recognizer(mModel, 16000);
            mInit = true;
            mError = false;
            setLibVersion(voskDir.getName());
            return mError;
        } catch (Exception e) {
            mError = true;
            mInit = false;
            mErrorText = getString(R.string.error_init);
            return mError;
        }
    }

    private boolean initUserRecognizer(File userVoskLib) {
        try {
            mModel = new Model(userVoskLib.getPath());
            mRecognizer = new Recognizer(mModel, 16000);
            mInit = true;
            setLibVersion(userVoskLib.getName());
            return true;
        } catch (Exception e) {

            try {
                File innerDir = new File(userVoskLib, userVoskLib.getName());
                mModel = new Model(innerDir.getPath());
                mRecognizer = new Recognizer(mModel, 16000);
                mInit = true;
                setLibVersion(innerDir.getName());
                return true;
            } catch (Exception f) {
                return false;
            }
        }
    }

    private void setDefaultSettings() {
        textSize = DEFAULT_TEXT_SIZE;
        fieldColor = ColorsPalette.BLACK;
        textColor = ColorsPalette.WHITE;
    }

    private void regActivityListener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                //
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                currentActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                //
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                Activity current = currentActivityRef != null ? currentActivityRef.get() : null;
                if (current == activity) {
                    currentActivityRef = null;
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                //
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                //
            }
        });
    }

    private File getUserVoskDir() {
        File root = getExternalFilesDir(null);
        File userVoskLib = new File(root, "you_vosk_lib");
        if (!userVoskLib.isDirectory() && !userVoskLib.mkdirs()) {
         return null;
        }
        return userVoskLib;
    }

    private void initUserVoskLib() {
        File userVoskDir = getUserVoskDir();
        if (userVoskDir != null) {
            File[] userVoskDirMass = userVoskDir.listFiles();
           int massSize = userVoskDirMass == null ? 0 : userVoskDirMass.length;
           if (massSize == 1) {
               boolean initUserRec = initUserRecognizer(userVoskDirMass[0]);
               if (initUserRec) {
                   callUnzipActivity(false);
                   return;
               }
           }
        }
        initAppVoskLib();
    }

    private void initAppVoskLib () {
        if (!voskDir.exists()) {
              unzipAsset();
        } else {
            mError = initAppRecognizer();
            callUnzipActivity(mError);
        }
    }
    private void callUnzipActivity(boolean error) {
        Activity current = currentActivityRef != null ? currentActivityRef.get() : null;
        if (current != null && current.getClass() == UnzipActivity.class) {
            ((UnzipActivity) current).startAct(error);
        }
    }
}

