package com.safelogj.echolog;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.safelogj.echolog.databinding.ActivityColorsBinding;

import java.util.ArrayList;
import java.util.List;

public class ColorsActivity extends AppCompatActivity {
    private ActivityColorsBinding mBinding;
    private AppController mController;
    private ColorsPalette[] mTextColors = new ColorsPalette[ColorsPalette.values().length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mBinding = ActivityColorsBinding.inflate(getLayoutInflater());
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
        mBinding.spinnerFieldColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mController.setFieldColor(ColorsPalette.values()[position]);
                mBinding.colorScrollView.setBackgroundColor(mController.getFieldColor().getColor());
                setTextSpinnerAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Действие, если ничего не выбрано
            }
        });

        mBinding.spinnerTextColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mController.setTextColor(mTextColors[position]);
                mBinding.colorsTextView.setTextColor(mTextColors[position].getColor());
                mBinding.spinnerTextColor.setBackgroundColor(mTextColors[position].getColor());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Действие, если ничего не выбрано
            }
        });
        mBinding.colorsTextView.setText(HtmlCompat.fromHtml(getString(R.string.color_view_text), HtmlCompat.FROM_HTML_MODE_LEGACY));
        mBinding.spinnerFieldColor.setAdapter(getFieldSpinnerAdapter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.colorsTextView.setTextColor(mController.getTextColor().getColor());
        mBinding.colorScrollView.setBackgroundColor(mController.getFieldColor().getColor());
        mBinding.colorsTextView.setTextSize(mController.getTextSize());

        mBinding.spinnerFieldColor.setBackgroundColor(mController.getFieldColor().getColor());
        mBinding.spinnerFieldColor.setSelection(mController.getFieldColor().ordinal());
    }

    private ColorsSpinnerAdapter getFieldSpinnerAdapter() {
        return new ColorsSpinnerAdapter(this, ColorsPalette.values());
    }

    private void setTextSpinnerAdapter() {
        List<ColorsPalette> textColors = new ArrayList<>();
        ColorsPalette fieldColor = mController.getFieldColor();
        for (ColorsPalette color : ColorsPalette.values()) {
            if (color != fieldColor) {
                textColors.add(color);
            }
        }
        mTextColors = textColors.toArray(new ColorsPalette[0]);
        ColorsSpinnerAdapter adapter = new ColorsSpinnerAdapter(this, mTextColors);
        mBinding.spinnerTextColor.setAdapter(adapter);
        mBinding.spinnerTextColor.setSelection(getPositionTextColor());
    }

    private int getPositionTextColor() {
        int textColorsLength = mTextColors.length;
        ColorsPalette textColor = mController.getTextColor();
        for (int i = 0; i < textColorsLength; i++) {
            if (textColor == mTextColors[i]) {
                return i;
            }
        }
        return 0;
    }

    private void setLightStatusBar() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.main_background));
        // }
    }
}