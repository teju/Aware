package com.iapps.libs.helpers;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iapps.common_library.R;
import com.iapps.libs.objects.BottomSheetMediaSelectionListener;
import com.iapps.libs.objects.Response;
import com.iapps.libs.views.LoadingCompound;
import com.iapps.logs.com.pascalabs.util.log.activity.ActivityPascaLog;
import com.iapps.logs.com.pascalabs.util.log.helper.Helper;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.paperdb.Paper;

import static com.iapps.libs.helpers.BaseUIHelper.calculateInSampleSize;


public class BaseHelper {

	private static final String MIN_AGO = "min ago.";
	private static final String MINS_AGO = "mins ago.";
	private static final String HOUR_AGO = "hour ago.";
	private static final String HOURS_AGO = "hours ago.";
	private static final String DAY_AGO = "day ago.";
	private static final String DAYS_AGO = "days ago.";

	private static final String MIN_LEFT = "min left.";
	private static final String MINS_LEFT = "mins left.";
	private static final String HOUR_LEFT = "hour left.";
	private static final String HOURS_LEFT = "hours left.";
	private static final String DAY_TO_GO = "day to go.";
	private static final String DAYS_TO_GO = "days to go.";
	public static final int REQUEST_EMAIL_CODE = 7221;
	public static final int REQUEST_GET_IMAGE_CODE = 9882;
	public static final int REQUEST_LOCATION_CODE = 9228;
	public static final int REQUEST_MULTIPLE_PERMISSIONS = 1001;

	/**
	 * Check if a string is empty (length == 0)
	 *
	 * @param string , to be checked
	 * @return true if empty
	 */
	public static boolean isEmpty(String string) {
		if (string == null || string.trim().length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Change the state of a view to View.GONE
	 *
	 * @param v , view to be updated
	 */
	public static void goneView(View v) {
		if (v != null) {
			v.setVisibility(View.GONE);
		}

	}

	public static void selectImageSource(Activity activity, final BottomSheetMediaSelectionListener bottomSheetMediaSelectionListener) {
		View modalBottomSheetView = activity.getLayoutInflater().inflate(R.layout.modal_bottomsheet_media_selection, null);

		final BottomSheetDialog bottomSheetModalDialog = new BottomSheetDialog(activity);
		bottomSheetModalDialog.setContentView(modalBottomSheetView);
		bottomSheetModalDialog.setCanceledOnTouchOutside(true);
		bottomSheetModalDialog.setCancelable(true);

		AppCompatButton btnCancel = (AppCompatButton) modalBottomSheetView.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bottomSheetModalDialog.dismiss();
			}
		});

