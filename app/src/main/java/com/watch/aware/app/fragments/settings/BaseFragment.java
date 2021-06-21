package com.watch.aware.app.fragments.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;


import com.iapps.libs.helpers.BaseHelper;
import com.iapps.logs.com.pascalabs.util.log.helper.Constants;
import com.szabh.smable3.entity.BleActivity;
import com.watch.aware.app.MainActivity;
import com.watch.aware.app.R;
import com.watch.aware.app.callback.NotifyListener;
import com.watch.aware.app.callback.PermissionListener;
import com.watch.aware.app.fragments.dialog.NotifyDialogFragment;
import com.watch.aware.app.helper.DataBaseHelper;
import com.watch.aware.app.helper.Helper;
import com.watch.aware.app.models.BaseParams;
import com.watch.aware.app.models.Steps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class BaseFragment extends GenericFragment {

    List<String> permissionsThatNeedTobeCheck = new ArrayList<>();
    public PermissionListener permissionListener;
    public View  v = null;
    public BaseParams baseParams = new BaseParams();
    public int selectedTabPos = 0;
    public void onBackTriggered() {
        home().proceedDoOnBackPressed();
    }
    public static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 911;

    public MainActivity home() {
        return (MainActivity)getActivity();
    }

    @NonNull
    public Observer obsNoInternet = (Observer)(new Observer() {
        // $FF: synthetic method
        // $FF: bridge method
        public void onChanged(Object var1) {
            this.onChanged((Boolean)var1);
        }

        public final void onChanged(Boolean isHaveInternet) {
            try {
                if (!isHaveInternet) {
                    if (BaseFragment.this.getActivity() == null) {
                        return;
                    }
                    BaseFragment.this.showNotifyDialog(getActivity().getString(R.string.no_internet),
                            getString(R.string.no_connection), "OK",
                            "", (NotifyListener)(new NotifyListener() {
                        public void onButtonClicked(int which) {
                        }
                    }));
                }
            } catch (Exception var3) {
            }

        }
    });
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.BLUETOOTH);
        checkPermissions(permissions, permissionListener);
    }

    public void checkPermissions(List<String> permissionsThatNeedTobeCheck, PermissionListener permissionListener) {
        this.permissionsThatNeedTobeCheck = permissionsThatNeedTobeCheck;
        this.permissionListener = permissionListener;
        ArrayList<String> permissionsNeeded =new  ArrayList<String>();
        List<String> permissionsList = permissionsThatNeedTobeCheck;
        try {
            for (String s : permissionsThatNeedTobeCheck) {
                if (s.equals(Manifest.permission.CAMERA)) {
                    if (!addPermission(permissionsList, Manifest.permission.CAMERA))
                        permissionsNeeded.add("Camera");
                    else if (s.equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
                            permissionsNeeded.add("ACCESS COARSE LOCATION");
                    } else if (s.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                            permissionsNeeded.add("ACCESS FINE LOCATION");
                    }
                }
            }
        } catch (Exception e){

        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                ActivityCompat.requestPermissions(
                        getActivity(),
                        permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                return;
            }
            ActivityCompat.requestPermissions(
                    getActivity(), permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        } else {
            permissionListener.onPermissionAlreadyGranted();
        }
    }

    public Boolean addPermission(List<String> permissionsList,String permission)  {
        if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission))
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {

                Boolean isAllGranted = false;
                int index = 0;
                for (String permission : permissionsThatNeedTobeCheck) {
                    if (permission.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false;
                            break;
                        } else {
                            isAllGranted = true;
                        }
                    } else if (permission.equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false;
                            break;
                        } else {
                            isAllGranted = true;
                        }
                    }

                }
                if (isAllGranted) {
                    permissionListener.onCheckPermission(permissions[index], true);
                } else {
                    permissionListener.onCheckPermission(permissions[index], false);
                }
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

   /* public void setBackButtonToolbarStyleOne(View v) {
        try {
            RelativeLayout llBack = v.findViewById(R.id.llBack);

                    llBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            home().onBackPressed();
                        }
                    });
        } catch (Exception e) {
        }
    }*/
    public void showNotifyDialog(String tittle, String messsage, String button_positive, String button_negative, NotifyListener n) {
        NotifyDialogFragment f = new NotifyDialogFragment();
        f.listener = n;

        f.notify_tittle = tittle;
        f.notify_messsage = messsage;
        f.button_positive = button_positive;
        f.button_negative = button_negative;
        f.setCancelable(false);
        if(!Helper.isEmpty(tittle) || !Helper.isEmpty(messsage)) {
            f.show(getActivity().getSupportFragmentManager(), NotifyDialogFragment.TAG);
        }
    }
    public void insertStepData(List<BleActivity> activities) {

        DataBaseHelper dataBaseHelper =new  DataBaseHelper(getActivity());
        dataBaseHelper.stepsInsert(dataBaseHelper, String.valueOf(activities.get(0).getMStep() - getLastHRSteps()),
                BaseHelper.parseDate(new Date(), Constants.DATE_JSON),String.valueOf(activities.get(0).getMDistance()/10000),
                String.valueOf(activities.get(0).getMCalorie()/10000), BaseHelper.parseDate(new Date(),Constants.TIME_JSON_HM));
    }
    public int getLastHRSteps() {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getActivity());
        List<Steps> dteps = dataBaseHelper.getAllSteps(
                "WHERE time <= " + (Integer.parseInt(BaseHelper.parseDate(new Date(),Constants.TIME_hA)) - 1)
                        + " AND date is  (" + BaseHelper.parseDate(
                        new Date(), Constants.DATE_JSON) + ") ORDER BY stepsCount DESC");
        int stepsCnt = 0;

        if(dteps.size() > 0) {
            stepsCnt = Integer.parseInt(dteps.get(0).getStepCount());
        }
        return  stepsCnt;
    }
}