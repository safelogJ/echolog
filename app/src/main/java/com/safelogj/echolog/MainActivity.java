package com.safelogj.echolog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.text.BidiFormatter;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.safelogj.echolog.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;


public class MainActivity extends AppCompatActivity {
    private static final String EMPTY_RESULT_REGEX = "(\\. \\n)+";
    private final BidiFormatter bidiFormatter = BidiFormatter.getInstance();
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
    private Recognizer mRecognizer;
    private String partText;
    private StringBuilder mStringBuilderFin;
    private ClipboardManager clipboard;

    public void setClipboardText() {
        if (mBinding.textView != null && clipboard != null && mText != null && !mText.isEmpty()) {
                clipboard.setPrimaryClip(ClipData.newPlainText(null, mText));
                Toast.makeText(this, R.string.copied_text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets gestureInsets = insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures());
            int leftPadding = Math.max(gestureInsets.left, systemBarInsets.left);
            int rightPadding = Math.max(gestureInsets.right, systemBarInsets.right);
            int bottomPadding = Math.max(gestureInsets.bottom, systemBarInsets.bottom);
            int leftPaddingLand = Math.max(leftPadding, systemBarInsets.top);
            int rightPaddingLand = Math.max(rightPadding, systemBarInsets.top);

            if (v.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                v.setPadding(leftPaddingLand, systemBarInsets.top, rightPaddingLand, bottomPadding);
            } else {
                v.setPadding(leftPadding, systemBarInsets.top, rightPadding, bottomPadding);
            }

            if (leftPadding > rightPadding) {
                moveBtnToLeft();
            }
            return WindowInsetsCompat.CONSUMED;
        });

        setLightStatusBar();

        mController = (AppController) getApplication();
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        initSizeBtn();
        initNavigationBtn();
        initMicBtn();
        initTextViewListener();
        mRecognizer = mController.getRecognizer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setText();
        setLibVersionText();
        initNavBtnColors();
        if (mBinding.touchView != null)
            mBinding.touchView.setBackgroundResource(R.drawable.mic_icon);
        if (mBinding.lottieView != null && mBinding.rotateImgView != null) {
            mBinding.lottieView.postDelayed(() -> {
                if (!mBinding.lottieView.isAnimating()) {
                    mBinding.lottieView.setVisibility(View.INVISIBLE);
                    mBinding.rotateImgView.setVisibility(View.VISIBLE);
                }
            }, 500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveText();
    }

    @Override
    protected void onStop() {
        if(mStringBuilderFin != null) {
            stopVoskService();
        }
        if (mBinding.textInputEditText != null) {
            Editable longText = mBinding.textInputEditText.getText();
            if (longText != null) {
                mController.setTextLines(longText.toString().trim());
            }
        }
        super.onStop();
    }

    private void initSizeBtn() {
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

    private void initNavigationBtn() {
        if (mBinding.noticeButton != null) {
            mBinding.noticeButton.setOnClickListener(view -> startActivity(new Intent(this, NoticeActivity.class)));
        }

        if (mBinding.colorButton != null) {
            mBinding.colorButton.setOnClickListener(view -> startActivity(new Intent(this, ColorsActivity.class)));
        }

        if (mBinding.youtubeButton != null) {
            mBinding.youtubeButton.setOnClickListener(view -> openYoutubeLink());
        }
    }

    private void initNavBtnColors() {
        if (mBinding.noticeButton != null) {
            mBinding.noticeButton.setTextColor(mController.getTextColor().getColor());
            mBinding.noticeButton.setBackgroundColor(mController.getFieldColor().getColor());
        }
        if (mBinding.colorButton != null) {
            mBinding.colorButton.setTextColor(mController.getTextColor().getColor());
            mBinding.colorButton.setBackgroundColor(mController.getFieldColor().getColor());
        }
        mBinding.startActCardView.setCardBackgroundColor(mController.getFieldColor().getColor());

    }

    private void initMicBtn() {
        if (mBinding.touchView != null && mBinding.textViewColdStartInfo != null) {
            mBinding.touchView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBinding.textViewColdStartInfo.setVisibility(View.INVISIBLE);
                    startSpeechRecognition();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mBinding.textViewColdStartInfo.setVisibility(View.INVISIBLE);
                    stopVoskService();
                    v.performClick();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL && mStringBuilderFin != null) {
                    mBinding.textViewColdStartInfo.setVisibility(View.VISIBLE);
                }
                return true;
            });
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBinding.touchView.getLayoutParams();
            if (mController.isTablet()) {
                params.matchConstraintPercentHeight = 0.23f;
            } else {
                params.matchConstraintPercentHeight = 0.32f;
            }
            mBinding.touchView.setLayoutParams(params);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTextViewListener() {
            mBinding.scrollView2.setOnTouchListener(new DoubleTapListener(this));
    }


    private void startSpeechRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestRecordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }

