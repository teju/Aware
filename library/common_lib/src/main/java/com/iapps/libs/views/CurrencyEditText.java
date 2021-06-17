package com.iapps.libs.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;
import com.iapps.libs.helpers.BaseHelper;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyEditText extends AppCompatEditText {

    private String current = "";
    private CurrencyEditText editText = CurrencyEditText.this;

    //properties
    private String Currency = "";
    private String Separator = ",";
    private Boolean Spacing = false;
    private Boolean Delimiter = false;
    private Boolean Decimals = true;

    public CurrencyEditText(Context context) {
        super(context);
        init();
    }

    public CurrencyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurrencyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    if(s.toString().contains(Currency)){
                        setValue(s);
                    }else{
                        editText.setText(current);
                        editText.setSelection(current.length());
                    }

                    editText.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void setValue(CharSequence s){

        if (!s.toString().equals(current)) {
            String formatted = BaseHelper.formatCurrency(s.toString(), Currency, Spacing, Delimiter, Decimals, Separator);
            current = formatted;
            editText.setText(formatted);
            editText.setSelection(formatted.length());
        }
    }

    /*
    *
    */
    public double getCleanDoubleValue() {
        double value = 0.0;
        if (Decimals) {
            value = Double.parseDouble(editText.getText().toString().replaceAll("[$,]", "").replaceAll(Currency, ""));
        } else {
            String cleanString = editText.getText().toString().replaceAll("[$,.]", "").replaceAll(Currency, "").replaceAll("\\s+", "");
            try {
                value = Double.parseDouble(cleanString);
            } catch (NumberFormatException e) {

            }
        }
        return value;
    }

    public int getCleanIntValue() {
        int value = 0;
        if (Decimals) {
            double doubleValue = Double.parseDouble(editText.getText().toString().replaceAll("[$,]", "").replaceAll(Currency, ""));
            value = (int) Math.round(doubleValue);
        } else {
            String cleanString = editText.getText().toString().replaceAll("[$,.]", "").replaceAll(Currency, "").replaceAll("\\s+", "");
            try {
                value = Integer.parseInt(cleanString);
            } catch (NumberFormatException e) {

            }
        }
        return value;
    }

    public void setDecimals(boolean value) {
        this.Decimals = value;
    }

    public void setCurrency(String currencySymbol) {
        this.Currency = currencySymbol;
    }

    public void setSpacing(boolean value) {
        this.Spacing = value;
    }

    public void setDelimiter(boolean value) {
        this.Delimiter = value;
    }

    /**
     * Separator allows a custom symbol to be used as the thousand separator. Default is set as comma (e.g: 20,000)
     * <p>
     * Custom Separator cannot be set when Decimals is set as `true`. Set Decimals as `false` to continue setting up custom separator
     *
     * @value is the custom symbol sent in place of the default comma
     */
    public void setSeparator(String value) {
        this.Separator = value;
    }
}