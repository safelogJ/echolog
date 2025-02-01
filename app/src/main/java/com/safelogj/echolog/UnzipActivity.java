package com.safelogj.echolog;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import com.safelogj.echolog.databinding.ActivityUnzipBinding;


public class UnzipActivity extends AppCompatActivity {

    private AppController mController;
    private int permCounter;
    private final ActivityResultCallback<Boolean> requestRecord = isGranted -> {
    if (Boolean.FALSE == isGranted) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            permCounter++;
            if (permCounter > 2) {
                permCounter = 0;
                openAppSettings();
            }
        }
    };
    private final ActivityResultLauncher<String> requestRecordPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), requestRecord);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityUnzipBinding mBinding = ActivityUnzipBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mController = (AppController) getApplication();
        mBinding.lottieView.addAnimatorListener(new AnimatorListenerAdapter() {


            @Override
            public void onAnimationRepeat(Animator animation) {
                if (!isPerm()) {
                    requestRecordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                } else if (mController.ismInit() && isPerm()) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });
        mBinding.lottieView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://lottiefiles.com/free-animation/loading-mqIYlHgteQ"));
            startActivity(intent);
        });
    }
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private boolean isPerm() {
       return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
}