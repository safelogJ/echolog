package com.safelogj.echolog;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.safelogj.echolog.databinding.ActivityNoticeBinding;

public class NoticeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityNoticeBinding binding = ActivityNoticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AppController controller = (AppController) getApplication();
        binding.noticeTextView.setText(HtmlCompat.fromHtml(getString(R.string.notice), HtmlCompat.FROM_HTML_MODE_LEGACY));
        binding.noticeTextView.setTextSize(controller.getTextSize());
        binding.noticeTextView.setMovementMethod(LinkMovementMethod.getInstance());

    }

}