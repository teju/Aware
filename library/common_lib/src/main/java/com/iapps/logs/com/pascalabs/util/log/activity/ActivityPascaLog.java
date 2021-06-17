package com.iapps.logs.com.pascalabs.util.log.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;


import com.github.droidpl.android.jsonviewer.JSONViewerActivity;
import com.iapps.common_library.R;
import com.iapps.logs.com.pascalabs.util.log.helper.EasyTimer;
import com.iapps.logs.com.pascalabs.util.log.helper.Helper;
import com.iapps.logs.com.pascalabs.util.log.helper.Preference;
import com.iapps.logs.com.pascalabs.util.log.model.BeanLog;
import com.iapps.logs.com.pascalabs.util.log.model.BeanLogAPI;
import com.pascalabs.util.log.adapter.LogAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;


public class ActivityPascaLog extends Activity implements View.OnClickListener {

    public static final ExecutorService exService = createTPENoQueue();

    public static ThreadPoolExecutor createTPENoQueue(){
        BlockingQueue queue = new ArrayBlockingQueue(1);
        return new ThreadPoolExecutor(1, 1, 3, TimeUnit.SECONDS, queue, new ThreadPoolExecutor.DiscardPolicy());
    }

    private ListView lv;
    private Button btnSendEmail;
    private Button btnJSONViewer, btnMenu, btnCloseLog, btnAPIToolkit;
    private TextView tvDetail;
    private Button btnClose;
    private ScrollView SVLogDetail;
    private TextView tvLoading, tvFilterBy;
    private LinearLayout LLFirst;
    private String jsonResponse = "";
    private BeanLogAPI selectedBeanLogApi;
    public static boolean justGotBackFromCallingRetrofitApiTesting = false;
    private String filterName = "Show All";
    private SwitchCompat scOnOffLog;

    private LogAdapter mAdapter;
    private ArrayList<BeanLog> mLog = new ArrayList<BeanLog>();

    private boolean toogleLogProcess = false;
    private boolean isStillProcessing = false;

    EasyTimer refreshTimerTask = new EasyTimer(1000);

    Helper.GenericAsyncTask taskExecute1, taskExecute2, taskExecute3, taskExecute4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Paper.init(this);
        setContentView(R.layout.fragment_log);

        lv = (ListView) findViewById(R.id.lv);
        btnSendEmail = (Button) findViewById(R.id.btnSendEmail);
        btnJSONViewer = (Button) findViewById(R.id.btnJSONViewer);
        tvDetail = (TextView) findViewById(R.id.tvDetail);
        btnClose = (Button) findViewById(R.id.btnClose);
        btnAPIToolkit = (Button) findViewById(R.id.btnAPIToolkit);
        SVLogDetail = (ScrollView) findViewById(R.id.SVLogDetail);
        tvLoading = (TextView) findViewById(R.id.tvLoading);
        LLFirst = (LinearLayout) findViewById(R.id.LLFirst);
        btnMenu = (Button) findViewById(R.id.btnMenu);
        btnCloseLog  = (Button) findViewById(R.id.btnCloseLog);
        tvFilterBy  = (TextView) findViewById(R.id.tvFilterBy);
        scOnOffLog = (SwitchCompat) findViewById(R.id.scOnOffLog);

