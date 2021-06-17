package com.iapps.libs.helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.res.ResourcesCompat;
import com.iapps.common_library.R;
import java.util.ArrayList;

/**
 * sub class of {@link EditText} that includes a clear (dismiss / close) button with
 * a OnClearListener to handle the event of clicking the button
 *
 * @author Pasca Maulana
 */
public class ClearableEditText extends AppCompatEditText {

    boolean isClearBtnHide = true;
    private int color = Color.GRAY;
    boolean clearFocus = false;

    private Context context;

    boolean isTicked = false;
    public boolean isTicked() {
        return isTicked;
    }

    boolean isMandatoryLengthChar = false;
    ArrayList<String> mandatoryLengthChar = new ArrayList<String>();
    public String getMandatoryLengthChar() {
        return android.text.TextUtils.join(",", mandatoryLengthChar);
    }

    private boolean isHintWhenTxtInputALsoHaveHint = false;
    private String HintWhenTxtInputALsoHaveHint;

    public void setHintWhenTxtInputALsoHaveHint(final String hint){
        isHintWhenTxtInputALsoHaveHint = true;
        HintWhenTxtInputALsoHaveHint = hint;
    }

    private boolean useStyleDotAndX = false;
    private boolean useStyleDot = true;

    public void setUseStyleDot(boolean useStyleDot) {
        this.useStyleDot = useStyleDot;
    }

    private boolean useRedLineIfError = false, useRedLineIfErrorToggle = false;
    private ColorFilter defaultColorFilter1, defaultColorFilter2;
    private int defaultColorText;

    public void useStyleDotAndX(){
        useStyleDotAndX = true;
        init();
    }

    public void useRedLineIfError(boolean useRedLineIfErrorToggle){
        this.useRedLineIfErrorToggle = useRedLineIfErrorToggle;
        if(useRedLineIfError) {
            if (useRedLineIfErrorToggle) {
                this.getBackground().mutate().clearColorFilter();
                this.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                this.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else {
                if (defaultColorFilter1 != null) {
                    this.getBackground().mutate().clearColorFilter();
                    this.getBackground().mutate().setColorFilter(defaultColorFilter1);
                    this.setTextColor(defaultColorText);
                } else {
                    this.getBackground().mutate().clearColorFilter();
                    this.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                    this.setTextColor(getResources().getColor(android.R.color.black));
                }

            }
        }
    }

    public void initRedLineColorIfError(){
        this.useRedLineIfError = true;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defaultColorFilter1 = this.getBackground().mutate().getColorFilter();
            }
        } catch (Exception e) {}

//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                defaultColorFilter2 = this.textInputLayout.getBackground().mutate().getColorFilter();
//            }
//        } catch (Exception e) {}

