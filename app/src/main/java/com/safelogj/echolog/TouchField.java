package com.safelogj.echolog;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;

public class TouchField  extends AppCompatButton {

    public TouchField(@NonNull Context context) {
        super(context);
    }

    public TouchField(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchField(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN : setBackgroundResource(R.drawable.mic_red_icon);
            return true;
            case MotionEvent.ACTION_UP : setBackgroundResource(R.drawable.mic_icon);
                performClick();
            return true;
            default: return false;
        }

    }
    @Override
    public boolean performClick() {
        super.performClick();
        Log.d("speech", "modelClick");
        return true;
    }

}
