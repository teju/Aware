package com.iapps.logs.com.pascalabs.util.log.helper;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iapps.common_library.R;
import com.iapps.logs.com.pascalabs.util.log.model.BeanLog;
import com.iapps.logs.com.pascalabs.util.log.model.BeanLogAPI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class Helper {

	public static final ExecutorService exLogServiceTPE = generateTPE();

	public static ThreadPoolExecutor generateTPE(){
		// extend LinkedBlockingQueue to force offer() to return false conditionally
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>() {
			@Override
			public boolean offer(Runnable e) {
        /*
         * Offer it to the queue if there is 0 items already queued, else
         * return false so the TPE will add another thread. If we return false
         * and max threads have been reached then the RejectedExecutionHandler
         * will be called which will do the put into the queue.
         */
				if (size() == 0) {
					return super.offer(e);
				} else {
					return false;
				}
			}
		};
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1 /*core*/, 15 /*max*/,
				60 /*secs*/, TimeUnit.SECONDS, queue);
		threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				try {
            /*
             * This does the actual put into the queue. Once the max threads
             * have been reached, the tasks will then queue up.
             */
					executor.getQueue().put(r);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		});
		return threadPool;
	}


	public static String headers;
	public static HashMap<String, String> headerList;

	public static void setEmailAndAddressForSendLogReport(Context ctx, String email, String subject){
		Preference.getInstance(ctx).saveEmailAddressSaved(email);
		Preference.getInstance(ctx).saveEmailSubjectSaved(subject);
	}

	public static void setAppPackage(Context ctx, String fullPackageName){
		Preference.getInstance(ctx).saveAppPackageSaved(fullPackageName);
	}

	public static final ExecutorService exLogService1 = Executors.newSingleThreadExecutor();

	public static void logEventLocal(final Context ctx, final String type, final String event,
			                        final String response){

		if(!Preference.getInstance(ctx).getONOFFSaved()) return;

		final Date dateObj = new Date();
		final String date = String.valueOf(dateObj);

		final GenericAsyncTask taskExecute1 = new GenericAsyncTask(ctx);
		taskExecute1.setTaskListener(new GenericAsyncTask.TaskListener() {

			@Override
			public void onPreExecute() {}

			@Override
			public void doInBackground() {
				try {

					String filterName = Preference.getInstance(taskExecute1.getContext()).getFilterSaved();
					if(!filterName.equalsIgnoreCase("Show ALL") && !filterName.equalsIgnoreCase("API")) return;

					ArrayList<BeanLog> templog = null;

					try {
						templog = Preference.getInstance(taskExecute1.getContext()).getLogTrackerSaved();
					} catch (Exception e) {
						if(templog==null) templog = new ArrayList<BeanLog>();
					}
					if(templog==null) templog = new ArrayList<BeanLog>();

					BeanLog beanLog = new BeanLog(type, event, date);
					beanLog.setTimestampMilis(dateObj.getTime());
					templog.add(beanLog);
					Preference.getInstance(taskExecute1.getContext()).saveLogTrackerSaved(templog);

				} catch (Exception e) {}
			}

			@Override
			public void onPostExecute() {}
		});
		executeOnExecutorWrapper(taskExecute1, exLogService1);

		final GenericAsyncTask taskExecute2 = new GenericAsyncTask(ctx);
		taskExecute2.setTaskListener(new GenericAsyncTask.TaskListener() {
			@Override
			public void onPreExecute() {}

			@Override
			public void doInBackground() {

				if(!Preference.getInstance(taskExecute2.getContext()).getONOFFSaved()) return;

				if(type.equalsIgnoreCase("api")){
					String url;
					String[] urlList = event.split("\n", 10);
					url = urlList[0];
					url = url.replaceAll("/", "");
					url = url.replaceAll(":", "");
					Paper.book().write(type + url + date, response);
				}else{
					Paper.book().write(type + date, response);
				}
			}

			@Override
			public void onPostExecute() {}
		});
		executeOnExecutorWrapper(taskExecute2, exLogServiceTPE);
	}

	public static final ExecutorService exLogService2 = Executors.newSingleThreadExecutor();

	public static void logEventLocal(final Context ctx, final String type, final String event,
									 final String response, final BeanLogAPI beanLogAPI){
		if(!Preference.getInstance(ctx).getONOFFSaved()) return;

		if(!Preference.getInstance(ctx).getONOFFSaved()) return;

		final GenericAsyncTask taskExecute3 = new GenericAsyncTask(ctx);
		taskExecute3.setTaskListener(new GenericAsyncTask.TaskListener() {
			@Override
			public void onPreExecute() {}

			@Override
			public void doInBackground() {
				try {

					String filterName = Preference.getInstance(taskExecute3.getContext()).getFilterSaved();
					if(!filterName.equalsIgnoreCase("Show ALL") && !filterName.equalsIgnoreCase("API")) return;

					ArrayList<BeanLog> templog = null;

					try {
						templog = Preference.getInstance(taskExecute3.getContext()).getLogTrackerSaved();
					} catch (Exception e) {
						if(templog==null) templog = new ArrayList<BeanLog>();
					}
					if(templog==null) templog = new ArrayList<BeanLog>();
					Date dateObj = new Date();
					String date = String.valueOf(dateObj);
					BeanLog beanLog = new BeanLog(type, event, date);
					beanLog.setTimestampMilis(dateObj.getTime());
					beanLog.setBeanLogAPI(beanLogAPI);
					templog.add(beanLog);
					Preference.getInstance(taskExecute3.getContext()).saveLogTrackerSaved(templog);

					if(type.equalsIgnoreCase("api")){
						String url;
						String[] urlList = event.split("\n", 10);
						url = urlList[0];
						url = url.replaceAll("/", "");
						url = url.replaceAll(":", "");
						Paper.book().write(type + url + date, response);
					}else{
						Paper.book().write(type + date, response);
					}

				} catch (Exception e) {}
			}

			@Override
			public void onPostExecute() {}
		});
		executeOnExecutorWrapper(taskExecute3, exLogService2);

	}

	public static final ExecutorService exLogService3 = Executors.newSingleThreadExecutor();

	public static void logEventLocal(final Context ctx, final String type, final String event){

		if(!Preference.getInstance(ctx).getONOFFSaved()) return;

		final GenericAsyncTask taskExecute4 = new GenericAsyncTask(ctx);
		taskExecute4.setTaskListener(new GenericAsyncTask.TaskListener() {
			@Override
			public void onPreExecute() {}

			@Override
			public void doInBackground() {
				try {

					String filterName = Preference.getInstance(taskExecute4.getContext()).getFilterSaved();
					if(!filterName.equalsIgnoreCase("Show ALL") && !filterName.equalsIgnoreCase("CHAT")) return;

					ArrayList<BeanLog> templog = null;

					try {
						templog = Preference.getInstance(taskExecute4.getContext()).getLogTrackerSaved();
					} catch (Exception e) {
						if(templog==null) templog = new ArrayList<BeanLog>();
					}

					if(templog==null) templog = new ArrayList<BeanLog>();

					Date dateObj = new Date();
					String date = String.valueOf(dateObj);
					BeanLog beanLog = new BeanLog(type, event, date);
					beanLog.setTimestampMilis(dateObj.getTime());
					templog.add(beanLog);
					Preference.getInstance(taskExecute4.getContext()).saveLogTrackerSaved(templog);

				} catch (Exception e) {}
			}

			@Override
			public void onPostExecute() {}
		});
		executeOnExecutorWrapper(taskExecute4, exLogService3);
	}

	public static void logExceptionMessage(final Context ctx, final String tag, final String eMessage){

		if(!Preference.getInstance(ctx).getONOFFSaved()) return;

		final GenericAsyncTask taskExecute5 = new GenericAsyncTask(ctx);
		taskExecute5.setTaskListener(new GenericAsyncTask.TaskListener() {
			@Override
			public void onPreExecute() {}

			@Override
			public void doInBackground() {
				try {

					if(!Preference.getInstance(taskExecute5.getContext()).getONOFFSaved()) return;

					String filterName = Preference.getInstance(taskExecute5.getContext()).getFilterSaved();
					if(!filterName.equalsIgnoreCase("Show ALL") && !filterName.equalsIgnoreCase("Exception")) return;

					ArrayList<BeanLog> templog = null;

					try {
						templog = Preference.getInstance(taskExecute5.getContext()).getLogTrackerSaved();
					} catch (Exception e) {
						if(templog==null) templog = new ArrayList<BeanLog>();
					}
					if(templog==null) templog = new ArrayList<BeanLog>();

					Date dateObj = new Date();
					String date = String.valueOf(dateObj);
					BeanLog beanLog = new BeanLog("Exception", tag, date);
					beanLog.setTimestampMilis(dateObj.getTime());
					templog.add(beanLog);
					Preference.getInstance(taskExecute5.getContext()).saveLogTrackerSaved(templog);
					Paper.book().write("Exception" + tag + date, eMessage);

				} catch (Exception e) {}
			}

			@Override
			public void onPostExecute() {}
		});
		executeOnExecutorWrapper(taskExecute5, exLogService3);
	}

	public static void logThisAPI(Context ctx, BeanLogAPI api){

		if(!Preference.getInstance(ctx).getONOFFSaved()) return;

		logThisAPI(ctx, api, "URL");
	}

	public static void logThisAPI(Context ctx, BeanLogAPI api, String Title){
		try {
			String shortenClassName = "N/A";
			try {
				shortenClassName = api.getShortenClassName().getClass().toString().substring(api.getShortenClassName().getClass().toString().indexOf("$")+1);
			} catch (Exception e) {}
			String params = "N/A";
			try {
				params = api.getParams().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String method = "N/A";
			try {
				method = api.getMethod();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String header = "N/A";
			try {
				if (api.getHeader() != null) {
					header = api.getHeader().toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String fileparam = "N/A";
			try {
				fileparam = api.getFileparam().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String byteparam = "N/A";
			try {
				byteparam = api.getByteparam().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String url = "N/A";
			try {
				url = api.getUrl().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String dateString = "N/A";
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(Constants.TIME_JSON_SHORTEN);
				dateString = formatter.format(new Date());
			} catch (Exception e) {
				e.printStackTrace();
			}

			logEventLocal(ctx, Title, url + "\n" +
					"Date : " + dateString + "\n" +
					"Method : " + method + "\n" +
					"------------------------------\n" +
					"Header : " + header + "\n" +
					"------------------------------\n" +
					(headers!=null?headers:"")+
					"\n------------------------------\n" +
					"Params : " + params + "\n" +
					"File Params : " + fileparam + "\n" +
					"Byte Params : " + byteparam + "\n" +
					"------------------------------\n" +
					"Status Code : " + api.getStatuscode() + "\n" +
					"------------------------------\n" +
					"Exception : " + api.getException() + "\n" +
					"------------------------------\n" ,
					api.getContent().toString(), api);
		} catch (Exception e) {

		}
	}

	public static void logException(Context ctx, Exception e){
		try {
			if(Constants.IS_DEBUGGING){
				if(Constants.IS_DEBUGGING){
					if(ctx != null)
						Log.v(ctx.getString(R.string.app_name), getStackTrace(e));
				}
			}
		} catch (Exception e1) {}
	}

	public static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	public static class GenericAsyncTask extends AsyncTask<Void, Void, Void>{

		private Context context;
		private Activity activity;


		public GenericAsyncTask() {}

		public GenericAsyncTask(Context context) {
			WeakReference<Context> weakContext = new WeakReference<>(context);
			this.context = weakContext.get();
		}

		public GenericAsyncTask(Activity activity) {
			WeakReference<Activity> weakActivity = new WeakReference<>(activity);
			this.activity = weakActivity.get();
		}

		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			WeakReference<Context> weakContext = new WeakReference<Context>(context);
			this.context = weakContext.get();
		}

		public Activity getActivity() {
			return activity;
		}

		public void setActivity(Activity activity) {
			WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
			this.activity = weakActivity.get();
		}

		public interface TaskListener{
			void onPreExecute();
			void doInBackground();
			void onPostExecute();
		}

		TaskListener taskListener;

		public void setTaskListener(TaskListener taskListener) {
			this.taskListener = taskListener;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			taskListener.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			taskListener.doInBackground();
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			taskListener.onPostExecute();
		}
	}

	public static void executeOnExecutorWrapper(GenericAsyncTask gat, ExecutorService executorService){
		try {
			gat.executeOnExecutor(executorService);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
