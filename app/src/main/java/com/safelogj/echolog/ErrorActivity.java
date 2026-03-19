package com.safelogj.echolog;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.safelogj.echolog.databinding.ActivityErrorBinding;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityErrorBinding binding = ActivityErrorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
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

        AppController appController = (AppController) getApplication();
        binding.errorActCardView.setCardBackgroundColor(appController.getFieldColor().getColor());
        binding.errorTextView.setTextColor(appController.getTextColor().getColor());
        binding.errorTextView.setText(appController.getErrorText());
       // binding.errorTextView.setText(getString(R.string.error_init));
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