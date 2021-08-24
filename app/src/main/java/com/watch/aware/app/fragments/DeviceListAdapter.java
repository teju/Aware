package com.watch.aware.app.fragments;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bestmafen.baseble.scanner.BleDevice;
import com.watch.aware.app.R;
import com.watch.aware.app.models.BleDevices;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {
    ArrayList<BleDevices> arrayList = new ArrayList<>();
    Context context;
    public DeviceListAdapter(Context context, ArrayList<BleDevices> arrayList) {
        this.arrayList=arrayList;
        this.context=context;
    }


    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView=layoutInflater.inflate(R.layout.device_list_adapter, null);
            TextView tittle=convertView.findViewById(R.id.deviceNAme);
            tittle.setText(arrayList.get(position).getName()+"\n"+arrayList.get(position).getAddress());
        }
        return convertView;
    }

}