        if (mBinding.textView != null) {
            if(mStringBuilderFin != null) {
                stopVoskService();
                return;
            }
            mStringBuilderFin = new StringBuilder();
            try {
                if (mBinding.touchView != null) {
                    mBinding.touchView.setBackgroundResource(R.drawable.mic_red_icon);
                }
                mSpeechService = new SpeechService(mRecognizer, 16000.0f);
                mSpeechService.startListening(new RecognitionListener() {
                    @Override
                    public void onPartialResult(String hypothesis) {
                        try {
                            JSONObject jsonObj = new JSONObject(hypothesis);
                            partText = jsonObj.getString("partial");
                            mBinding.textView.setText(bidiFormatter.unicodeWrap(partText));
                        } catch (JSONException e) {
                            //
                        }

                    }

                    @Override
                    public void onResult(String hypothesis) {
                        try {
                            JSONObject jsonObj = new JSONObject(hypothesis);
                            String text = jsonObj.getString("text");
                            if (mStringBuilderFin != null &&!text.isEmpty()) {
                                mStringBuilderFin.append(text).append(". \n");
                                partText = "";
                            }
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
                        String mes = getString(R.string.error_speech) + e.getLocalizedMessage();
                        mBinding.textView.setText(bidiFormatter.unicodeWrap(mes));
                    }

                    @Override
                    public void onTimeout() {
                        mBinding.textView.setText(bidiFormatter.unicodeWrap(getString(R.string.error_timeout)));
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.error_speech), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void stopSpeechRecognition() {
        if (mBinding.touchView != null) {
            mBinding.touchView.setBackgroundResource(R.drawable.mic_icon);
        }
        saveRecResult();
        mStringBuilderFin = null;
    }

    private void saveRecResult() {
        Log.d(AppController.LOG_TAG, "билд и сохранение текстов");
            buildText();
            mController.setTextLine(mText);
            saveText();
    }

    private void stopVoskService() {
        if (mSpeechService != null) {
            mSpeechService.stop();
            mSpeechService = null;
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
        mTextSize = mController.getTextSize();
        if (mBinding.textView != null) {
            mText = mController.getText();
            mBinding.textView.setText(bidiFormatter.unicodeWrap(mText));
            mBinding.textView.setTextSize(mTextSize);
            mBinding.textView.setTextColor(mController.getTextColor().getColor());
        } else if (mBinding.textInputEditText != null) {
            mBinding.textInputEditText.setTextSize(mTextSize);
            mBinding.textInputEditText.setTextColor(mController.getTextColor().getColor());
            mBinding.textInputEditText.setText(bidiFormatter.unicodeWrap(mController.getStringBuilderFile().toString().trim()));
        }
    }

    private void buildText() {
        if (mBinding.textView != null && mStringBuilderFin != null) {
            String result = mStringBuilderFin.append(partText).toString();
            if (!result.isEmpty() && !result.contains("null") && !result.matches(EMPTY_RESULT_REGEX)) {
                mText = result;
            } else {
                mText = getString(R.string.need_speak);
            }
            mBinding.textView.setText(bidiFormatter.unicodeWrap(mText));
        }
    }

    private void setLibVersionText() {
        if (mBinding.textViewLibInfo != null) {
            mBinding.textViewLibInfo.setText(mController.getLibVersion());
            mBinding.textViewLibInfo.setTextColor(mController.getTextColor().getColor());
        }
    }

    private void setLightStatusBar() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.main_background));
    }

    private void moveBtnToLeft() {
        if (mBinding.startGuideLeft != null && mBinding.startGuideLeftBtn != null
                && mBinding.startGuideRight != null && mBinding.startGuideRightBtn != null) {
            mBinding.startGuideLeft.setGuidelinePercent(0.13f);
            mBinding.startGuideRight.setGuidelinePercent(1f);
            mBinding.startGuideLeftBtn.setGuidelinePercent(0f);
            mBinding.startGuideRightBtn.setGuidelinePercent(0.13f);

        }
    }

    private void openYoutubeLink() {
        try {
            Uri webpage = Uri.parse("https://www.youtube.com/watch?v=maNAYwyKn94");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(intent);
        } catch (Exception e) {
            //
        }
    }
}