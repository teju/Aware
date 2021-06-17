package com.iapps.libs.helpers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.res.ResourcesCompat;
import com.iapps.common_library.R;

/**
 * sub class of {@link AutoCompleteTextView} that includes a clear (dismiss / close) button with
 * a OnClearListener to handle the event of clicking the button
 * @author Pasca Maulana
 *
 */
public class ClearableAutoCompleteTextViewAsDropDown extends AppCompatAutoCompleteTextView {

	boolean isClearBtnHide = true;
	private int color = R.color.themeBlue;
	private boolean enoughToFilter = true;
	private boolean canClear = false;

	public Drawable imgClearButton = ResourcesCompat.getDrawable(getResources(),
			R.drawable.ic_clear, null);

	public Drawable imgDot = ResourcesCompat.getDrawable(getResources(),
			R.drawable.icon_dot_clear_grey, null);

	private boolean useStyleDotAndX = false;

	public void useStyleDotAndX(){
		useStyleDotAndX = true;
		init();
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setCanClear(boolean canClear) {
		this.canClear = canClear;
	}

	public void setEnoughToFilter(boolean enoughToFilter) {
		this.enoughToFilter = enoughToFilter;
	}

	private OnTextListener onTextListener;
	private OnTextListener2 onTextListener2;

	public void setOnTextListener(OnTextListener onTextListener) {
		this.onTextListener = onTextListener;
	}

	public void setOnTextListener2(OnTextListener2 onTextListener2) {
		this.onTextListener2 = onTextListener2;
	}

	public interface OnTextListener {
		void ontextChange(String s);
	}

	public interface OnTextListener2 {
		void ontextChange(View view, String s);
	}

	// if not set otherwise, the default clear listener clears the text in the
	// text view
	private OnClearListener defaultClearListener = new OnClearListener() {

		@Override
		public void onClear() {
			ClearableAutoCompleteTextViewAsDropDown et = ClearableAutoCompleteTextViewAsDropDown.this;

			if(canClear)
			et.setText("");
		}
	};

	@Override
	public boolean enoughToFilter() {
		return enoughToFilter;
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction,
								  Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		try {
			if (focused) {
                performFiltering(getText(), 0);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void forceFilterEmpty(){
		performFiltering("", 0);
	}

	private OnClearListener onClearListener = defaultClearListener;

	// The image we defined for the spinner state
	public Drawable imgSpinner = ResourcesCompat.getDrawable(getResources(),
			R.drawable.chevron_down, null);


	public void setImgSpinner(int imgSpinner) {
		this.imgSpinner = ResourcesCompat.getDrawable(getResources(), imgSpinner, null);
	}

	public interface OnClearListener {
		void onClear();
	}

	/* Required methods, not used in this implementation */
	public ClearableAutoCompleteTextViewAsDropDown(Context context) {
		super(context);
		init();
	}

	/* Required methods, not used in this implementation */
	public ClearableAutoCompleteTextViewAsDropDown(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/* Required methods, not used in this implementation */
	public ClearableAutoCompleteTextViewAsDropDown(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {

		clearState();

		if(useStyleDotAndX)
			this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(color, imgDot), null);
		else
			showSpinner();

		// if the clear button is pressed, fire up the handler. Otherwise do nothing
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				checkOriginalOntouch(v, event);
				return false;
			}
		});

		this.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0) {
					clearState();
					showClearButton();


					if(onTextListener!=null){
						onTextListener.ontextChange(s.toString());
					}

					if(onTextListener2!=null){
						onTextListener2.ontextChange(ClearableAutoCompleteTextViewAsDropDown.this, s.toString());
					}

				} else {
					clearState();
					showSpinner();
				}

				try {
					if(useStyleDotAndX){
						if (s.length() > 0) {
							clearState();
							showSpinner();
						} else {
							clearState();
							ClearableAutoCompleteTextViewAsDropDown.this.setCompoundDrawablesWithIntrinsicBounds(null,
									null, makeCustomColor(color, imgDot), null);
						}
					}
				} catch (Exception e) {}


			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});


		this.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {


				try {
					if(b){

						if(useStyleDotAndX){
							setImgClearButton(imgClearButton);
						}


					}else{

						if(useStyleDotAndX){
							setImgClearButton(imgDot);
						}
					}
				} catch (Exception e) {}
			}
		});


	}

	public void checkOriginalOntouch(View v, MotionEvent event){
		ClearableAutoCompleteTextViewAsDropDown et = ClearableAutoCompleteTextViewAsDropDown.this;

		if (et.getCompoundDrawables()[2] == null)
			return;

		if (event.getAction() != MotionEvent.ACTION_UP) {
			onClearListener.onClear();
			return;
		}

		if (event.getX() > et.getWidth() - et.getPaddingRight() - imgSpinner.getIntrinsicWidth()) {
			if(canClear) {
				if (!isClearBtnHide)
					onClearListener.onClear();
			}else{
				onClearListener.onClear();
			}
		}
	}

	public void setImgClearButton(Drawable imgClearButton) {
		this.imgSpinner = imgClearButton;
	}

	public void setImgClearButton(Drawable imgClearButton, int color) {
		this.imgSpinner = imgClearButton;
		this.color = color;
	}

	public void setOnClearListener(final OnClearListener clearListener) {
		this.onClearListener = clearListener;
	}

	public void clearState() {
		isClearBtnHide = true;
		this.setCompoundDrawables(null, null, null, null);
	}

	public void showClearButton() {
		isClearBtnHide = false;
		this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(color, imgSpinner), null);
	}

	public void showSpinner() {
		isClearBtnHide = false;
		this.setCompoundDrawablesWithIntrinsicBounds(null, null, makeCustomColor(color, imgSpinner), null);
	}

	public static Drawable makeCustomColor(int color, Drawable drble){
		drble.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		return drble;
	}


	/**
	 * An interface which a client of this Spinner could use to receive
	 * open/closed events for this Spinner.
	 */
	public interface OnSpinnerEventsListener {

		/**
		 * Callback triggered when the spinner was opened.
		 */
		void onSpinnerOpened(ClearableAutoCompleteTextViewAsDropDown spinner);

		/**
		 * Callback triggered when the spinner was closed.
		 */
		void onSpinnerClosed(ClearableAutoCompleteTextViewAsDropDown spinner);

	}

	private OnSpinnerEventsListener mListener;
	private boolean mOpenInitiated = false;

	// implement the Spinner constructors that you need

	@Override
	public boolean performClick() {
		// register that the Spinner was opened so we have a status
		// indicator for when the container holding this Spinner may lose focus
		mOpenInitiated = true;
		if (mListener != null) {
			mListener.onSpinnerOpened(this);
		}
		return super.performClick();
	}

	/**
	 * Register the listener which will listen for events.
	 */
	public void setSpinnerEventsListener(
			OnSpinnerEventsListener onSpinnerEventsListener) {
		mListener = onSpinnerEventsListener;
	}

	/**
	 * Propagate the closed Spinner event to the listener from outside if needed.
	 */
	public void performClosedEvent() {
		mOpenInitiated = false;
		if (mListener != null) {
			mListener.onSpinnerClosed(this);
		}
	}

	/**
	 * A boolean flag indicating that the Spinner triggered an open event.
	 *
	 * @return true for opened Spinner
	 */
	public boolean hasBeenOpened() {
		return mOpenInitiated;
	}

	public void onWindowFocusChanged (boolean hasFocus) {
		if (hasBeenOpened() && hasFocus) {
			performClosedEvent();
		}
	}

}