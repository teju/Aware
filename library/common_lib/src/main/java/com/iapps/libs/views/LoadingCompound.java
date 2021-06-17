package com.iapps.libs.views;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.iapps.common_library.R;
import com.iapps.libs.helpers.BaseHelper;

/**
 * Compound view to be used as a loading screen To show retry button {@link LoadingListener} must be
 * set
 * @author melvin
 * 
 */
public class LoadingCompound
	extends LinearLayout {

	private LinearLayout loadingContainer;
	private LinearLayout retryContainer;

	private TextView tvTitle;
	private TextView tvMessage;
	private TextView tvLoading;
	private Button btnRetry;
	private LoadingListener mStartLoadingListener;
	private String noNetworkMessage;
	private String unknownResponseMessage;
	private String noInternetMessage;
	private ImageView ivNila;
	private ProgressBar progressBarLoadingCompound;

	private int countTotal = 1, countSuccess;
	private int animResId2 = R.anim.fadein;
	private int animResId = R.anim.fadeout;
	private boolean showLoadingRetry = true;

	/**
	 * Listener for retry button onClick Events, and loading is shown
	 * @author melvin
	 * 
	 */
	public interface LoadingListener {
		public void onStartLoading();
	}

	public LoadingCompound(Context context) {
		super(context, null);

	}

	public void setShowLoadingRetry(boolean showLoadingRetry) {
		this.showLoadingRetry = showLoadingRetry;
	}

	public LoadingCompound(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.custom_loading_compound, this, true);

		if (!isInEditMode()) {

			loadingContainer = (LinearLayout) this.findViewById(R.id.loadingContainer);
			retryContainer = (LinearLayout) this.findViewById(R.id.retryContainer);
			tvLoading = (TextView) this.findViewById(R.id.textViewLoadingMessage);
			tvTitle = (TextView) this.findViewById(R.id.textViewLoadingErrorTitle);
			tvMessage = (TextView) this.findViewById(R.id.textViewLoadingErrorMessage);
			btnRetry = (Button) this.findViewById(R.id.buttonRetry);

			ivNila = (ImageView) this.findViewById(R.id.ivNila);
			progressBarLoadingCompound = (ProgressBar) this.findViewById(R.id.progressBarLoadingCompound);

			if (attrs != null) {
				TypedArray a = context.obtainStyledAttributes(attrs,
						R.styleable.iapps);

				// Set the colors from the attributes
				int loadingMessageColor = a.getColor(
						R.styleable.iapps_loadingMessageColor, context
								.getResources().getColor(R.color.DarkGray));
				int errTitleColor = a.getColor(
						R.styleable.iapps_loadingErrorTitleColor, context
								.getResources().getColor(R.color.Black));
				int errMessageColor = a.getColor(
						R.styleable.iapps_loadingErrorMessageColor,
						context.getResources().getColor(R.color.Black));
				tvLoading.setTextColor(loadingMessageColor);
				tvTitle.setTextColor(errTitleColor);
				tvMessage.setTextColor(errMessageColor);

				// Set the text resources from attributes
				String loadingMessage = a.getString(R.styleable.iapps_loadingMessage);
				if (loadingMessage != null) {
					tvLoading.setText(loadingMessage);
				}

				String retryButtonText = a.getString(R.styleable.iapps_loadingRetryButtonText);
				if (retryButtonText != null) {
					btnRetry.setText(retryButtonText);
				}

				boolean useNilaGif = a.getBoolean(R.styleable.iapps_loadingGifNila, false);
				if(useNilaGif){
//					Glide.with(context)
//							.asGif()
//							.load(R.drawable.ssc_nila_dab)
//							.into(ivNila);
//					ivNila.setVisibility(View.VISIBLE);
//					progressBarLoadingCompound.setVisibility(View.GONE);
//					tvLoading.setVisibility(View.GONE);
				}else{
					ivNila.setVisibility(View.GONE);
					progressBarLoadingCompound.setVisibility(View.VISIBLE);
					tvLoading.setVisibility(View.VISIBLE);
				}

				noNetworkMessage = a.getString(R.styleable.iapps_loadingErrorNetworkTitle);
				unknownResponseMessage = a.getString(R.styleable.iapps_loadingErrorUnknownResponseMessage);
				noInternetMessage = a.getString(R.styleable.iapps_loadingErrorNoInternetMessage);

				if (noNetworkMessage == null) {
					noNetworkMessage = getContext().getString(R.string.iapps__network_error);
				}

				if (noInternetMessage == null) {
					noInternetMessage = getContext().getString(R.string.iapps__no_internet);
				}

				if (unknownResponseMessage == null) {
					unknownResponseMessage = getContext().getString(R.string.iapps__unknown_response);
				}

				a.recycle();
			}

			btnRetry.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					BaseHelper.visibleView(loadingContainer);
					BaseHelper.goneView(retryContainer);

					if (mStartLoadingListener != null) {

						if(showLoadingRetry)
						showLoading();

						mStartLoadingListener.onStartLoading();
					}
				}
			});
		}
	}

	/**
	 * Hide the loading compound
	 */
	public void hide() {
		try {
			countSuccess++;

			// Hide ld if successfully load all api(s)
			// If at least 1 api call is fail, don't hide. BUT, you have to handle this on fail.
			if (countTotal <= countSuccess)
				BaseHelper.goneView(this);
		} catch (Exception e){

		}
	}

	public void hideForce() {
		countSuccess = countTotal - 1;
		hide();
	}

	public void setLoadingMessage(String msg){
		if(tvLoading != null){
			tvLoading.setText(msg);
		}
	}

	/**
	 * Hide the loading compound
	 * @param withAnim, true if the hiding should use animation
	 */
	public void hide(boolean withAnim) {
		if (withAnim) {
			try {
				// Check the visibility
				if (this.getVisibility() != View.GONE) {
					Animation a = AnimationUtils.loadAnimation(getContext(), animResId);
					a.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							hide();
						}
					});
					this.startAnimation(a);
				}
				else {
					// Is already gone, do not need to start animation or hide the loading compound
				}
			}
			catch (NotFoundException e) {
				// If animation is not found
				hide();
				e.printStackTrace();
			}
		}
		else {
			hide();
		}
	}

	public void setAnimResId(int animResId) {
		this.animResId = animResId;
	}

	public void setOnStartLoadingListener(LoadingListener l) {
		this.mStartLoadingListener = l;
	}

	public void setNoNetworkMessage(int noNetworkMessage) {
		setNoNetworkMessage(getContext().getString(noNetworkMessage));
	}

	public void setUnknownResponseMessage(int unknownResponseMessage) {
		setUnknownResponseMessage(getContext().getString(unknownResponseMessage));
	}

	public void setNoInternetMessage(int noInternetMessage) {
		setNoInternetMessage(getContext().getString(noInternetMessage));
	}

	public void setNoNetworkMessage(String noNetworkMessage) {
		this.noNetworkMessage = noNetworkMessage;
	}

	public void setUnknownResponseMessage(String unknownResponseMessage) {
		this.unknownResponseMessage = unknownResponseMessage;
	}

	public void setNoInternetMessage(String noInternetMessage) {
		this.noInternetMessage = noInternetMessage;
	}

	public void setRetryButtonTitle(String title) {
		btnRetry.setText(title);
	}

	public void setRetryButtonTitle(int titleResId) {
		btnRetry.setText(titleResId);
	}

	public void addCountSuccess() {
		this.countSuccess++;
	}

	public int getCountSuccess() {
		return this.countSuccess;
	}

	public void setCountTotal(int totalCount) {
		this.countTotal = totalCount;
	}

	public int getCountTotal() {
		return this.countTotal;
	}

	public void showLoading() {
		BaseHelper.goneView(retryContainer);
		BaseHelper.visibleView(loadingContainer);
	}

	public void showLoadingV2() {
		BaseHelper.goneView(retryContainer);
		BaseHelper.visibleView(loadingContainer);
		this.setVisibility(View.VISIBLE);
		LoadingCompound.this.setVisibility(View.VISIBLE);
	}

	public void showLoadingV3() {
		this.clearAnimation();
		this.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.DarkGray));

		try {
			// Check the visibility
			if (this.getVisibility() != View.GONE) {
				Animation a = AnimationUtils.loadAnimation(getContext(), animResId2);
				a.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						showLoadingV2();
					}
				});
				this.startAnimation(a);
			}
			else {
				// Is already gone, do not need to start animation or hide the loading compound
			}
		}
		catch (Exception e) {
			showLoadingV2();
		}

	}

	public void showError(String title, String message) {
		this.clearAnimation();
		BaseHelper.visibleView(this);
		BaseHelper.visibleView(retryContainer);
		if (mStartLoadingListener != null) {
			BaseHelper.visibleView(btnRetry);
		}
		else {
			BaseHelper.goneView(btnRetry);
		}
		BaseHelper.goneView(loadingContainer);
		if (title != null) {
			tvTitle.setText(title);
			BaseHelper.visibleView(tvTitle);

		}

		try {
			if (message != null) {
				tvMessage.setText(message);
				BaseHelper.visibleView(tvMessage);
			}
		}catch (Exception e){}

	}

	public void showInternetError() {
		this.showError(
				noNetworkMessage,
				noInternetMessage);

	}

	public void showUnknownResponse() {
		this.showError(
				noNetworkMessage,
				unknownResponseMessage);

	}

	public void setRetryEnabled(boolean b) {
		if (b) {
			BaseHelper.visibleView(btnRetry);
		}
		else {
			BaseHelper.goneView(btnRetry);
		}

	}

}
