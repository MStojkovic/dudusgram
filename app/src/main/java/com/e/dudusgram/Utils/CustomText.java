package com.e.dudusgram.Utils;


import android.content.Context;
import android.graphics.Rect;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class CustomText extends android.support.v7.widget.AppCompatEditText {

    private static final int MAX_LINES = 5;

    public CustomText(Context context) {
        super(context);
    }

    public CustomText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {

        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            post(new Runnable() {
                @Override
                public void run() {
                    setSelection(getText().length());
                }
            });
        } else {
            hideKeyboard(getRootView());
        }
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (null != getLayout() && getLayout().getLineCount() > MAX_LINES) {
            getText().delete(getText().length() - 1, getText().length());
        }
    }
}