		AppCompatButton btnCamera = (AppCompatButton) modalBottomSheetView.findViewById(R.id.btnCamera);
		btnCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bottomSheetModalDialog.dismiss();
				bottomSheetMediaSelectionListener.onCameraSelection();
			}
		});

		AppCompatButton btnGallery = (AppCompatButton) modalBottomSheetView.findViewById(R.id.btnGallery);
		btnGallery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bottomSheetModalDialog.dismiss();
				bottomSheetMediaSelectionListener.onMediaStorageSelection();
			}
		});

		bottomSheetModalDialog.show();

	}


	/**
	 * Change the state of a view to View.VISIBLE
	 *
	 * @param v , view to be updated
	 */
	public static void visibleView(View v) {
		if (v != null) {
			v.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Change the state of a view to View.INVISIBLE
	 *
	 * @param v , view to be updated
	 */
	public static void invisibleView(View v) {
		if (v != null) {
			v.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Send email
	 *
	 * @param activity
	 * @param emailAddresses
	 * @param subject
	 * @param body
	 * @param uri            , image or attachment
	 */
	public static void sendEmail(
			Activity activity, String[] emailAddresses, String subject, String body, Uri uri) {
		Intent email = new Intent(Intent.ACTION_SEND);
		if (emailAddresses != null) {
			email.putExtra(Intent.EXTRA_EMAIL, emailAddresses);
		}
		email.putExtra(Intent.EXTRA_SUBJECT, subject);
		if (body == null) {
			body = "";
		}

		if (uri != null) {
			email.putExtra(Intent.EXTRA_STREAM, uri);
		}

		email.putExtra(Intent.EXTRA_TEXT, body);

		email.setType("message/rfc822");

		activity.startActivityForResult(Intent.createChooser(email, "Choose an Email client :"),
				REQUEST_EMAIL_CODE);
	}

	/**
	 * Open activity to send an SMS
	 *
	 * @param activity to start the sms activity from
	 * @param smsBody
	 */
	public static void sendSMS(Activity activity, String smsBody) {
		sendSMS(activity, smsBody, null);
	}

	/**
	 * Open activity to send an SMS, specifying the phone number
	 *
	 * @param activity
	 * @param smsBody
	 * @param phoneNumber
	 */
	public static void sendSMS(Activity activity, String smsBody, String phoneNumber) {

		try {

			Intent sendIntent = new Intent(Intent.ACTION_VIEW);
			if (phoneNumber != null && phoneNumber.trim().length() > 0) {
				sendIntent.putExtra("address", phoneNumber);
			}
			if (smsBody != null) {
				sendIntent.putExtra("sms_body", smsBody);
			}
			sendIntent.setType("vnd.android-dir/mms-sms");

			activity.startActivity(sendIntent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(activity, "Unable to send SMS", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Show dialog alert
	 *
	 * @param context
	 * @param title
	 * @param message
	 * @return {@link AlertDialog}
	 */
	public static AlertDialog showAlert(
			Context context, String title, String message,
			DialogInterface.OnClickListener listener) {
		try {
			if (listener == null) {
				listener = new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						dialog = null;
					}
				};
			}

			AlertDialog d = new Builder(context).setMessage(message).setTitle(title)
					.setCancelable(true).setNeutralButton(android.R.string.ok, listener).show();
			return d;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static AlertDialog showAlertNotCancelAble(
			Context context, String title, String message,
			DialogInterface.OnClickListener listener) {
		if (listener == null) {
			listener = new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			};
		}

		AlertDialog d = new AlertDialog.Builder(context).setMessage(message).setTitle(title)
				.setCancelable(false).setNeutralButton(android.R.string.ok, listener).show();
		return d;
	}

	public static AlertDialog showAlert(Context context, String title, String message) {
		return showAlert(context, title, message, null);
	}

	public static AlertDialog showAlert(Context context, String message) {
		return showAlert(context, null, message, null);
	}

	public static AlertDialog showAlert(Context context, int titleResId, int messageResId) {
		String title = null;
		try {
			title = context.getString(titleResId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String message = null;
		try {
			message = context.getString(messageResId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return showAlert(context, title, message, null);

	}

	public static AlertDialog showAlert(Context context, int messageResId) {

		String message = null;
		try {
			message = context.getString(messageResId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return showAlert(context, null, message, null);

	}

	public static AlertDialog.Builder buildAlert(Context context, String title, String message) {
		Builder dlg = new AlertDialog.Builder(context).setMessage(message).setTitle(title)
				.setCancelable(true);
		return dlg;
	}

	public static void showInternetError(Context context) {
		showAlert(context, context.getResources().getString(R.string.iapps__network_error), context
				.getResources().getString(R.string.iapps__no_internet), null);
	}

	public static void showUnknownResponseError(Context context) {
		showAlert(context, context.getResources().getString(R.string.iapps__network_error), context
				.getResources().getString(R.string.iapps__unknown_response), null);
	}

	/**
	 * Handle response object and returns {@link JSONObject}
	 *
	 * @param response , response object to be handled
	 * @param loading  , loading compound to show the error message/ to hide
	 * @return valid {@link JSONObject}
	 */
	public static JSONObject handleResponse(Response response, LoadingCompound loading) {
		return handleResponse(response, loading, false, null);
	}

	/**
	 * Handle response object and returns {@link JSONObject}
	 *
	 * @param response            , response object to be handled
	 * @param shouldDisplayDialog , true if should display error dialog popup
	 * @param context             , context being used
	 * @return valid {@link JSONObject}
	 */
	public static JSONObject handleResponse(
			Response response, boolean shouldDisplayDialog, Context context) {
		return handleResponse(response, null, shouldDisplayDialog, context);
	}

	private static JSONObject handleResponse(
			Response response, LoadingCompound loading, boolean shouldDisplayDialog,
			Context context) {
		if (response != null) {
			JSONObject json = response.getContent();
			// changes
			if (response.getStatusCode() == BaseConstants.STATUS_BAD_REQUEST || response.getStatusCode() == BaseConstants.STATUS_NOT_FOUND) {
				try {
					if (json.getString("status_code").equals("3057") || json.getString("status_code").equals("3066") || json.getInt("status_code") == 3057 || json.getInt("status_code") == 3066 || json.getInt("status_code") == 3425) {
						return json;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			//
			if (response.getStatusCode() == BaseConstants.STATUS_SUCCESS) {
				return json;
			} else if (response.getStatusCode() == BaseConstants.STATUS_TIMEOUT) {
				if (loading != null) {
					loading.showError(null, loading.getContext().getString(R.string.iapps__conn_timeout));
				} else if (shouldDisplayDialog && context != null) {
					BaseHelper.showAlert(context, null,
							context.getString(R.string.iapps__conn_timeout));
				}
			} else if (response.getStatusCode() == BaseConstants.STATUS_NO_CONNECTION) {
				if (loading != null) {
					loading.showError(null, loading.getContext().getString(R.string.iapps__conn_fail));
				} else if (shouldDisplayDialog && context != null) {
					BaseHelper.showAlert(context, null,
							context.getString(R.string.iapps__conn_fail));
				}
			} else {
				try {
					String message = json.getString(BaseKeys.MESSAGE);
					if (loading != null) {

						loading.showError(null, message);
					} else if (shouldDisplayDialog && context != null) {
						BaseHelper.showAlert(context, null, message, null);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					if (loading != null) {
						loading.showUnknownResponse();
					} else if (shouldDisplayDialog && context != null) {
						BaseHelper.showUnknownResponseError(context);
					}
				}

			}
		} else {
			if (loading != null) {
				loading.showInternetError();
			} else if (shouldDisplayDialog && context != null) {
				BaseHelper.showInternetError(context);
			}
		}
		return null;
	}

	/**
	 * Show required popup message
	 *
	 * @param context
	 * @param field
	 */
	public static void showRequired(Context context, String field) {
		showAlert(context, context.getResources().getString(R.string.iapps__required_field), field,
				null);
	}

	public static void showRequired(Context context, int resId) {
		try {
			String f = context.getString(resId);
			showRequired(context, f);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isSameDay(DateTime first, DateTime second) {
		return DateTimeComparator.getDateOnlyInstance().compare(first, second) == 0 ? true : false;
	}

	public static boolean isEmpty(EditText edt) {
		return isEmpty(edt.getText().toString());
	}

	/**
	 * Whether the target equals 'Y'
	 *
	 * @param target
	 * @return
	 */
	public static boolean isYes(String target) {
		if (target == null) {
			return false;
		}

		if (target.equals(BaseConstants.YES)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Show confirm dialog popup
	 *
	 * @param context
	 * @param title
	 * @param message
	 * @param l
	 */
	public static AlertDialog.Builder confirm(
			Context context, String title, String message, final ConfirmListener l,
			final CancelListener c) {
		AlertDialog.Builder b = BaseHelper.buildAlert(context, title, message);
		b.setCancelable(false);
		if (c != null)
			b.setNegativeButton(context.getResources().getString(R.string.iapps__cancel),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							c.onNo();
						}
					});
		else b.setNegativeButton(context.getResources().getString(R.string.iapps__no), null);

		b.setPositiveButton(context.getResources().getString(R.string.iapps__yes),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						l.onYes();
					}
				});

		b.show();

		return b;
	}

	public static AlertDialog.Builder confirm(
			Context context, String title, String message, final ConfirmListener l) {
		return confirm(context, title, message, l, null);
	}

	/**
	 * Show confirm dialog popup
	 *
	 * @param context
	 * @param titleResId
	 * @param messageResId
	 * @param l
	 */
	public static AlertDialog.Builder confirm(
			Context context, int titleResId, int messageResId, final ConfirmListener l) {
		String title = null;
		try {
			title = context.getString(titleResId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String message = null;
		try {
			message = context.getString(messageResId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return confirm(context, title, message, l, null);
	}

	/**
	 * Show confirm dialog
	 *
	 * @param context
	 * @param messageResId
	 * @param l
	 */
	public static AlertDialog.Builder confirm(
			Context context, int messageResId, final ConfirmListener l) {

		String message = null;
		try {
			message = context.getString(messageResId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return confirm(context, null, message, l, null);
	}

	public static interface ConfirmListener {

		public void onYes();
	}

	public static interface CancelListener {

		public void onNo();
	}

	/**
	 * Show a prompt dialog to the user
	 *
	 * @param context
	 * @param titleResId
	 * @param positiveButtonResId
	 * @param negativeButtonResId
	 * @param l
	 */
	public static void prompt(
			Context context, int titleResId, int positiveButtonResId, int negativeButtonResId,
			final PromptListener l) {
		prompt(context, titleResId, positiveButtonResId, negativeButtonResId,
				InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, l);
	}

	/**
	 * Show a prompt dialog to the user with custom input type
	 *
	 * @param context
	 * @param titleResId
	 * @param positiveButtonResId
	 * @param negativeButtonResId
	 * @param inputType
	 * @param l
	 */
	public static void prompt(
			Context context, int titleResId, int positiveButtonResId, int negativeButtonResId,
			int inputType, final PromptListener l) {
		String title = null;

		try {
			title = context.getString(titleResId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String positiveButton = null;
		try {
			positiveButton = context.getString(positiveButtonResId);
		} catch (Exception e) {
			e.printStackTrace();
			positiveButton = context.getString(R.string.iapps__done);
		}

		String negativeButton = null;
		try {
			negativeButton = context.getString(negativeButtonResId);
		} catch (Exception e) {
			e.printStackTrace();
			negativeButton = context.getString(R.string.iapps__cancel);
		}

		prompt(context, title, positiveButton, negativeButton, inputType, l);
	}

	public static void prompt(
			Context context, String title, String positiveButton, String negativeButton,
			int inputType, final PromptListener l) {
		final EditText input = new EditText(context);
		input.setInputType(inputType);
		input.setSingleLine();

		new AlertDialog.Builder(context).setTitle(title).setView(input)
				.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						String mInput = input.getText().toString().trim();
						if (l != null) {
							l.onYes(mInput);
						}
					}
				}).setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();
	}

	public static interface PromptListener {

		public void onYes(String input);
	}

	public static String formatDate(Date date, String format) {
		SimpleDateFormat postFormater = new SimpleDateFormat(format, Locale.ENGLISH);

		String newDateStr = postFormater.format(date);
		return newDateStr;
	}

	public static String formatDateTime(DateTime date, String format) {
		return DateTimeFormat.forPattern(format).print(date);
	}

	public static String formatDateTime(String dateTime, String formatFrom, String formatTo) {
		return DateTime.parse(dateTime, DateTimeFormat.forPattern(formatFrom)).toString(formatTo);
	}

	public static String formatISO8601(DateTime date) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis();
		return fmt.print(date);
	}

	public static Date parseDate(String sqlDate, String format) {
		// 2012-12-24 02:01:57
		SimpleDateFormat curFormater = new SimpleDateFormat(format, Locale.ENGLISH);
		Date dateObj = null;
		try {
			dateObj = curFormater.parse(sqlDate);
			return dateObj;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String parseDate(Date sqlDate, String format) {
		// 2012-12-24 02:01:57
		SimpleDateFormat curFormater = new SimpleDateFormat(format, Locale.ENGLISH);
		String dateObj = null;
		try {
			dateObj = curFormater.format(sqlDate);
			return dateObj;
		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}

	}

	public static String parseDate(int date, int month, int year, String format) {
		CharSequence strDate = null;

		// 2012-12-24 02:01:57
		Time chosenDate = new Time();
		chosenDate.set(date, month, year);
		long dtDob = chosenDate.toMillis(true);

		strDate = DateFormat.format(format, dtDob);

		return strDate.toString();
	}

	public static void pickCSV(int requestCode, Activity activity) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(BaseConstants.MIME_CSV);
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		activity.startActivityForResult(intent, requestCode);
	}

	public static DateTime parseDateTime(String dateString, String format) {
		return DateTimeFormat.forPattern(format).parseDateTime(dateString);
	}

	public static DateTime parseISO8601(String dateString) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis();
		DateTime dt = null;
		try {
			dt = fmt.parseDateTime(dateString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dt;
	}

	public static String formatDouble(double d) {
		DecimalFormat formatter = new DecimalFormat("#,###.##");
		d = Math.round(d);
		return formatter.format(d);
	}

	public static String trim(TextView edt) {
		return edt.getText().toString().trim();
	}

	/**
	 * use calcTimeDiff(DateTime target, Context context)
	 *
	 * @param target
	 * @return
	 */
	@Deprecated
	public static String calcTimeDiff(DateTime target) {
		return calcTimeDiff(target.toDate());
	}

	/**
	 * Use calcTimeDiff(Date, Context)
	 *
	 * @param target
	 * @return
	 */
	@Deprecated
	public static String calcTimeDiff(Date target) {
		if (target == null) {
			return null;
		}
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();

		calendar1.setTime(target);

		long milsecs1 = calendar1.getTimeInMillis();
		long milsecs2 = calendar2.getTimeInMillis();

		long diff = milsecs2 - milsecs1;
		long dminutes = diff / (60 * 1000);
		long dhours = diff / (60 * 60 * 1000);
		long ddays = diff / (24 * 60 * 60 * 1000);

		String toReturn = "";
		if (diff >= 0) {
			// In the past
			if (dminutes < 60) {
				toReturn = String.valueOf(dminutes);
				if (dminutes == 1) {
					toReturn += " " + MIN_AGO;
				} else {
					toReturn += " " + MINS_AGO;
				}
			} else if (dhours < 24) {
				toReturn = String.valueOf(dhours);
				if (dhours == 1) {
					toReturn += " " + HOUR_AGO;
				} else {
					toReturn += " " + HOURS_AGO;
				}
			} else if (ddays <= 7) {
				toReturn = String.valueOf(ddays);
				if (ddays == 1) {
					toReturn += " " + DAY_AGO;
				} else {
					toReturn += " " + DAYS_AGO;
				}
			} else {
				toReturn = formatDate(target, "dd MMM yyy");
			}
		} else {
			// In the future
			diff *= -1;
			dminutes *= -1;
			dhours *= -1;
			ddays *= -1;
			if (dminutes < 60) {
				toReturn = String.valueOf(dminutes);
				if (dminutes == 1) {
					toReturn += " " + MIN_LEFT;
				} else {
					toReturn += " " + MINS_LEFT;
				}
			} else if (dhours < 24) {
				toReturn = String.valueOf(dhours);
				if (dhours == 1) {
					toReturn += " " + HOUR_LEFT;
				} else {
					toReturn += " " + HOURS_LEFT;
				}
			} else if (ddays <= 7) {
				toReturn = String.valueOf(ddays);
				if (ddays == 1) {
					toReturn += " " + DAY_TO_GO;
				} else {
					toReturn += " " + DAYS_TO_GO;
				}
			} else {
				toReturn = formatDate(target, "dd MMM yyy");
			}
		}
		return toReturn;
	}

	public static String calcTimeDiff(DateTime target, Context context) {
		return calcTimeDiff(target.toDate(), context);
	}

	/**
	 * Calculate absolute time difference
	 *
	 * @param target  , date to be compared to now
	 * @param context , the context
	 * @return time difference, or date if it is more than 7 days
	 */
	public static String calcTimeDiff(Date target, Context context) {
		if (target == null || context == null) {
			return null;
		}
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();

		calendar1.setTime(target);

		long milsecs1 = calendar1.getTimeInMillis();
		long milsecs2 = calendar2.getTimeInMillis();

		long diff = milsecs2 - milsecs1;
		if (diff < 0) {
			diff *= -1; // make it absolute
		}
		long dminutes = diff / (60 * 1000);
		long dhours = diff / (60 * 60 * 1000);
		long ddays = diff / (24 * 60 * 60 * 1000);

		String toReturn = "";
		if (dminutes < 60) {
			toReturn = String.valueOf(dminutes);
			toReturn += " "
					+ context.getResources().getQuantityString(R.plurals.minutes, (int) dminutes);
		} else if (dhours < 24) {
			toReturn = String.valueOf(dhours);
			toReturn += " "
					+ context.getResources().getQuantityString(R.plurals.hours, (int) dhours);
		} else if (ddays <= 7) {
			toReturn = String.valueOf(ddays);
			toReturn += " " + context.getResources().getQuantityString(R.plurals.days, (int) ddays);
		} else {
			toReturn = formatDate(target, "dd MMM yyy");
		}
		return toReturn;
	}


	public static boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isAlphaNumeric(String s) {

		String pattern = "^[a-zA-Z0-9 ]*$";
		if (s.matches(pattern)) {
			return true;
		}
		return false;
	}

	// private static Intent pickFromGallery() {
	// Intent pickPhoto = new Intent(Intent.ACTION_GET_CONTENT);
	// pickPhoto.setType("image/*");
	// pickPhoto.putExtra("crop", "true");
	// // Set this to define the X aspect ratio of the shape
	// pickPhoto.putExtra("aspectX", 1);
	// // Set this to define the Y aspect ratio of the shape
	// pickPhoto.putExtra("aspectY", 1);
	// // pickPhoto.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
	// // pickPhoto
	// // .putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
	// return pickPhoto;
	// }

	private static Intent pickFromGallery() {
		Intent pickPhoto = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		pickPhoto.setType("image/*");
		pickPhoto.putExtra("crop", "true");
		// Set this to define the X aspect ratio of the shape
		pickPhoto.putExtra("aspectX", 1);
		// Set this to define the Y aspect ratio of the shape
		pickPhoto.putExtra("aspectY", 1);
		pickPhoto.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
		pickPhoto.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
		return pickPhoto;
	}

	private static Intent pickFromCamera() {

		Intent capturePhoto = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

		capturePhoto.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
		capturePhoto.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

		// Crop feature causes crash
		// capturePhoto.putExtra("crop", "true");
		// capturePhoto.putExtra("scale", true);
		// Set this to define the X aspect ratio of the shape
		// capturePhoto.putExtra("aspectX", 1);
		// Set this to define the Y aspect ratio of the shape
		// capturePhoto.putExtra("aspectY", 1);
		return capturePhoto;
	}

	private static Intent pickFromCamera(Fragment frag) {

		Intent capturePhoto = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

		Uri uri = null;
		try {
			uri = FileProvider.getUriForFile(frag.getActivity(), frag.getActivity().getPackageName() + ".provider", getTempFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		capturePhoto.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		capturePhoto.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

		// Crop feature causes crash
		// capturePhoto.putExtra("crop", "true");
		// capturePhoto.putExtra("scale", true);
		// Set this to define the X aspect ratio of the shape
		// capturePhoto.putExtra("aspectX", 1);
		// Set this to define the Y aspect ratio of the shape
		// capturePhoto.putExtra("aspectY", 1);
		return capturePhoto;
	}


	public static Uri getTempUri() {
		return Uri.fromFile(getTempFile());
	}

	/**
	 * Get the URL to the app detail page in the PlayStore
	 *
	 * @param context
	 * @return URL to the PlayStore
	 */
	public static String getPlayStoreLink(Context context) {
		final String packageName = context.getPackageName();
		return BaseConstants.PLAY_STORE_LINK + packageName;
	}

	public static void gotoSlide(Context context) {
		final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
		try {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getPlayStoreLink(context))));
		} catch (android.content.ActivityNotFoundException anfe) {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
		}
	}

	private static File getTempFile() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

			File file = new File(Environment.getExternalStorageDirectory(),
					BaseConstants.TEMP_PHOTO_FILE);
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return file;
		} else {
			return null;
		}
	}

	public static String getRealPathFromURI(Uri contentUri, Activity context) {
		String[] proj = {
				MediaStore.Images.Media.DATA
		};
		Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * Select an image and allow user to crop the image using requestCode {@link BaseHelper}
	 * .REQUEST_IMAGE_CODE Implement {@link BaseUIHelper} .processImage / processPreview
	 * onActivityResult to process the image results and get the file
	 *
	 * @param activity , activity to handle the onActivityResult
	 */
	public static void pickImage(final Activity activity) {
		AlertDialog.Builder bld = BaseHelper.buildAlert(activity, "Select Image Source", null);
		bld.setItems(R.array.imageSource, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						Intent i = pickFromGallery();
						activity.startActivityForResult(i, REQUEST_GET_IMAGE_CODE);
						break;
					case 1:
						// camera
						PackageManager pm = activity.getApplicationContext().getPackageManager();

						if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
							Intent i2 = pickFromCamera();
							activity.startActivityForResult(i2, REQUEST_GET_IMAGE_CODE);
						} else {
							BaseHelper.showAlert(activity, null, "You dont have a camera", null);
						}

						break;

					default:
						// nothing
				}
			}
		});
		bld.setNeutralButton(android.R.string.cancel, null);
		bld.setCancelable(true);
		bld.show();
	}

	/**
	 * Select an image and allow user to crop the image using requestCode {@link BaseHelper}
	 * .REQUEST_IMAGE_CODE Implement {@link BaseUIHelper} .processImage / processPreview
	 * onActivityResult to process the image results and get the file
	 *
	 * @param fragment , fragment to handle the onActivityResult
	 */
	public static void pickImage(final Fragment fragment) {
		AlertDialog.Builder bld = BaseHelper.buildAlert(fragment.getActivity(),
				"Select image source", null);
		bld.setItems(R.array.imageSource, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						Intent i = pickFromGallery();
						fragment.startActivityForResult(i, REQUEST_GET_IMAGE_CODE);
						break;
					case 1:
						// camera
						PackageManager pm = fragment.getActivity().getApplicationContext()
								.getPackageManager();

						if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
							Intent i2 = pickFromCamera();
							fragment.startActivityForResult(i2, REQUEST_GET_IMAGE_CODE);
						} else {
							BaseHelper.showAlert(fragment.getActivity(), null,
									"You dont have a camera", null);
						}

						break;

					default:
						// nothing
				}
			}
		});
		bld.setNeutralButton(android.R.string.cancel, null);
		bld.setCancelable(true);
		bld.show();
	}

	public static void pickImage(final Fragment fragment, boolean isAndroidN) {
		AlertDialog.Builder bld = BaseHelper.buildAlert(fragment.getActivity(),
				"Select image source", null);
		bld.setItems(R.array.imageSource, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						Intent i = pickFromGallery();
						fragment.startActivityForResult(i, REQUEST_GET_IMAGE_CODE);
						break;
					case 1:
						// camera
						PackageManager pm = fragment.getActivity().getApplicationContext()
								.getPackageManager();

						if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
							Intent i2 = pickFromCamera(fragment);
							fragment.startActivityForResult(i2, REQUEST_GET_IMAGE_CODE);
						} else {
							BaseHelper.showAlert(fragment.getActivity(), null,
									"You dont have a camera", null);
						}

						break;

					default:
						// nothing
				}
			}
		});
		bld.setNeutralButton(android.R.string.cancel, null);
		bld.setCancelable(true);
		bld.show();
	}

	/**
	 * Get the version name of this build
	 *
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		if (context == null) {
			return null;
		}
		String ver = null;
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			ver = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return ver;
	}

	/**
	 * Get the version code of this build
	 *
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context) {
		if (context == null) {
			return 0;
		}
		int ver = 0;
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			ver = pInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return ver;
	}

	/**
	 * Format distance to be displayed to the user
	 *
	 * @param d , distance in metres
	 * @return formatted {@link String}
	 */
	public static String formatDistance(double d) {
		DecimalFormat formatter = new DecimalFormat("#,### 'm'");
		d = Math.round(d);
		if (d > 1000) {
			formatter = new DecimalFormat("#,###.# 'km'");
			d = d / 1000;
		}

		return formatter.format(d);
	}

	public static String formatCurrency(String s,
										String Currency,
										boolean Spacing,
										boolean Delimiter,
										boolean Decimals,
										String Separator) {


		String cleanString = s.toString().replaceAll("[$,.]", "").replaceAll(Currency, "").replaceAll("\\s+", "");

		if (cleanString.length() != 0) {
			try {

				String currencyFormat = "";
				if (Spacing) {
					if (Delimiter) {
						currencyFormat = Currency + ". ";
					} else {
						currencyFormat = Currency + " ";
					}
				} else {
					if (Delimiter) {
						currencyFormat = Currency + ".";
					} else {
						currencyFormat = Currency;
					}
				}

				double parsed;
				int parsedInt;
				String formatted;

				if (Decimals) {
					parsed = Double.parseDouble(cleanString);
					formatted = NumberFormat.getCurrencyInstance().format((parsed / 100)).replace(NumberFormat.getCurrencyInstance().getCurrency().getSymbol(), currencyFormat);
				} else {
					parsedInt = Integer.parseInt(cleanString);
					formatted = currencyFormat + NumberFormat.getNumberInstance(Locale.US).format(parsedInt);
				}

				//if decimals are turned off and Separator is set as anything other than commas..
				if (!Separator.equals(",") && !Decimals) {
					//..replace the commas with the new separator
					s = formatted.replaceAll(",", Separator);
				} else {
					//since no custom separators were set, proceed with comma separation
					s = formatted;
				}

			} catch (Exception e) {
			}
		}

		return s;
	}


	public static void ShareToFriends(Context context, String content) {
		String shareBody = content;
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		context.startActivity(Intent.createChooser(sharingIntent, ""));
	}

	public static void SharingUsingWhatsapp(Context context, String content) {
		Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
		whatsappIntent.setType("text/plain");
		whatsappIntent.setPackage("com.whatsapp");
		whatsappIntent.putExtra(Intent.EXTRA_TEXT, content);
		try {
			context.startActivity(whatsappIntent);
		} catch (android.content.ActivityNotFoundException ex) {

		}
	}

	public static void SharingUsingMessage(Context context, String content) {
		Intent message = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ""));
		message.putExtra("sms_body", content);
		context.startActivity(message);
	}

	public static void SharingUsingfb(Context context, String content) {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, content);
		share.setPackage("com.facebook.katana"); //Facebook App package
		context.startActivity(Intent.createChooser(share, "Title of the dialog the system will open"));
	}

	public static void CallPhone(Context context, String phone_number) {


		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_number));
		context.startActivity(intent);
	}

	public static void SharingUsingTwitter(Context context, String content) {

		Intent shareIntent;

		if (isPackageExisted(context, "com.twitter.android")) {
			shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setClassName("com.twitter.android",
					"com.twitter.android.PostActivity");
			shareIntent.setType("text/*");
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
			context.startActivity(shareIntent);

		} else {
			String tweetUrl = "https://twitter.com/intent/tweet?text=" + content;
			Uri uri = Uri.parse(tweetUrl);
			shareIntent = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(shareIntent);

		}

	}

	public static boolean isPackageExisted(Context context, String targetPackage) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
		return true;
	}

	public String hmacSha256(String KEY, String VALUE) {

		return hmacSha(KEY, VALUE, "HmacSHA256");
	}

	private String hmacSha(String KEY, String VALUE, String SHA_TYPE) {
		try {
			SecretKeySpec signingKey = new SecretKeySpec(KEY.getBytes(), SHA_TYPE);
			Mac mac = Mac.getInstance(SHA_TYPE);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(VALUE.getBytes());

			byte[] hexArray = {
					(byte)'0', (byte)'1', (byte)'2', (byte)'3',
					(byte)'4', (byte)'5', (byte)'6', (byte)'7',
					(byte)'8', (byte)'9', (byte)'a', (byte)'b',
					(byte)'c', (byte)'d', (byte)'e', (byte)'f'
			};
			byte[] hexChars = new byte[rawHmac.length * 2];
			for ( int j = 0; j < rawHmac.length; j++ ) {
				int v = rawHmac[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			}
			return new String(hexChars);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getRandomString(int length) {
		Random random = new SecureRandom();
		StringBuffer sb = new StringBuffer();
		while (sb.length() < length) {
			sb.append(Integer.toHexString(random.nextInt()));
		}

		return sb.toString().substring(0, length);
	}

	public byte[] padBytes(byte[] source){
		char paddingChar = ' ';
		int size = 16;
		int x = source.length % size;
		int padLength = size - x;
		int bufferLength = source.length + padLength;
		byte[] ret = new byte[bufferLength];
		int i = 0;
		for ( ; i < source.length; i++){
			ret[i] = source[i];
			ret[i] = source[i];
		}
		for ( ; i < bufferLength; i++){
			ret[i] = (byte)paddingChar;
		}

		return ret;
	}

	public static void openUpdateInstall(Context context) {
		try {
			try {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BaseConstants.PLAY_STORE_LINK + context.getPackageName())));
			} catch (android.content.ActivityNotFoundException anfe) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.iapps.gon")));
			}
		} catch (Exception e) {
		}
	}


	public static Bitmap ScaleBitmap(Bitmap temp, int size)
	{

		if (size > 0) {
			int width = temp.getWidth();
			int height = temp.getHeight();
			float ratioBitmap = (float) width / (float) height;
			int finalWidth = size;
			int finalHeight = size;
			if (ratioBitmap < 1) {
				finalWidth = (int) ((float) size * ratioBitmap);
			} else {
				finalHeight = (int) ((float) size / ratioBitmap);
			}
			return Bitmap.createScaledBitmap(temp, finalWidth, finalHeight, true);
		} else {
			return temp;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static void triggerNotifLog(Activity act){
		try {
			Paper.init(act);
			Intent intent = new Intent(act, ActivityPascaLog.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(act, 01, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			Notification.Builder builder = new Notification.Builder(act);
			Helper.setAppPackage(act, act.getPackageName());
			builder.setContentTitle("Aware User Log is running");
			builder.setContentText("Click to launch screen");
			builder.setContentIntent(pendingIntent);
			builder.setTicker("Aware Log");
			builder.setSmallIcon(R.drawable.memu_logo);
			builder.setAutoCancel(false);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				builder.setPriority(Notification.PRIORITY_HIGH);
			}
			builder.setOngoing(true);
			Notification notification = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				notification = builder.build();
			}

			NotificationManager notificationManger =
					(NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				String channelId = "Your_channel_id";
				int importance = NotificationManager.IMPORTANCE_DEFAULT;

				NotificationChannel channel = new NotificationChannel(
						channelId,
						"Channel human readable title",
						importance);
				notificationManger.createNotificationChannel(channel);
				builder.setChannelId(channelId);
			}
			notificationManger.notify(223456, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static Uri getImageUri(Context applicationContext, Bitmap photo)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStore.Images.Media.TITLE, "Image");
		contentValues.put("_data", "/sdcard/signifio/1111.jpg");
		//String path = applicationContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), photo, "Title", null);
		return Uri.parse("path");
	}
	public static String getRealPathFromUri(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	public static Bitmap drawableToBitmap (Drawable drawable) {
		Bitmap bitmap = null;

		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if(bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}

		if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

/*
	public static LatLng getLocationFromAddress(String strAddress, Context context){

		Geocoder coder = new Geocoder(context);
		List<Address> address;
		LatLng p1 = null;

		try {
			// May throw an IOException
			address = coder.getFromLocationName(strAddress, 5);
			if (address == null) {
				return null;
			}

			Address location = address.get(0);
			p1 = new LatLng(location.getLatitude(), location.getLongitude() );

		} catch (Exception ex) {
			System.out.println("getLocationFromAddress Exception "+ex.toString());
			ex.printStackTrace();
		}

		return p1;
	}
*/
	public static JSONObject getLocationInfo(String address) {
		StringBuilder stringBuilder = new StringBuilder();
		try {

			address = address.replaceAll(" ","%20");

			HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
			HttpClient client = new DefaultHttpClient();
			HttpResponse response;
			stringBuilder = new StringBuilder();


			response = client.execute(httppost);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
			System.out.println("Exception12334 "+stringBuilder.toString());

		} catch (Exception e) {
			System.out.println("Exception12334 "+e.toString());
		}

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(stringBuilder.toString());
		} catch (Exception e) {
			System.out.println("Exception12334 "+e.toString());

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject;
	}
	public static double showDistance(LatLng origin, LatLng dest ) {
		Location locationA = new Location("Location A");

		locationA.setLatitude(origin.latitude);

		locationA.setLongitude(origin.longitude);

		Location locationB = new Location("Location B");

		locationB.setLatitude(dest.latitude);

		locationB.setLongitude(dest.longitude);
		locationA.getTime();
		System.out.println("showDistance "+locationA.distanceTo(locationB) * 0.001);
		return locationA.distanceTo(locationB) * 0.001;


	}

	public static double showTime(LatLng origin, LatLng dest ) {

		Location locationA = new Location("Location A");

		locationA.setLatitude(origin.latitude);

		locationA.setLongitude(origin.longitude);

		Location locationB = new Location("Location B");

		locationB.setLatitude(dest.latitude);

		locationB.setLongitude(dest.longitude);
		double dist = locationA.distanceTo(locationB) * 0.001 ;
		int speedIs1KmMinute = 100;
		double estimatedDriveTimeInMinutes = dist / speedIs1KmMinute;
		return estimatedDriveTimeInMinutes;

	}
	public static String getHAshKey(Context context){
		PackageInfo info;
		try {
			info = context.getPackageManager().getPackageInfo("com.memu", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encodeBase64(md.digest(), false));
				//String something = new String(Base64.encodeBytes(md.digest()));
				Log.e("hash key", something);
				return something;
			}
		} catch (NameNotFoundException e1) {
			Log.e("name not found", e1.toString());
		} catch (NoSuchAlgorithmException e) {
			Log.e("no such an algorithm", e.toString());
		} catch (Exception e) {
			Log.e("exception", e.toString());
		}
		return "";
	}

	public static AnimatorSet getViewToViewScalingAnimator(final View parentView,
														   final View viewToAnimate,
														   final View fromViewRect,
														   final View toViewRect,
														   final long duration,
														   final long startDelay) {
		// get all coordinates at once

		viewToAnimate.setScaleX(1f);
		viewToAnimate.setScaleY(1f);

		// rescaling of the object on X-axis
		final ValueAnimator valueAnimatorWidth = ValueAnimator.ofInt(fromViewRect.getWidth(), toViewRect.getWidth());
		valueAnimatorWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// Get animated width value update
				int newWidth = (int) valueAnimatorWidth.getAnimatedValue();

				// Get and update LayoutParams of the animated view
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewToAnimate.getLayoutParams();

				lp.width = newWidth;
				viewToAnimate.setLayoutParams(lp);
			}
		});

		// rescaling of the object on Y-axis
		final ValueAnimator valueAnimatorHeight = ValueAnimator.ofInt(fromViewRect.getHeight(), toViewRect.getHeight());
		valueAnimatorHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// Get animated width value update
				int newHeight = (int) valueAnimatorHeight.getAnimatedValue();

				// Get and update LayoutParams of the animated view
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewToAnimate.getLayoutParams();
				lp.height = newHeight;
				viewToAnimate.setLayoutParams(lp);
			}
		});

		// moving of the object on X-axis
		ObjectAnimator translateAnimatorX = ObjectAnimator.ofFloat(viewToAnimate, "X", fromViewRect.getLeft() - parentView.getLeft(), toViewRect.getLeft() - parentView.getLeft());

		// moving of the object on Y-axis
		ObjectAnimator translateAnimatorY = ObjectAnimator.ofFloat(viewToAnimate, "Y", fromViewRect.getTop() - parentView.getTop(), toViewRect.getTop() - parentView.getTop());

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.setInterpolator(new DecelerateInterpolator(1f));
		animatorSet.setDuration(duration); // can be decoupled for each animator separately
		animatorSet.setStartDelay(startDelay); // can be decoupled for each animator separately
		animatorSet.playTogether(valueAnimatorWidth, valueAnimatorHeight, translateAnimatorX, translateAnimatorY);

		return animatorSet;
	}
	public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(
				bm, 0, 0, width, height, matrix, false);
		bm.recycle();
		return resizedBitmap;
	}
	public static Intent getPickIntent(Uri cameraOutputUri,Context context) {
		final List<Intent> intents = new ArrayList<Intent>();

		if (true) {
			intents.add(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
		}

		if (true) {
			setCameraIntents(intents, cameraOutputUri,context);
		}

		if (intents.isEmpty()) return null;
		Intent result = Intent.createChooser(intents.remove(0), null);
		if (!intents.isEmpty()) {
			result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[] {}));
		}
		return result;
	}
	private static void setCameraIntents(List<Intent> cameraIntents, Uri output, Context context) {
		final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		final PackageManager packageManager = context.getPackageManager();
		final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
		for (ResolveInfo res : listCam) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (intent.resolveActivity(context.getPackageManager()) != null) {
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, output);
				cameraIntents.add(intent);
			}
		}
	}
	public static boolean containsIgnoreCase(List<String> list, String soughtFor) {
		for (String current : list) {
			if (current.equalsIgnoreCase(soughtFor)) {
				return true;
			}
		}
		return false;
	}
}