        lv.setOnItemClickListener(ListenerClickItem);
        btnSendEmail.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnJSONViewer.setOnClickListener(this);


        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenuDialog();
            }
        });

        btnCloseLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnAPIToolkit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ActivityPascaLog.this, "To use this feature, please open the app first", Toast.LENGTH_SHORT).show();
                goToRetrofitAPITesting(selectedBeanLogApi);
            }
        });


        if(Preference.getInstance(this).getONOFFSaved()){
            scOnOffLog.setChecked(true);
        }else{
            scOnOffLog.setChecked(false);
        }

        scOnOffLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Preference.getInstance(ActivityPascaLog.this).saveONOFFSaved(true);
                }else{
                    Preference.getInstance(ActivityPascaLog.this).saveONOFFSaved(false);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        //halt back button to close, use the top close button to close
        //to minimize mistakenly closing the log

        SVLogDetail.setVisibility(View.GONE);
        LLFirst.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            refreshTimerTask = new EasyTimer(1000);
            refreshTimerTask.setOnTaskRunListener(new EasyTimer.OnTaskRunListener() {
                @Override
                public void onTaskRun(long past_time, String rendered_time) {
                    try {
                        if (isStillProcessing == false) {
                            isStillProcessing = true;
                            loadLogData();
                        }
                    } catch (Exception e) {}
                }
            });

            refreshTimerTask.start();
        } catch (Exception e) {}


        if(justGotBackFromCallingRetrofitApiTesting){
            taskExecute1 = new Helper.GenericAsyncTask(this);
            taskExecute1.setTaskListener(new Helper.GenericAsyncTask.TaskListener() {

                String log = "";
                String response = "";
                BeanLog beanLog;

                @Override
                public void onPreExecute() {
                    if(tvLoading != null)
                        tvLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void doInBackground() {
                    try {
                        ArrayList<BeanLog> logListTemp = Preference.getInstance(ActivityPascaLog.this).getLogTrackerSaved();
                        ArrayList<BeanLog> logListFilteredAPI =  new ArrayList<BeanLog>();

                        for(int i = 0 ;  i < logListTemp.size(); i++) {
                            try {
                                if(logListTemp.get(i).getType().equalsIgnoreCase("api")){
                                    BeanLog beanLog = new BeanLog(logListTemp.get(i).getType(),
                                            logListTemp.get(i).getEvent(),
                                            logListTemp.get(i).getTimestamp());
                                    beanLog.setTimestampMilis(logListTemp.get(i).getTimestampMilis());
                                    beanLog.setBeanLogAPI(logListTemp.get(i).getBeanLogAPI());
                                    logListFilteredAPI.add(beanLog);
                                }
                            } catch (Exception e) {}
                        }

                        Collections.reverse(logListFilteredAPI);
                        beanLog = logListFilteredAPI.get(0);
                        log = beanLog.getEvent();

                        if(beanLog.getType().equalsIgnoreCase("api")) {
                            try {
                                response = Preference.getInstance(ActivityPascaLog.this).
                                        getResponse(beanLog);

                                if (response == null) throw new Exception("no response");

                                log = log + "Response : " + response;
                            } catch (Exception e) {
                                log = log + "Response : Unable to get the response\n\n" + getStackTrace(e);
                            }

                            try {
                                selectedBeanLogApi = (beanLog).getBeanLogAPI();
                            } catch (Exception e) {}

                        }else if(beanLog.getType().equalsIgnoreCase("exception")) {

                            String eMessage;

                            try {
                                eMessage = Preference.getInstance(ActivityPascaLog.this).
                                        getException(beanLog);

                                if (eMessage == null) throw new Exception("no exception message");

                                log = log + "Exception : " + eMessage;
                            } catch (Exception e) {
                                log = log + "Unable to get the exception message\n\n" + getStackTrace(e);
                            }
                        }
                    } catch (Exception e) {}
                }

                @Override
                public void onPostExecute() {
                    try {
                        tvLoading.setVisibility(View.GONE);
                        tvDetail.setText(log);
                        try {
                            jsonResponse = response;
                        } catch (Exception e) {}
                        SVLogDetail.setVisibility(View.VISIBLE);
                        LLFirst.setVisibility(View.GONE);
                    } catch (Exception e) {}
                }
            });
            executeParalelPlease(taskExecute1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        justGotBackFromCallingRetrofitApiTesting = false;
        try {
            refreshTimerTask.stop();
        } catch (Exception e) {}

        if(taskExecute1 != null){
            taskExecute1.cancel(true);
        }
        if(taskExecute2 != null){
            taskExecute2.cancel(true);
        }
        if(taskExecute3 != null){
            taskExecute3.cancel(true);
        }
        if(taskExecute4 != null){
            taskExecute4.cancel(true);
        }
    }

    public void loadLogData() {
        if(toogleLogProcess) return;

        taskExecute2 = new Helper.GenericAsyncTask(this);
        taskExecute2.setTaskListener(new Helper.GenericAsyncTask.TaskListener() {
            @Override
            public void onPreExecute() {
                try {
                    if(mLog.size() == 0)
                        tvLoading.setVisibility(View.VISIBLE);
                } catch (Exception e) {}
            }

            @Override
            public void doInBackground() {
                try {
                    filterName = Preference.getInstance(ActivityPascaLog.this).getFilterSaved();
                    mLog = Preference.getInstance(ActivityPascaLog.this).getLogTrackerSaved();
                    Collections.reverse(mLog);
                }catch (Exception e) {}
            }

            @Override
            public void onPostExecute() {
                try {
                    if(mLog.size() > 0)
                        tvLoading.setVisibility(View.GONE);

                    if(mAdapter == null) {
                        mAdapter = new LogAdapter(ActivityPascaLog.this, mLog);
                        lv.setAdapter(mAdapter);
                    }else{
                        mAdapter.setItems(mLog);
                        mAdapter.notifyDataSetChanged();
                    }
                    checkFilter();

                    isStillProcessing = false;
                }
                catch (Exception e) {}
            }
        });
        executeParalelPlease(taskExecute2);

    }

    public AdapterView.OnItemClickListener ListenerClickItem = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {

            taskExecute3 = new Helper.GenericAsyncTask(ActivityPascaLog.this);
            taskExecute3.setTaskListener(new Helper.GenericAsyncTask.TaskListener() {

                String log = "";
                String response = "";

                @Override
                public void onPreExecute() {
                    tvLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void doInBackground() {
                    log = mAdapter.getItem(pos).getEvent();

                    if(mAdapter.getItem(pos).getType().equalsIgnoreCase("api")) {
                        try {
                            response = Preference.getInstance(ActivityPascaLog.this).
                                    getResponse(mAdapter.getItem(pos));

                            if (response == null) throw new Exception("no response");

                            log = log + "Response : " + response;
                        } catch (Exception e) {
                            log = log + "Response : Unable to get the response\n\n" + getStackTrace(e);
                        }

                        try {
                            selectedBeanLogApi = ((BeanLog)mAdapter.getItem(pos)).getBeanLogAPI();
                        } catch (Exception e) {}

                    }else if(mAdapter.getItem(pos).getType().equalsIgnoreCase("exception")) {

                        String eMessage;

                        try {
                            eMessage = Preference.getInstance(ActivityPascaLog.this).
                                    getException(mAdapter.getItem(pos));

                            if (eMessage == null) throw new Exception("no exception message");

                            log = log + "Exception : " + eMessage;
                        } catch (Exception e) {
                            log = log + "Unable to get the exception message\n\n" + getStackTrace(e);
                        }
                    }
                }

                @Override
                public void onPostExecute() {
                    tvLoading.setVisibility(View.GONE);
                    tvDetail.setText(log);
                    try {
                        jsonResponse = response;
                    } catch (Exception e) {}
                    SVLogDetail.setVisibility(View.VISIBLE);
                    LLFirst.setVisibility(View.GONE);
                }
            });
            executeParalelPlease(taskExecute3);

        }
    };

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }


    public void goToRetrofitAPITesting(BeanLogAPI beanLogAPI){
        try {
            tvLoading.setVisibility(View.VISIBLE);
            SVLogDetail.setVisibility(View.GONE);
            LLFirst.setVisibility(View.VISIBLE);
            if(beanLogAPI == null) throw new Exception("null");
            Intent intent = new Intent("RetrofitAPITesting");
            Bundle b = new Bundle();
            b.putSerializable("RetrofitAPITesting", beanLogAPI);
            intent.putExtras(b);
            sendBroadcast(intent);
        } catch (Exception e) {
            Toast.makeText(ActivityPascaLog.this, "Error! something ain\'t right", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnClose){
            SVLogDetail.setVisibility(View.GONE);
            LLFirst.setVisibility(View.VISIBLE);
            justGotBackFromCallingRetrofitApiTesting = false;
        }else if(view.getId() == R.id.btnJSONViewer){

            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                JSONViewerActivity.startActivity(ActivityPascaLog.this, jsonObj);
            } catch (Exception e) {
                try {
                    JSONArray jsonObj = new JSONArray(jsonResponse);
                    JSONViewerActivity.startActivity(ActivityPascaLog.this, jsonObj);
                } catch (Exception e1) {
                    Toast.makeText(ActivityPascaLog.this, "Unable to open with JSONViewer", Toast.LENGTH_SHORT).show();
                }
            }

        }else if(view.getId() == R.id.btnSendEmail){
            try {
                Uri path = null;

//                try {
//                    String packageName = "com.iapps.slide.agentapp";
//                    File dataDirectory = Environment.getDataDirectory();
//                    String preferencesPath = "/data/" + packageName + "/shared_prefs/" + Preference.PREF_NAME_NO_CLEAR;
//                    File preferencesFile = new File(dataDirectory, preferencesPath);
//
//                    File extDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//                    File copyPreferencesFile = new File(extDirectory, preferencesPath);
//
//                    copyFile(preferencesFile, copyPreferencesFile);
//
//                    path = Uri.fromFile(copyPreferencesFile);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{Preference.getInstance(ActivityPascaLog.this).getEmailAddressSaved()});
                i.putExtra(Intent.EXTRA_SUBJECT, Preference.getInstance(ActivityPascaLog.this).getEmailSubjectSaved());
                i.putExtra(Intent.EXTRA_TEXT   , tvDetail.getText().toString());

//                if(path != null)
//                i .putExtra(Intent.EXTRA_STREAM, path);

                try {
                    startActivity(Intent.createChooser(i, "Send mail"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ActivityPascaLog.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(ActivityPascaLog.this, "Please set your email address", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void showFilterDialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityPascaLog.this);
        builderSingle.setTitle("Filter by :");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ActivityPascaLog.this,
                android.R.layout.select_dialog_singlechoice);

        arrayAdapter.add("Show ALL");
        arrayAdapter.add("API");
        arrayAdapter.add("CHAT");
        arrayAdapter.add("CRASH");
        arrayAdapter.add("Exception");
        try {
            ArrayList<String> listType = new ArrayList<>();
            for(BeanLog beanLog : mLog){
                listType.add(beanLog.getType());
            }
            Set<String> uniqueType = new HashSet<String>(listType);

            int count = uniqueType.size();
            for(int i = 0; i < count; i++){
                if(uniqueType.contains("Show ALL") || uniqueType.contains("show all")){
                    uniqueType.remove("Show ALL");
                }else if(uniqueType.contains("API") || uniqueType.contains("api")){
                    uniqueType.remove("API");
                }else if(uniqueType.contains("CHAT") || uniqueType.contains("chat")){
                    uniqueType.remove("CHAT");
                }else if(uniqueType.contains("CRASH") || uniqueType.contains("crash")){
                    uniqueType.remove("CRASH");
                }else if(uniqueType.contains("Exception")){
                    uniqueType.remove("Exception");
                }
            }

            for(String type : uniqueType){
                arrayAdapter.add(type);
            }
        } catch (Exception e) {}

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filterName = arrayAdapter.getItem(which);
                Preference.getInstance(ActivityPascaLog.this).filterSaved(filterName);
                checkFilter();
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }


    public void showMenuDialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityPascaLog.this);
        builderSingle.setTitle("Menu :");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ActivityPascaLog.this,
                android.R.layout.select_dialog_singlechoice);

        arrayAdapter.add("Filter");
        arrayAdapter.add("Send Log");
        arrayAdapter.add("Clear log");
        arrayAdapter.add("Enter debug (IDE)");
        arrayAdapter.add("Switch ENV Development");
        arrayAdapter.add("Switch ENV Staging");
        arrayAdapter.add("Switch ENV Production");

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selected = arrayAdapter.getItem(which);

                if(selected.equalsIgnoreCase("filter")){
                    showFilterDialog();
                }else if(selected.equalsIgnoreCase("send log")){

                    Toast.makeText(ActivityPascaLog.this, "Populating data for the last 5 API", Toast.LENGTH_SHORT).show();

                    taskExecute4 = new Helper.GenericAsyncTask(ActivityPascaLog.this);
                    taskExecute4.setTaskListener(new Helper.GenericAsyncTask.TaskListener() {

                        String log = "";
                        String response = "";
                        BeanLog beanLog;

                        @Override
                        public void onPreExecute() {
                            if(tvLoading != null)
                                tvLoading.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void doInBackground() {
                            try {
                                ArrayList<BeanLog> logListTemp = Preference.getInstance(ActivityPascaLog.this).getLogTrackerSaved();
                                ArrayList<BeanLog> logListFilteredAPI =  new ArrayList<BeanLog>();

                                for(int i = 0 ;  i < logListTemp.size(); i++) {
                                    try {
                                        if(logListTemp.get(i).getType().equalsIgnoreCase("api")){
                                            BeanLog beanLog = new BeanLog(logListTemp.get(i).getType(),
                                                    logListTemp.get(i).getEvent(),
                                                    logListTemp.get(i).getTimestamp());
                                            beanLog.setTimestampMilis(logListTemp.get(i).getTimestampMilis());
                                            beanLog.setBeanLogAPI(logListTemp.get(i).getBeanLogAPI());
                                            logListFilteredAPI.add(beanLog);
                                        }
                                    } catch (Exception e) {}
                                }

                                Collections.reverse(logListFilteredAPI);
                                for(int i = 0; i<5;i++) {
                                    try {
                                        beanLog = logListFilteredAPI.get(i);
                                        log = log + beanLog.getEvent();

                                        if (beanLog.getType().equalsIgnoreCase("api")) {
                                            try {
                                                response = Preference.getInstance(ActivityPascaLog.this).
                                                        getResponse(beanLog);

                                                if (response == null)
                                                    throw new Exception("no response");

                                                log = log + "Response : " + response + "\n\n";
                                            } catch (Exception e) {
                                                log = log + "Response : Unable to get the response\n\n" + getStackTrace(e);
                                            }

                                            try {
                                                selectedBeanLogApi = (beanLog).getBeanLogAPI();
                                            } catch (Exception e) {
                                            }

                                        } else if (beanLog.getType().equalsIgnoreCase("exception")) {

                                            String eMessage;

                                            try {
                                                eMessage = Preference.getInstance(ActivityPascaLog.this).
                                                        getException(beanLog);

                                                if (eMessage == null)
                                                    throw new Exception("no exception message");

                                                log = log + "Exception : " + eMessage + "\n\n";
                                            } catch (Exception e) {
                                                log = log + "Unable to get the exception message\n\n" + getStackTrace(e);
                                            }
                                        }
                                    } catch (Exception e) {}
                                }
                            } catch (Exception e) {}
                        }

                        @Override
                        public void onPostExecute() {
                            try {
                                tvDetail.setText(log);
                                btnSendEmail.performClick();
                            } catch (Exception e) {}
                        }
                    });
                    executeParalelPlease(taskExecute4);

                }else if(selected.equalsIgnoreCase("clear")){
                    try {
                        mAdapter = null;
                        mLog = new ArrayList<BeanLog>();
                        Preference.getInstance(ActivityPascaLog.this).saveLogTrackerSaved(mLog);
                    } catch (Exception e) {}
                }else if(selected.equalsIgnoreCase("Enter debug (IDE)")){
                    Debug.waitForDebugger();
                }else if(selected.equalsIgnoreCase("Switch ENV Development")){

                    try {
                        Intent restartIntent = ActivityPascaLog.this.getPackageManager()
                                .getLaunchIntentForPackage(Preference.getInstance(ActivityPascaLog.this).getAppPackageSaved());
                        restartIntent.putExtra("env", "development");
                        restartIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent intent = PendingIntent.getActivity(
                                ActivityPascaLog.this, 0,
                                restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager manager = (AlarmManager) ActivityPascaLog.this.getSystemService(Context.ALARM_SERVICE);
                        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 10, intent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0); //Prevents the service/app from freezing
                    } catch (Exception e) {
                        Toast.makeText(ActivityPascaLog.this, "Please define the app package in the code", Toast.LENGTH_SHORT).show();
                    }

                }else if(selected.equalsIgnoreCase("Switch ENV Staging")){

                    try {
                        Intent restartIntent = ActivityPascaLog.this.getPackageManager()
                                .getLaunchIntentForPackage(Preference.getInstance(ActivityPascaLog.this).getAppPackageSaved());
                        restartIntent.putExtra("env", "staging");
                        restartIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent intent = PendingIntent.getActivity(
                                ActivityPascaLog.this, 0,
                                restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager manager = (AlarmManager) ActivityPascaLog.this.getSystemService(Context.ALARM_SERVICE);
                        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 10, intent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0); //Prevents the service/app from freezing
                    } catch (Exception e) {
                        Toast.makeText(ActivityPascaLog.this, "Please define the app package in the code", Toast.LENGTH_SHORT).show();
                    }

                }else if(selected.equalsIgnoreCase("Switch ENV Production")){

                    try {
                        Intent restartIntent = ActivityPascaLog.this.getPackageManager()
                                .getLaunchIntentForPackage(Preference.getInstance(ActivityPascaLog.this).getAppPackageSaved());
                        restartIntent.putExtra("env", "production");
                        restartIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent intent = PendingIntent.getActivity(
                                ActivityPascaLog.this, 0,
                                restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager manager = (AlarmManager) ActivityPascaLog.this.getSystemService(Context.ALARM_SERVICE);
                        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 10, intent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0); //Prevents the service/app from freezing
                    } catch (Exception e) {
                        Toast.makeText(ActivityPascaLog.this, "Please define the app package in the code", Toast.LENGTH_SHORT).show();
                    }

                }

                dialog.dismiss();
            }
        });
        builderSingle.show();
    }

    public void checkFilter(){
        try {
            mAdapter.getFilter().filter(filterName);
            if(!filterName.equalsIgnoreCase("show all")) {
                tvFilterBy.setVisibility(View.VISIBLE);
                tvFilterBy.setText("Filter ON : " + filterName);
            }else{
                tvFilterBy.setVisibility(View.GONE);
            }
        } catch (Exception e) {}
    }


    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private void executeParalelPlease(AsyncTask<Void,Void,Void> task){
        try {
            task.executeOnExecutor(exService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
