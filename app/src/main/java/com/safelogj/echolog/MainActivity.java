package com.safelogj.echolog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.safelogj.echolog.databinding.ActivityMainBinding;
import com.yandex.mobile.ads.nativeads.NativeAd;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private SpeechService mSpeechService;
    private int permCounter;
    private final ActivityResultCallback<Boolean> requestRecord = isGranted -> {
        if (Boolean.FALSE == isGranted) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            permCounter++;
            if (permCounter > 1) {
                permCounter = 0;
                openAppSettings();
            }
        }
    };
    private final ActivityResultLauncher<String> requestRecordPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), requestRecord);

    private ActivityMainBinding mBinding;
    private AppController mController;
    private float mTextSize;
    private String mText;
    private NativeAd mNativeAd;
    private Recognizer mRecognizer;
    private String partText;
    private StringBuilder mStringBuilderFin = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mController = (AppController) getApplication();
        if (mBinding.lottieView != null) {
            mBinding.lottieView.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://lottiefiles.com/free-animation/rotate-phone-QZQdb2qCwS"));
                startActivity(intent);
            });
        }
        setText();
        initBtn();

        if (mBinding.touchView != null) {
            mBinding.touchView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBinding.touchView.setBackgroundResource(R.drawable.mic_red_icon);
                    startSpeechRecognition();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mBinding.touchView.setBackgroundResource(R.drawable.mic_icon);
                    v.performClick();
                    stopSpeechRecognition();
                }
                return true;
            });
        }
        mRecognizer = mController.getmRecognizer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBinding.NativeView != null) {
            mNativeAd = mController.getNativeAd();
            if (mNativeAd != null) {
                mBinding.NativeView.setAd(mNativeAd);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setText();
        if(mBinding.touchView != null)mBinding.touchView.setBackgroundResource(R.drawable.mic_icon);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveText();
    }


    private void initBtn() {
        if (mBinding.textView != null) {
            if (mBinding.plusButton != null) {
                mBinding.plusButton.setOnClickListener(view -> {
                    if (mTextSize < 96) {
                        mTextSize += 8f;
                        mBinding.textView.setTextSize(mTextSize);
                    }
                });
            }
            if (mBinding.minusButton != null) {
                mBinding.minusButton.setOnClickListener(view -> {
                    if (mTextSize > 24) {
                        mTextSize -= 8f;
                        mBinding.textView.setTextSize(mTextSize);
                    }
                });
            }
        }
    }

    private void startSpeechRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestRecordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }

        if (mBinding.textView != null) {
           mStringBuilderFin = new StringBuilder();
            try {
                mSpeechService = new SpeechService(mRecognizer, 16000.0f);
                mSpeechService.startListening(new RecognitionListener() {
                    @Override
                    public void onPartialResult(String hypothesis) {
                        try {
                            JSONObject jsonObj = new JSONObject(hypothesis);
                            partText = jsonObj.getString("partial");
                            mBinding.textView.setText(partText);
                        } catch (JSONException e) {
                            //
                        }

                    }

                    @Override
                    public void onResult(String hypothesis) {
                        try {
                            JSONObject jsonObj = new JSONObject(hypothesis);
                            String text = jsonObj.getString("text");
                            mStringBuilderFin.append(text).append(". \n");
                        } catch (JSONException e) {
                            //
                        }
                    }

                    @Override
                    public void onFinalResult(String hypothesis) {
                        stopSpeechRecognition();
                    }

                    @Override
                    public void onError(Exception e) {
                        String mes = "Ошибка при распознавании речи: " + e.getLocalizedMessage();
                        mBinding.textView.setText(mes);
                    }

                    @Override
                    public void onTimeout() {
                        mBinding.textView.setText("Таймаут при распознавании речи");
                    }
                });
            } catch (IOException | NullPointerException e) {
                //
            }
        }

    }

    private void stopSpeechRecognition() {
        if (mSpeechService != null) {
            mSpeechService.stop();
            mSpeechService = null;
        }
        if (mBinding.textView != null) {
            mText = mStringBuilderFin.toString() + partText;
            if(!mText.contains("null")) mBinding.textView.setText(mText);
        }
    }
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void saveText() {
        if (mBinding.textView != null) {
            mController.setText(mText);
            mController.setTextSize(mTextSize);
        }
    }
    private void setText() {
        if (mBinding.textView != null) {
            mTextSize = mController.getTextSize();
            mText = mController.getText();
            mBinding.textView.setText(mText);
            mBinding.textView.setTextSize(mTextSize);
        }
    }
}