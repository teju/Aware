package com.iapps.logs.com.pascalabs.util.log.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.iapps.logs.com.pascalabs.util.log.model.BeanLog;

import java.util.ArrayList;

import io.paperdb.Paper;

public class Preference {

	public final static String PREF_NAME_NO_CLEAR = "pascalabs_log_noclear";

	//Email address
	private final String EMAIL_ADDRESS_SAVED = "email_address_saved";

	//Email Subject
	private final String EMAIL_SUBJECT_SAVED = "email_subject_saved";

	// App Package
	private final String APP_PACKAGE_SAVED = "app_package_saved";

	//Filter
	private final String FILTER_SAVED = "log_saved";

	//ON OFF
	private final String ONOFF_SAVED = "onoff_saved";

    //LOG
    private final String LOG_SAVED = "log_saved";

	private Context context;
	private static Preference pref;

	public static Preference getInstance(Context context) {
		if (pref == null) {
			pref = new Preference(context);
		}

		return pref;
	}

	private Preference(Context context) {
		this.context = context;
	}

	public void filterSaved(String filterName) {
		try {
			getPreferenceNoClear().edit().putString(FILTER_SAVED, filterName).commit();
		} catch (Exception e) {}
	}

	public String getFilterSaved() {
		try {
			String test = getPreferenceNoClear().getString(FILTER_SAVED, "API");
			return test;
		} catch (Exception e) {}
		return null;
	}


	public void saveEmailAddressSaved(String email) {
		try {
			getPreferenceNoClear().edit().putString(EMAIL_ADDRESS_SAVED, email).commit();
		} catch (Exception e) {}
	}

	public String getEmailAddressSaved() {
		try {
			String test = getPreferenceNoClear().getString(EMAIL_ADDRESS_SAVED, null);
			return test;
		} catch (Exception e) {}
		return null;
	}

	public void saveEmailSubjectSaved(String subject) {
		try {
			getPreferenceNoClear().edit().putString(EMAIL_SUBJECT_SAVED, subject).commit();
		} catch (Exception e) {}
	}

	public String getEmailSubjectSaved() {
		try {
			String test = getPreferenceNoClear().getString(EMAIL_SUBJECT_SAVED, null);
			return test;
		} catch (Exception e) {}
		return null;
	}


	public void saveONOFFSaved(boolean onoff) {
		try {
			getPreferenceNoClear().edit().putBoolean(ONOFF_SAVED, onoff).commit();
		} catch (Exception e) {}
	}

	public boolean getONOFFSaved() {
		try {
			boolean test = getPreferenceNoClear().getBoolean(ONOFF_SAVED, false);
			return test;
		} catch (Exception e) {}
		return false;
	}

	public void saveAppPackageSaved(String packageName) {
		try {
			getPreferenceNoClear().edit().putString(APP_PACKAGE_SAVED, packageName).commit();
		} catch (Exception e) {}
	}

	public String getAppPackageSaved() {
		try {
			String test = getPreferenceNoClear().getString(APP_PACKAGE_SAVED, null);
			return test;
		} catch (Exception e) {}
		return null;
	}

	public SharedPreferences getPreferenceNoClear() {
		return context.getSharedPreferences(PREF_NAME_NO_CLEAR, Context.MODE_PRIVATE);
	}

	public void saveLogTrackerSaved(ArrayList<BeanLog> nsaved) {

		if(!getONOFFSaved()) return;

			try {
				if(nsaved.size() > 50){
                    nsaved = new ArrayList<BeanLog>();
                }

				Paper.book().write("log", nsaved);

			} catch (Exception e) {}

	}

	public ArrayList<BeanLog> getLogTrackerSaved() {

		if(!getONOFFSaved()) return null;

//		try {
//			String test = getPreferenceNoClear().getString(LOG_SAVED, null);
//			return ((ArrayList<BeanLog>)ObjectSerializer.deserialize(test));
//		} catch (Exception e) {}

		try {
			return Paper.book().read("log");
		} catch (Exception e) {}

		return null;
	}

	public String getResponse(BeanLog beanLog){
		try {
			if(beanLog.getType().equalsIgnoreCase("API")) {
				String url;
				String[] urlList = beanLog.getEvent().split("\n", 10);
				url = urlList[0];
				url = url.replaceAll("/", "");
				url = url.replaceAll(":", "");
				return Paper.book().read(beanLog.getType() + url + beanLog.getTimestamp());
			}
			return Paper.book().read(beanLog.getType() + beanLog.getTimestamp());
		} catch (Exception e) {}

		return null;
	}

	public String getException(BeanLog beanLog){
		try {
			return Paper.book().read(beanLog.getType() +  beanLog.getEvent() + beanLog.getTimestamp());
		} catch (Exception e) {}

		return null;
	}

}
