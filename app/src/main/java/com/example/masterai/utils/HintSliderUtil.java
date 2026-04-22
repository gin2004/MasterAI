package com.example.masterai.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import androidx.core.content.ContextCompat;

import com.example.masterai.R;

import java.util.List;

public class HintSliderUtil {
    private TextSwitcher textSwitcher;
    private EditText editText;
    private List<String> hintList;
    private long intervalMs;

    private int currentIndex = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            if (hintList != null && !hintList.isEmpty()) {
                currentIndex = (currentIndex + 1) % hintList.size();
                textSwitcher.setText(hintList.get(currentIndex));
            }
            handler.postDelayed(this, intervalMs);
        }
    };

    // Constructor đầy đủ tham số
    public HintSliderUtil(TextSwitcher textSwitcher, EditText editText, List<String> hintList, long intervalMs) {
        this.textSwitcher = textSwitcher;
        this.editText = editText;
        this.hintList = hintList;
        this.intervalMs = intervalMs;

        setupTextSwitcher();
        setupEditTextListener();
    }

    // Constructor mặc định thời gian trượt là 2500ms (3 giây)
    public HintSliderUtil(TextSwitcher textSwitcher, EditText editText, List<String> hintList) {
        this(textSwitcher, editText, hintList, 2000L);
    }

    private void setupTextSwitcher() {
        final Context context = textSwitcher.getContext();

        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(context);
                textView.setTextSize(14f); // Khớp với kích thước chữ của EditText
                // Sử dụng màu xám cho giống hint
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                textView.setGravity(Gravity.TOP);
                return textView;
            }
        });

        // Set file animation
        textSwitcher.setInAnimation(context, R.anim.slide_in_up);
        textSwitcher.setOutAnimation(context, R.anim.slide_out_up);

        if (hintList != null && !hintList.isEmpty()) {
            textSwitcher.setCurrentText(hintList.get(0));
        }
    }

    private void setupEditTextListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // KIỂM TRA TEXT: Nếu có text thì dừng hint, nếu trống thì chạy lại
                if (s == null || s.length() == 0) {
                    textSwitcher.setVisibility(View.VISIBLE);
                    startSliding();
                } else {
                    textSwitcher.setVisibility(View.INVISIBLE);
                    stopSliding();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void startSliding() {
        // Chỉ chạy nếu đang không chạy VÀ EditText đang trống
        if (!isRunning && (editText.getText() == null || editText.getText().length() == 0)) {
            handler.postDelayed(slideRunnable, intervalMs);
            isRunning = true;
        }
    }

    public void stopSliding() {
        if (isRunning) {
            handler.removeCallbacks(slideRunnable);
            isRunning = false;
        }
    }
}
