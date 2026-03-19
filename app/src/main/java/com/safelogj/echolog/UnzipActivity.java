package com.safelogj.echolog;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.safelogj.echolog.databinding.ActivityUnzipBinding;


public class UnzipActivity extends AppCompatActivity {

    private AppController mController;
    private ActivityUnzipBinding mBinding;

    public void startAct(boolean error) {
        if (!error) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(getApplicationContext(), ErrorActivity.class));
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mBinding = ActivityUnzipBinding.inflate(getLayoutInflater());
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
            return WindowInsetsCompat.CONSUMED;
        });

        setLightStatusBar();
        mController = (AppController) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.lottieView.postDelayed(() -> {
            if (!mBinding.lottieView.isAnimating()) {
                mBinding.lottieView.setVisibility(View.INVISIBLE);
                mBinding.unzipTextView.setVisibility(View.VISIBLE);
            }
        }, 500);
        doResult();
    }

    private void doResult() {
        if (mController.ismError()) {
            startActivity(new Intent(getApplicationContext(), ErrorActivity.class));
            finish();
        } else if (mController.ismInit()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    private void setLightStatusBar() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        //  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.main_background));
        // }
    }
}