        defaultColorText = this.getTextColors().getDefaultColor();

    }

    public void setMandatoryLengthChar(ArrayList<String> mandatoryLengthChar){
        isMandatoryLengthChar = true;
        this.mandatoryLengthChar = mandatoryLengthChar;
        try {
            this.setFilters(new InputFilter[] {new InputFilter.
                    LengthFilter(Integer.parseInt(mandatoryLengthChar.
                    get(mandatoryLengthChar.size()-1)))});
        } catch (Exception e) {}
    }

    public void disableMandatoryLengthChar(){
        isMandatoryLengthChar = false;
        try {
            this.setFilters(new InputFilter[] {new InputFilter.
                    LengthFilter(500)});
        } catch (Exception e) {}
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(color, imgClearButton), null);
        isTicked = false;
    }

    public void setClearFocus(boolean clearFocus) {
        this.clearFocus = clearFocus;
    }

    // if not set otherwise, the default clear listener clears the text in the
    // text view
    private OnClearListener defaultClearListener = new OnClearListener() {

        @Override
        public void onClear() {
            ClearableEditText et = ClearableEditText.this;
            et.setText("");
        }
    };

    private OnClearListener onClearListener = defaultClearListener;

    private OnFocusChangeListener onFocusChangeListener;

    public void setOnFocusChangeListener2(OnFocusChangeListener onFocusChangeListener) {
        this.onFocusChangeListener = onFocusChangeListener;
    }

    private OnClickListener onClickListener;

    public void setOnTouchListener2(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    // The image we defined for the clear button
    public Drawable imgClearButton = ResourcesCompat.getDrawable(getResources(),
            R.drawable.ic_clear, null);
    public Drawable background = ResourcesCompat.getDrawable(getResources(),
            R.drawable.edit_text_bg, null);

    public Drawable imgClearButtonX = ResourcesCompat.getDrawable(getResources(),
            R.drawable.ic_close_black_24dp, null);

    public Drawable imgClearButtonX2 = ResourcesCompat.getDrawable(getResources(),
            R.drawable.ic_clear, null);


    public Drawable imgTickedButton = ResourcesCompat.getDrawable(getResources(),
            R.drawable.ic_check_black_24dp, null);


    public Drawable imgDot = ResourcesCompat.getDrawable(getResources(),
            R.drawable.icon_dot_clear_grey, null);

    public interface OnClearListener {
        void onClear();
    }

    public interface OnTextListener {
        void ontextChange(String s);
    }

    private OnTextListener2 onTextListener2;
    public void setOnTextListener2(OnTextListener2 onTextListener2) {
        this.onTextListener2 = onTextListener2;
    }

    public interface OnTextListener2 {
        void ontextChange(View view, String s);
    }


    public interface AfterTextListener {
        void afterTextChange(String s);
    }

    private OnTextListener onTextListener;
    private AfterTextListener afterTextListener;

    public void setAfterTextListener(AfterTextListener afterTextListener) {
        this.afterTextListener = afterTextListener;
    }

    public void setOnTextListener(OnTextListener onTextListener) {
        this.onTextListener = onTextListener;
    }

    /* Required methods, not used in this implementation */
    public ClearableEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    /* Required methods, not used in this implementation */
    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    /* Required methods, not used in this implementation */
    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public boolean stopForWhileTextListener = false;

    void init() {

        if(useStyleDotAndX || useStyleDot)
            this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(Color.parseColor("#CCAB60"), imgDot), null);
        else
            hideClearButton();


        // if the clear button is pressed, fire up the handler. Otherwise do nothing
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (onClickListener != null)
                        onClickListener.onClick(v);

                    checkOriginalOnTouch(v, event);
                }

                return false;
            }
        });

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!stopForWhileTextListener)
                checkTextChange(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!stopForWhileTextListener)
                    if(afterTextListener!=null)
                    afterTextListener.afterTextChange(s.toString());


                try {
                    if(useStyleDotAndX){
                        if (s.length() > 0) {
                            hideClearButton();
                            showClearButton();
                        } else {
                            hideClearButton();
                            ClearableEditText.this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(color, imgDot), null);
                        }
                    }
                } catch (Exception e) {}

            }
        });


        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if(onFocusChangeListener != null){
                    onFocusChangeListener.onFocusChange(view, b);
                }

                try {
                    if(b){

                        if(useStyleDotAndX){
                            setImgClearButton(imgClearButton);
                        }

                        if(isHintWhenTxtInputALsoHaveHint) {
                            ClearableEditText.this.setHint(HintWhenTxtInputALsoHaveHint);
                            showSoftKeyBoardEditText(context, ClearableEditText.this);
                        }
                    }else{
                        if(isHintWhenTxtInputALsoHaveHint) {
                            ClearableEditText.this.setHint("");
                        }

                        if(useStyleDotAndX){
                            setImgClearButton(imgDot);
                        }
                    }

                } catch (Exception e) {}
                view.setBackground(background);

            }
        });
        this.setBackground(background);
    }


    public static void showSoftKeyBoardEditText(Context ctx, EditText et){
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et,0);
    }

    public void checkTextChange(String s){
        try {
            if (s.length() > 0) {
                if(useStyleDotAndX) {
                    hideClearButton();
                    showClearButton();
                }
            } else {
                hideClearButton();
            }

            if(onTextListener!=null){
                onTextListener.ontextChange(s.toString());
            }

            if(onTextListener2!=null){
                onTextListener2.ontextChange(ClearableEditText.this, s.toString());
            }
        } catch (Exception e) {}
    }

    public void checkOriginalOnTouch(View v, MotionEvent event) {
        ClearableEditText et = ClearableEditText.this;

        if (et.getCompoundDrawables()[2] == null)
            return;

        if (event.getX() > et.getWidth() - et.getPaddingRight() - imgClearButton.getIntrinsicWidth()) {
            if (!isClearBtnHide && !isTicked)
                onClearListener.onClear();
        }
    }

    public boolean isClearTriggered(View v, MotionEvent event) {
        ClearableEditText et = ClearableEditText.this;

        if (et.getCompoundDrawables()[2] == null)
            return false;

        if (event.getAction() != MotionEvent.ACTION_UP)
            return false;

        if (event.getX() > et.getWidth() - et.getPaddingRight() - imgClearButton.getIntrinsicWidth()) {
            if (!isClearBtnHide && !isTicked)
                return true;
        }
        return false;
    }

    public void setImgClearButton(Drawable imgClearButton) {
        this.imgClearButton = imgClearButton;
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(color, imgClearButton), null);
    }

    public void setImgClearButton(Drawable imgClearButton, int color) {
        this.imgClearButton = imgClearButton;
        this.color = color;
    }

    public void setOnClearListener(final OnClearListener clearListener) {
        this.onClearListener = clearListener;
    }

    public void hideClearButton() {
        isClearBtnHide = true;
        this.setCompoundDrawables(null, null, null, null);
    }

    public void showClearButton() {
        isClearBtnHide = false;
        if(isMandatoryLengthChar) {
            try {

                boolean isFound = false;

                for(String x : mandatoryLengthChar){
                    try {
                        if(this.length() == Integer.parseInt(x.trim())){
                            isFound = true;
                        }
                    } catch (Exception e) {}
                }

                if(isFound){
                    isTicked = true;
                    this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(Color.parseColor("#7ED321"), imgTickedButton), null);
                }else{
                    isTicked = false;
                    this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(Color.RED, imgClearButtonX), null);
                }
            } catch (Exception e) {}
        }else {
                if(useStyleDotAndX)
                    this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(color, imgClearButtonX2), null);
                else
                this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(color, imgClearButton), null);
        }
    }

    public Drawable makeCustomColor(int color, Drawable drble) {
        drble.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return drble;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (clearFocus) {
                clearFocus();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

}