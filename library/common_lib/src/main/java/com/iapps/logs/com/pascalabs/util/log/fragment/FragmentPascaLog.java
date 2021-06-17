package com.pascalabs.util.log.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.iapps.common_library.R;
import com.iapps.logs.com.pascalabs.util.log.helper.EasyTimer;
import com.iapps.logs.com.pascalabs.util.log.helper.Preference;
import com.iapps.logs.com.pascalabs.util.log.model.BeanLog;
import com.pascalabs.util.log.adapter.LogAdapter;

import java.util.ArrayList;
import java.util.Collections;


public class FragmentPascaLog
	extends Fragment implements View.OnClickListener {

	private ListView lv;
    private Button btnClear;
    private Button btnSendEmail;
    private TextView tvDetail;
    private TextView tvInfoClose;
    private Button btnClose;
    private ScrollView SVLogDetail;
    private TextView tvLoading;
    private LinearLayout LLFirst;

    EasyTimer refreshTimerTask = new EasyTimer();

	private LogAdapter mAdapter;
	private ArrayList<BeanLog> mLog = new ArrayList<BeanLog>();

    private boolean toogleLogProcess = false;
    private View v;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_log, container, false);
		return v;
	}

    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        lv = (ListView) v.findViewById(R.id.lv);
        btnSendEmail = (Button) v.findViewById(R.id.btnSendEmail);
        tvDetail = (TextView) v.findViewById(R.id.tvDetail);
        tvInfoClose = (TextView) v.findViewById(R.id.tvInfoClose);
        SVLogDetail = (ScrollView) v.findViewById(R.id.SVLogDetail);
        btnClose = (Button) v.findViewById(R.id.btnClose);
        tvLoading = (TextView) v.findViewById(R.id.tvLoading);
        LLFirst = (LinearLayout) v.findViewById(R.id.LLFirst);

        lv.setOnItemClickListener(ListenerClickItem);
        btnClear.setOnClickListener(this);
        btnSendEmail.setOnClickListener(this);
        tvInfoClose.setOnClickListener(this);
        btnClose.setOnClickListener(this);

	}

    @Override
    public void onResume() {
        super.onResume();
        try {
            refreshTimerTask.setOnTaskRunListener(new EasyTimer.OnTaskRunListener() {
                @Override
                public void onTaskRun(long past_time, String rendered_time) {
                    try {
                        loadLogData();
                    } catch (Exception e) {}
                }
            });

            refreshTimerTask.start();
        } catch (Exception e) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            refreshTimerTask.stop();
        } catch (Exception e) {}
    }

	public void loadLogData() {
        if(toogleLogProcess) return;

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                tvLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    mLog = Preference.getInstance(getActivity()).getLogTrackerSaved();
                    Collections.reverse(mLog);
                }catch (Exception e) {}
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                tvLoading.setVisibility(View.GONE);
                try {
                    mAdapter.setItems(mLog);
                    mAdapter.notifyDataSetChanged();
                    lv.invalidateViews();
                }
                catch (Exception e) {}
            }
        }.execute();

	}


	public OnItemClickListener ListenerClickItem = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
            tvDetail.setText( mLog.get(pos).getEvent());
            SVLogDetail.setVisibility(View.VISIBLE);
            LLFirst.setVisibility(View.GONE);
        }
	};


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnClose){
            SVLogDetail.setVisibility(View.GONE);
            LLFirst.setVisibility(View.VISIBLE);
        }else if(view.getId() == R.id.btnSendEmail){
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{Preference.getInstance(getActivity()).getEmailAddressSaved()});
                i.putExtra(Intent.EXTRA_SUBJECT, Preference.getInstance(getActivity()).getEmailSubjectSaved());
                i.putExtra(Intent.EXTRA_TEXT   , tvDetail.getText().toString());
                try {
                    startActivity(Intent.createChooser(i, "Send mail"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Please set your email address", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
