package com.watch.aware.app.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.iapps.libs.helpers.BaseHelper
import com.iapps.logs.com.pascalabs.util.log.helper.Constants
import com.watch.aware.app.R
import com.watch.aware.app.callback.NotifyListener
import com.watch.aware.app.fragments.settings.BaseFragment
import com.watch.aware.app.helper.Constants.Companion.COUGH
import com.watch.aware.app.helper.Constants.Companion.HR
import com.watch.aware.app.helper.Constants.Companion.SPO2
import com.watch.aware.app.helper.Constants.Companion.TIMEFORMAT
import com.watch.aware.app.helper.Constants.Companion.Temp
import com.watch.aware.app.helper.DataBaseHelper
import com.watch.aware.app.helper.MyBroadcastReceiver
import com.watch.aware.app.helper.UserInfoManager
import com.watch.aware.app.models.HeartRate
import com.watch.aware.app.models.SpoRate
import com.watch.aware.app.models.TempRate
import com.watch.aware.app.webservices.PostCovidStatusDataViewModel
import com.watch.aware.app.webservices.PostRegisterViewModel
import com.watch.aware.app.webservices.PostUpdateProfileModel
import com.yc.pedometer.info.*
import com.yc.pedometer.listener.OxygenRealListener
import com.yc.pedometer.listener.RateCalibrationListener
import com.yc.pedometer.listener.TemperatureListener
import com.yc.pedometer.listener.TurnWristCalibrationListener
import com.yc.pedometer.sdk.*
import com.yc.pedometer.update.Updates
import com.yc.pedometer.utils.*
import kotlinx.android.synthetic.main.fragment_welness.*
import java.util.*
import kotlin.collections.ArrayList


class WelnessFragment : BaseFragment() , ICallback, ServiceStatusCallback,
    OnServerCallbackListener, TemperatureListener, OxygenRealListener, RateCalibrationListener, TurnWristCalibrationListener{

    private var ble_connecte = false
    private var heartRates: List<HeartRate> = ArrayList()
    private var spoRates: List<SpoRate> = ArrayList()
    private var tempRates: List<TempRate> = ArrayList()

    protected val TAG = "WelnessFragment"

    lateinit var postGetCovidStatusDataViewModel: PostCovidStatusDataViewModel
    lateinit var postUpdateProfileModel: PostUpdateProfileModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_welness, container, false)
        return v;
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setGetCovidStatusDataAPIObserver()
            setUpdateProfileAPIObserver()
            syncing.visibility = View.VISIBLE
            if (UserInfoManager.getInstance(activity!!).getISFirstTime()) {
                postUpdateProfileModel.loadData(
                    UserInfoManager.getInstance(activity!!).getAccountName(),
                    UserInfoManager.getInstance(activity!!).getAge(),
                    UserInfoManager.getInstance(activity!!).getEmail(),
                    UserInfoManager.getInstance(activity!!).getContactNumber(),
                    UserInfoManager.getInstance(activity!!).getGEnder(),
                    UserInfoManager.getInstance(activity!!).getEmail()
                )
            }
            startAlert()
            welcome.text =
                "Welcome back, " + UserInfoManager.getInstance(activity!!).getAccountName()
            if (UserInfoManager.getInstance(activity!!).getGEnder().contentEquals("F")) {
                human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_female))
            } else {
                human.setImageDrawable(activity?.resources?.getDrawable(R.drawable.human_male))
            }
            swiperefresh_items.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {})


            refresh.setOnClickListener {
                mWriteCommand?.syncAllSleepData()
            }
            // cyyonkm2kip25hvyvq34p7775d6yxxwwjbrsazad2ubvzaldkxaq
            mySQLOperate = UTESQLOperate.getInstance(activity) // 2.2.1版本修改
            mBLEServiceOperate = BLEServiceOperate.getInstance(activity)

            mBLEServiceOperate?.setServiceStatusCallback(this)

            // 如果没在搜索界面提前实例BLEServiceOperate的话，下面这4行需要放到OnServiceStatuslt
            mContext = activity
            LogUtils.setLogEnable(true) //是否开启log

            mySQLOperate = UTESQLOperate.getInstance(mContext) // 2.2.1版本修改

            mBLEServiceOperate = BLEServiceOperate.getInstance(mContext)
            mBLEServiceOperate?.setServiceStatusCallback(this)
            LogUtils.d(TAG, "setServiceStatusCallback后 mBLEServiceOperate =$mBLEServiceOperate")
            // 如果没在搜索界面提前实例BLEServiceOperate的话，下面这4行需要放到OnServiceStatuslt
            // 如果没在搜索界面提前实例BLEServiceOperate的话，下面这4行需要放到OnServiceStatuslt
            mBluetoothLeService = mBLEServiceOperate?.getBleService()
            if (mBluetoothLeService != null) {
                mBluetoothLeService!!.setICallback(this)
                mBluetoothLeService!!.setRateCalibrationListener(this) //设置心率校准监听
                mBluetoothLeService!!.setTurnWristCalibrationListener(this) //设置翻腕校准监听
                mBluetoothLeService!!.setTemperatureListener(this) //设置体温测试，采样数据回调
                mBluetoothLeService!!.setOxygenListener(this) //Oxygen Listener
            }

            mDataProcessing = DataProcessing.getInstance(mContext)
            mDataProcessing?.setOnRateListener(mOnRateListener)
            mDataProcessing?.setOnRateOf24HourListenerRate(mOnRateOf24HourListener)


            mWriteCommand = WriteCommandToBLE.getInstance(mContext)
            mUpdates = Updates.getInstance(mContext)
            mUpdates?.setHandler(mHandler) // 获取升级操作信息

            mUpdates?.registerBroadcastReceiver()
            mUpdates?.setOnServerCallbackListener(this)
            LogUtils.d(
                TAG, "MainActivity_onCreate   mUpdates  ="
                        + mUpdates
            )

            mBLEServiceOperate?.connect(UserInfoManager.getInstance(activity!!).getDeviceID())

            CURRENT_STATUS = CONNECTING
            setTempData()
            setSPoData()
            setHeartData()
            val connected = SPUtil.getInstance(activity?.getApplicationContext()).bleConnectStatus

            info_txt.text = "Please wait ..."
        } catch (e:Exception){

        }
    }


    fun onConnected() {

        try {

            refresh.performClick();

            swiperefresh_items.setRefreshing(false);

            if (heartRates.size != 0  && tempRates.size != 0 ) {
                postGetCovidStatusDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getEmail())
            } else {
                info_txt.text = "Please wait, data being transferred ..."
                info_txt.setTextColor(activity?.resources?.getColor(R.color.DarkGray)!!)
            }

            syncing.visibility = View.GONE
            if(COUGH == 1) {
                tvcough.setText("Yes")
                tvcough.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
            } else {
                tvcough.setText("NO")
                tvcough.setTextColor(activity?.resources?.getColor(R.color.Black)!!)
            }
        } catch (e:Exception){
            e.toString()
        }
    }

  
    fun setHeartData() {
        val db = DataBaseHelper(activity!!)
        heartRates = db.getAllHeartRate("Where heartRate != 0  ORDER by Id DESC")
        if(heartRates.size != 0) {
            oxygen_level.text = "96"
            val lastsynced = BaseHelper.parseDate(heartRates.get(0).time, Constants.TIME_JSON_HM)
            val today_date = BaseHelper.parseDate(Date(),Constants.DATE_JSON)
            val diffDays = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_JSON),
                BaseHelper.parseDate(heartRates.get(0).date,Constants.DATE_JSON))
            if(diffDays?.days?.toInt() == 0) {
                last_synced.text = BaseHelper.parseDate(lastsynced, TIMEFORMAT)
            } else if(diffDays.days?.toInt() == 1) {
                last_synced.text = "Yesterday"
            }
            else {
                last_synced.text = heartRates.get(0).date
            }
            heart_rate.text = heartRates.get(0).heartRate.toString()
            HR = heartRates.get(0).heartRate.toInt()
        }
    }

    fun setTempData() {
        val db = DataBaseHelper(activity!!)
        tempRates = db.getAllTemp("Where TempRate != 0 ORDER by Id DESC")
        if(tempRates.size != 0) {
            try {
                val lastsynced = BaseHelper.parseDate(tempRates.get(0).time, Constants.TIME_JSON_HM)
                val today_date = BaseHelper.parseDate(Date(),Constants.DATE_JSON)
                val diffDays = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_JSON),
                    BaseHelper.parseDate(tempRates.get(0).date,Constants.DATE_JSON))
                if(diffDays?.days?.toInt() == 0) {
                    last_synced.text = BaseHelper.parseDate(lastsynced, TIMEFORMAT)
                } else if(diffDays.days?.toInt() == 1) {
                    last_synced.text = "Yesterday"
                }
                else {
                    last_synced.text = tempRates.get(0).date
                }
            }catch (e:Exception){
                e.printStackTrace()

            }
            temp.text = String.format("%.1f",tempRates.get(0).tempRate)
            Temp = tempRates.get(0).tempRate.toDouble()
        }
    }

    fun setSPoData() {
        val db = DataBaseHelper(activity!!)
        spoRates = db.getAllSpoRate("Where SpoRate != 0 ORDER BY Id DESC")
        if(spoRates.size != 0) {
            try {
                val lastsynced = BaseHelper.parseDate(spoRates.get(0).time, Constants.TIME_JSON_HM)
                val today_date = BaseHelper.parseDate(Date(),Constants.DATE_JSON)
                val diffDays = BaseHelper.printDifference(BaseHelper.parseDate(today_date,Constants.DATE_JSON),
                    BaseHelper.parseDate(spoRates.get(0).date,Constants.DATE_JSON))
                if(diffDays?.days?.toInt() == 0) {
                    last_synced.text = BaseHelper.parseDate(lastsynced, TIMEFORMAT)
                } else if(diffDays.days?.toInt() == 1) {
                    last_synced.text = "Yesterday"
                }
                else {
                    last_synced.text = spoRates.get(0).date
                }
            }catch (e:Exception){
                e.printStackTrace()

            }
            oxygen_level.text = spoRates.get(0).spoRate.toString()
            SPO2 = spoRates.get(0).spoRate
        }
    }

    fun setGetCovidStatusDataAPIObserver() {
        postGetCovidStatusDataViewModel = ViewModelProviders.of(this).get(PostCovidStatusDataViewModel::class.java).apply {
            this@WelnessFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                     //   syncing.visibility = View.VISIBLE
                    } else {
                        syncing.visibility = View.GONE
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet as Observer<in Boolean>)
                getTrigger().observe(thisFragReference, Observer { state ->

                    when (state) {
                        PostCovidStatusDataViewModel.NEXT_STEP -> {
                            when(postGetCovidStatusDataViewModel.obj?.CovidPrediction) {
                                "G" ->{
                                    info_txt.text = "Your wellness data\nseems ok !"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.colorAccent)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                                "Y" -> {
                                    info_txt.text = "There is some issue\nwith your wellness data"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.DarkOrange)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.DarkOrange), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                                "R" -> {
                                    info_txt.text = "Please contact\nyour doctor"
                                    info_txt.setTextColor(activity?.resources?.getColor(R.color.Red)!!)
                                    circle.setColorFilter(ContextCompat.getColor(activity!!, R.color.Red), android.graphics.PorterDuff.Mode.SRC_IN);

                                }
                            }
                        }
                    }
                })

            }
        }
    }

    fun setUpdateProfileAPIObserver() {
        postUpdateProfileModel = ViewModelProviders.of(this).get(PostUpdateProfileModel::class.java).apply {
           this@WelnessFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet as Observer<in Boolean>)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostUpdateProfileModel.NEXT_STEP -> {
                            UserInfoManager.getInstance(activity!!).saveIsFirstTime(false)

                        }
                        PostRegisterViewModel.ERROR -> {
                        }
                    }
                })
            }
        }
    }

   

    fun startAlert() {
        val intent = Intent(activity, MyBroadcastReceiver::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            activity?.getApplicationContext(), 234324243, intent, 0)
        val alarmManager: AlarmManager? = activity?.getSystemService(ALARM_SERVICE) as AlarmManager?
        alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),1*60*1000, pendingIntent);

    }


    private val mOnRateListener =
        RateChangeListener { rate, status ->

                tempRate = rate
                tempStatus = status
                LogUtils.i(TAG, "Rate_tempRate =$tempRate")
                mHandler.sendEmptyMessage(UPDATA_REAL_RATE_MSG)
                activity?.runOnUiThread {
                    try {
                    heartRateInsert(rate)
                    setHeartData()
                    heart_rate.text = "" + tempRate.toString()
                    HR = rate
                    onConnected()
                } catch (e:java.lang.Exception) {

                }
            }
        }

    private val mOnRateOf24HourListener =
        RateOf24HourRealTimeListener { maxHeartRateValue, minHeartRateValue, averageHeartRateValue, isRealTimeValue -> //监听24小时心率手环的最大、最小、平均值。需要手环端进入到心率测试界面（或者调用同步方法后）才会出值

        }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            try {
                when (msg.what) {
                    RATE_SYNC_FINISH_MSG -> {
                        UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0))
                    }
                    RATE_OF_24_HOUR_SYNC_FINISH_MSG -> {
                        mySQLOperate!!.query24HourRateAllInfo()
                    }
                    UPDATA_REAL_RATE_MSG ->
                        if (tempStatus == GlobalVariable.RATE_TEST_FINISH) {
                            UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0))
                        }


                    UPDATE_SLEEP_UI_MSG -> LogUtils.d(
                        TAG,
                        "UPDATE_SLEEP_UI_MSG"
                    )
                    GlobalVariable.START_PROGRESS_MSG -> {
                        LogUtils.i(
                            TAG,
                            "(Boolean) msg.obj=" + msg.obj as Boolean
                        )
                        isUpdateSuccess = msg.obj as Boolean
                        LogUtils.i(
                            TAG,
                            "BisUpdateSuccess=$isUpdateSuccess"
                        )
                        this.postDelayed(mDialogRunnable!!, TIME_OUT)
                    }
                    GlobalVariable.DOWNLOAD_IMG_FAIL_MSG -> {
                        if (mDialogRunnable != null) this.removeCallbacks(mDialogRunnable)
                    }
                    GlobalVariable.DISMISS_UPDATE_BLE_DIALOG_MSG -> {
                        LogUtils.i(
                            TAG,
                            "(Boolean) msg.obj=" + msg.obj as Boolean
                        )
                        isUpdateSuccess = msg.obj as Boolean
                        LogUtils.i(
                            TAG,
                            "BisUpdateSuccess=$isUpdateSuccess"
                        )
                        if (mDialogRunnable != null) {
                            this.removeCallbacks(mDialogRunnable)
                        }
                        if (isUpdateSuccess) {
                        }
                    }
                    GlobalVariable.SERVER_IS_BUSY_MSG -> {

                    }
                    DISCONNECT_MSG -> {
                        try {
                            connection_status.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.close_circle,
                                0
                            );
                            connection_status.setText(getString(R.string.disconnect))

                            CURRENT_STATUS = DISCONNECTED

                            val connectResute0 = mBLEServiceOperate?.connect(
                                UserInfoManager.getInstance(activity!!).getDeviceID()
                            )
                            LogUtils.i(
                                TAG,
                                "connectResute0=$connectResute0"
                            )

                        } catch (e: Exception) {

                        }
                    }
                    CONNECTED_MSG -> {
                        try {
                            connection_status.setText(getString(R.string.connected))
                            connection_status.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.check_circle,
                                0
                            );
                        } catch (e: java.lang.Exception) {

                        }
                        mBluetoothLeService!!.setRssiHandler(this)
                        Thread(Runnable {
                            while (!Thread.interrupted()) {
                                try {
                                    Thread.sleep(1000)
                                } catch (e: InterruptedException) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace()
                                }
                                if (mBluetoothLeService != null) {
                                    mBluetoothLeService!!.readRssi()
                                }
                            }
                        }).start()
                        CURRENT_STATUS = CONNECTED

                        onConnected()
                    }
                    GlobalVariable.UPDATE_BLE_PROGRESS_MSG -> {
                        val schedule = msg.arg1
                        LogUtils.i("zznkey", "schedule =$schedule")
                    }
                    OPEN_CHANNEL_OK_MSG -> {
                        resultBuilder.append(
                            resources.getString(
                                R.string.open_channel_ok
                            ) + ","
                        )
                        mWriteCommand!!.sendAPDUToBLE(
                            WriteCommandToBLE
                                .hexString2Bytes(testKey1)
                        )
                    }
                    CLOSE_CHANNEL_OK_MSG -> resultBuilder.append(
                        resources.getString(
                            R.string.close_channel_ok
                        ) + ","
                    )
                    TEST_CHANNEL_OK_MSG -> {
                        resultBuilder.append(
                            resources.getString(
                                R.string.test_channel_ok
                            ) + ","
                        )
                        mWriteCommand!!.closeBLEchannel()
                    }
                    SERVER_CALL_BACK_OK_MSG -> {
                        if (mDialogServerRunnable != null) {
                            this.removeCallbacks(mDialogServerRunnable)
                        }
                        val localVersion =
                            SPUtil.getInstance(mContext).imgLocalVersion
                        val status = mUpdates!!.getBLEVersionStatus(localVersion)
                        LogUtils.i(
                            TAG,
                            "固件升级 VersionStatus =$status"
                        )
                        if (status == GlobalVariable.OLD_VERSION_STATUS) {
                        } else if (status == GlobalVariable.NEWEST_VERSION_STATUS) {

                        }
                    }
                    OFFLINE_SKIP_SYNC_OK_MSG -> {

                    }
                    test_mag1 -> {

                    }
                    test_mag2 -> {
                    }
                    OFFLINE_STEP_SYNC_OK_MSG -> {
                    }
                    UPDATE_SPORTS_TIME_DETAILS_MSG -> {
                    }
                    UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG -> {
                    }
                    UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG -> {
                    }
                    UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG -> {
                    }
                    UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL_MSG -> {
                    }
                    BIND_CONNECT_SEND_ACCOUNT_ID_MSG -> mWriteCommand!!.sendAccountId(1234)
                    else -> {
                    }
                }
            } catch (e:java.lang.Exception){

            }
        }
    }

    /*
	 * 获取一天最新心率值、最高、最低、平均心率值
	 */
    private fun UpdateUpdataRateMainUI(calendar: String) {
        // UTESQLOperate mySQLOperate = UTESQLOperate.getInstance(mContext);
        val mRateOneDayInfo = mySQLOperate?.queryRateOneDayMainInfo(calendar)
        if (mRateOneDayInfo != null) {
            val currentRate = mRateOneDayInfo.currentRate
            val lowestValue = mRateOneDayInfo.lowestRate
            val averageValue = mRateOneDayInfo.verageRate
            val highestValue = mRateOneDayInfo.highestRate
            // current_rate.setText(currentRate + "");
        }
    }


    private val mDialogRunnable: Runnable? = object : Runnable {
        override fun run() {

            // mDownloadButton.setText(R.string.suota_update_succeed);
            mHandler.removeCallbacks(this)
            if (!isUpdateSuccess) {

                mUpdates!!.clearUpdateSetting()
            } else {
                isUpdateSuccess = false

            }
        }
    }
    private val mDialogServerRunnable: Runnable? = object : Runnable {
        override fun run() {

            // mDownloadButton.setText(R.string.suota_update_succeed);
            mHandler.removeCallbacks(this)

        }
    }



    /**
     * 获取某一天睡眠详细，并更新睡眠UI CalendarUtils.getCalendar(0)代表今天，也可写成"20141101"
     * CalendarUtils.getCalendar(-1)代表昨天，也可写成"20141031"
     * CalendarUtils.getCalendar(-2)代表前天，也可写成"20141030" 以此类推
     */



    override fun OnResult(result: Boolean, status: Int) {
        try {
            LogUtils.i(
                TAG,
                "result=$result,status=$status"
            )
            when (status) {
                ICallbackStatus.OFFLINE_STEP_SYNC_OK -> mHandler.sendEmptyMessage(
                    OFFLINE_STEP_SYNC_OK_MSG
                )
                ICallbackStatus.OFFLINE_SLEEP_SYNC_OK -> {
                }
                ICallbackStatus.SYNC_TIME_OK -> {
                }
                ICallbackStatus.GET_BLE_VERSION_OK -> {
                }
                ICallbackStatus.DISCONNECT_STATUS -> mHandler.sendEmptyMessage(DISCONNECT_MSG)
                ICallbackStatus.CONNECTED_STATUS -> {
                    mHandler.sendEmptyMessage(CONNECTED_MSG)
                    mHandler.postDelayed({
                        //                        mWriteCommand.sendToQueryPasswardStatus();
                    }, 600) // 2.2.1版本修改
                }
                ICallbackStatus.DISCOVERY_DEVICE_SHAKE -> LogUtils.d(
                    TAG,
                    "摇一摇拍照"
                )
                ICallbackStatus.OFFLINE_RATE_SYNC_OK -> mHandler.sendEmptyMessage(
                    RATE_SYNC_FINISH_MSG
                )
                ICallbackStatus.OFFLINE_24_HOUR_RATE_SYNC_OK -> mHandler.sendEmptyMessage(
                    RATE_OF_24_HOUR_SYNC_FINISH_MSG
                )
                ICallbackStatus.SET_METRICE_OK -> {
                }
                ICallbackStatus.SET_INCH_OK -> {
                }
                ICallbackStatus.SET_FIRST_ALARM_CLOCK_OK -> {
                }
                ICallbackStatus.SET_SECOND_ALARM_CLOCK_OK -> {
                }
                ICallbackStatus.SET_THIRD_ALARM_CLOCK_OK -> {
                }
                ICallbackStatus.SEND_PHONE_NAME_NUMBER_OK -> mWriteCommand!!.sendQQWeChatVibrationCommand(
                    5
                )
                ICallbackStatus.SEND_QQ_WHAT_SMS_CONTENT_OK -> mWriteCommand!!.sendQQWeChatVibrationCommand(
                    1
                )
                ICallbackStatus.PASSWORD_AUTHENTICATION_OK -> LogUtils.d(
                    TAG,
                    "验证成功或者设置密码成功"
                )
                ICallbackStatus.OFFLINE_SWIM_SYNCING -> LogUtils.d(
                    TAG,
                    "游泳数据同步中"
                )
                ICallbackStatus.OFFLINE_SWIM_SYNC_OK -> {
                    LogUtils.d(TAG, "游泳数据同步完成")
                    mHandler.sendEmptyMessage(OFFLINE_SWIM_SYNC_OK_MSG)
                }
                ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNCING -> LogUtils.d(
                    TAG,
                    "血压数据同步中"
                )
                ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK -> {
                    LogUtils.d(TAG, "血压数据同步完成")
                    mHandler.sendEmptyMessage(OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG)
                }
                ICallbackStatus.OFFLINE_SKIP_SYNCING -> LogUtils.d(
                    TAG,
                    "跳绳数据同步中"
                )
                ICallbackStatus.OFFLINE_SKIP_SYNC_OK -> {
                    LogUtils.d(TAG, "跳绳数据同步完成")
                    mHandler.sendEmptyMessage(OFFLINE_SKIP_SYNC_OK_MSG)
                }
                ICallbackStatus.MUSIC_PLAYER_START_OR_STOP -> LogUtils.d(
                    TAG,
                    "音乐播放/暂停"
                )
                ICallbackStatus.MUSIC_PLAYER_NEXT_SONG -> LogUtils.d(
                    TAG,
                    "音乐下一首"
                )
                ICallbackStatus.MUSIC_PLAYER_LAST_SONG -> LogUtils.d(
                    TAG,
                    "音乐上一首"
                )
                ICallbackStatus.OPEN_CAMERA_OK -> LogUtils.d(
                    TAG,
                    "打开相机ok"
                )
                ICallbackStatus.CLOSE_CAMERA_OK -> LogUtils.d(
                    TAG,
                    "关闭相机ok"
                )
                ICallbackStatus.PRESS_SWITCH_SCREEN_BUTTON -> {
                    LogUtils.d(TAG, "表示按键1短按下，用来做切换屏,表示切换了手环屏幕")
                    mHandler.sendEmptyMessage(test_mag1)
                }
                ICallbackStatus.PRESS_END_CALL_BUTTON -> LogUtils.d(
                    TAG,
                    "表示按键1长按下，一键拒接来电"
                )
                ICallbackStatus.PRESS_TAKE_PICTURE_BUTTON -> LogUtils.d(
                    TAG,
                    "表示按键2短按下，用来做一键拍照"
                )
                ICallbackStatus.PRESS_SOS_BUTTON -> {
                    LogUtils.d(TAG, "表示按键3短按下，用来做一键SOS")
                    mHandler.sendEmptyMessage(test_mag2)
                }
                ICallbackStatus.PRESS_FIND_PHONE_BUTTON -> LogUtils.d(
                    TAG,
                    "表示按键按下，手环查找手机的功能。"
                )
                ICallbackStatus.READ_ONCE_AIR_PRESSURE_TEMPERATURE_SUCCESS -> LogUtils.d(
                    TAG,
                    "读取当前气压传感器气压值和温度值成功，数据已保存到数据库，查询请调用查询数据库接口，返回的数据中，最新的一条为本次读取的数据"
                )
                ICallbackStatus.SYNC_HISORY_AIR_PRESSURE_TEMPERATURE_SUCCESS -> LogUtils.d(
                    TAG,
                    "同步当天历史数据成功，包括气压传感器气压值和温度值，数据已保存到数据库，查询请调用查询数据库接口"
                )
                ICallbackStatus.SYNC_HISORY_AIR_PRESSURE_TEMPERATURE_FAIL -> LogUtils.d(
                    TAG,
                    "同步当天历史数据失败，数据不保存"
                )
                ICallbackStatus.START_BREATHE_COMMAND_OK -> {
                    LogUtils.d(TAG, "开启测试呼吸率")
                    BreatheUtil.LogI("开启测试呼吸率")
                }
                ICallbackStatus.STOP_BREATHE_COMMAND_OK -> BreatheUtil.LogI("关闭测试呼吸率")
                ICallbackStatus.QUERY_CURRENT_BREATHE_COMMAND_OK -> BreatheUtil.LogI("获取当前呼吸率测试状态")
                ICallbackStatus.BREATHE_DATA_SYNCING -> BreatheUtil.LogI("同步呼吸率数据中")
                ICallbackStatus.SYNC_BREATHE_COMMAND_OK -> BreatheUtil.LogI("同步呼吸率数据完成")
                ICallbackStatus.SET_BREATHE_AUTOMATIC_TEST_COMMAND_OK -> BreatheUtil.LogI("设置呼吸率自动测试完成")
                ICallbackStatus.SYNC_BREATHE_TIME_PERIOD_COMMAND_OK -> BreatheUtil.LogI("设置呼吸率时间段和开关")
                ICallbackStatus.READ_CHAR_SUCCESS -> {
                }
                ICallbackStatus.BIND_CONNECT_SEND_ACCOUNT_ID -> {
                    LogUtils.d(TAG, "发送用户ID")
                    mHandler.sendEmptyMessage(BIND_CONNECT_SEND_ACCOUNT_ID_MSG)
                }
                ICallbackStatus.BIND_CONNECT_COMPARE_SUCCESS -> LogUtils.d(
                    TAG,
                    "绑定成功"
                )
                ICallbackStatus.BIND_CONNECT_BAND_CLICK_CONFIRM -> LogUtils.d(
                    TAG,
                    "手环点击 确认 按钮"
                )
                ICallbackStatus.BIND_CONNECT_VALID_ID -> LogUtils.d(
                    TAG,
                    "手环已经存在有效ID"
                )
                ICallbackStatus.BIND_CONNECT_IDVALID_ID -> LogUtils.d(
                    TAG,
                    "手环不存在有效ID"
                )
                ICallbackStatus.BIND_CONNECT_BAND_CLICK_CANCEL -> LogUtils.d(
                    TAG,
                    "手环点击 取消 按钮"
                )
                else -> {
                }
            }
        }catch (e:java.lang.Exception){

        }
    }


    override fun OnDataResult(
        result: Boolean,
        status: Int,
        data: ByteArray?
    ) {
        try {
            var stringBuilder: StringBuilder? = null
            if (data != null && data.size > 0) {
                stringBuilder = StringBuilder(data.size)
                for (byteChar in data) {
                    stringBuilder.append(String.format("%02X", byteChar))
                }
                LogUtils.i(
                    TAG,
                    "BLE---->APK data =$stringBuilder"
                )
            }
            when (status) {
                ICallbackStatus.OPEN_CHANNEL_OK -> mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG)
                ICallbackStatus.CLOSE_CHANNEL_OK -> mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG)
                ICallbackStatus.BLE_DATA_BACK_OK -> mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG)
                ICallbackStatus.UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS -> mHandler.sendEmptyMessage(
                    UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG
                )
                ICallbackStatus.UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL -> mHandler.sendEmptyMessage(
                    UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG
                )
                ICallbackStatus.UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS -> mHandler.sendEmptyMessage(
                    UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG
                )
                ICallbackStatus.UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL -> mHandler.sendEmptyMessage(
                    UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG
                )
                ICallbackStatus.CUSTOMER_ID_OK -> if (result) {
                    LogUtils.d(
                        TAG,
                        "客户ID = " + GBUtils.getInstance(mContext).customerIDAsciiByteToString(data)
                    )
                }
                ICallbackStatus.DO_NOT_DISTURB_CLOSE -> if (data != null && data.size >= 2) {
                    LogUtils.d(TAG, "勿扰模式已关闭")
                    //将data[1]转成二进制，比如data[1] =10，转成二进制为1010，则B3为1（即一键拒接电话开关为开启），B2为0（即消息勿扰开关为关闭），B1为1（即马达勿扰的开关为开启），B0为0（即屏勿扰的开关为关闭状态）
//                    开1关0
//                    B3	              B2	     B1	     B0
//                    一键拒接电话开关	消息勿扰	  马达勿扰	屏勿扰（已弃用）
                }
                ICallbackStatus.DO_NOT_DISTURB_OPEN -> if (data != null && data.size >= 2) {
                    LogUtils.d(TAG, "勿扰模式已打开")
                    //将data[1]转成二进制，比如data[1] =10，转成二进制为1010，则B3为1（即一键拒接电话开关为开启），B2为0（即消息勿扰开关为关闭），B1为1（即马达勿扰的开关为开启），B0为0（即屏勿扰的开关为关闭状态）
//                    开1关0
//                    B3	              B2	     B1	     B0
//                    一键拒接电话开关	消息勿扰	  马达勿扰	屏勿扰（已弃用）
//
                }
                ICallbackStatus.QUICK_SWITCH_SURPPORT_COMMAND_OK -> LogUtils.d(
                    TAG,
                    "APP查询支持的快捷开关，返回所有的快捷开关"
                )
                ICallbackStatus.QUICK_SWITCH_STATUS_COMMAND_OK -> LogUtils.d(
                    TAG,
                    "APP查询快捷开关的状态，返回所有的快捷开关状态，以及手环端快捷开关发生变化时主动上报快捷开关状态"
                )
                else -> {
                }
            }
        }catch (e:Exception){

        }
    }

    override fun onCharacteristicWriteCallback(status: Int) { // add 20170221
        // 写入操作的系统回调，status = 0为写入成功，其他或无回调表示失败
        LogUtils.d(
            TAG,
            "Write System callback status = $status"
        )
    }

    override fun onIbeaconWriteCallback(
        b: Boolean,
        i: Int,
        i1: Int,
        s: String?
    ) {
    }

    override fun onQueryDialModeCallback(b: Boolean, i: Int, i1: Int, i2: Int) {}

    override fun onControlDialCallback(b: Boolean, i: Int, i1: Int) {}

    override fun onSportsTimeCallback(
        b: Boolean,
        s: String?,
        i: Int,
        i1: Int
    ) {
    }

    override fun OnResultSportsModes(
        b: Boolean,
        i: Int,
        i1: Int,
        i2: Int,
        sportsModesInfo: SportsModesInfo?
    ) {
    }

    override fun OnServerCallback(status: Int, description: String?) {
        LogUtils.i(
            TAG,
            "服务器回调 OnServerCallback status =$status"
        )
        if (status == GlobalVariable.SERVER_CALL_BACK_SUCCESSFULL) { //访问服务器OK
            mHandler.sendEmptyMessage(SERVER_CALL_BACK_OK_MSG)
        } else { //访问不到服务器
            mHandler.sendEmptyMessage(GlobalVariable.SERVER_IS_BUSY_MSG)
        }
    }

    override fun OnServiceStatuslt(status: Int) {
        try {
            if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
                LogUtils.d(
                    TAG,
                    "OnServiceStatuslt mBluetoothLeService11 =$mBluetoothLeService"
                )
                if (mBluetoothLeService == null) {
                    mBluetoothLeService = mBLEServiceOperate!!.bleService
                    mBluetoothLeService?.setICallback(this)
                    mBluetoothLeService?.setRateCalibrationListener(this) //设置心率校准监听
                    mBluetoothLeService?.setTurnWristCalibrationListener(this) //设置翻腕校准监听
                    mBluetoothLeService?.setTemperatureListener(this) //设置体温测试，采样数据回调
                    mBluetoothLeService?.setOxygenListener(this) //Oxygen Listener
                    LogUtils.d(
                        TAG,
                        "OnServiceStatuslt mBluetoothLeService22 =$mBluetoothLeService"
                    )
                }
                mDataProcessing = DataProcessing.getInstance(mContext)
                mDataProcessing?.setOnRateListener(mOnRateListener)
                mDataProcessing?.setOnRateOf24HourListenerRate(mOnRateOf24HourListener)
                mBLEServiceOperate?.connect(UserInfoManager.getInstance(activity!!).getDeviceID())

            }
        }catch (e:Exception){

        }
    }


    /*
	 * 获取一天最新心率值、最高、最低、平均心率值
	 */


    //本次同步多运动模式的次数


    /*
	 * 获取一天最新心率值、最高、最低、平均心率值
	 */
    //本次同步多运动模式的次数
    override fun OnResultHeartRateHeadset(
        result: Boolean,
        status: Int,
        sportStatus: Int,
        values: Int,
        info: HeartRateHeadsetSportModeInfo?
    ) {
        try {
            HeartRateHeadsetUtils.LLogI(
                "OnResultHeartRateHeadset  result =" + result + ",status =" + status + ",sportStatus =" + sportStatus
                        + ",values =" + values + ",info =" + info
            )
            when (status) {
                ICallbackStatus.HEART_RATE_HEADSET_SPORT_STATUS -> HeartRateHeadsetUtils.LLogI("心率耳机 运动状态 运动类型=$values,运动状态=$sportStatus")
                ICallbackStatus.HEART_RATE_HEADSET_RATE_INTERVAL -> HeartRateHeadsetUtils.LLogI("心率耳机 时间间隔=$values")
                ICallbackStatus.HEART_RATE_HEADSET_SPORT_DATA -> if (info != null) {
                    val sportMode = info.hrhSportsModes
                    val rateValue = info.hrhRateValue
                    val calories = info.hrhCalories
                    val pace = info.hrhPace
                    val stepCount = info.hrhSteps
                    val count = info.hrhCount
                    val distance = info.hrhDistance
                    val durationTime = info.hrhDuration
                    HeartRateHeadsetUtils.LLogI(
                        "心率耳机 上报上来的实时数据 回调 sportMode=" + sportMode + ",rateValue=" + rateValue + ",calories=" + calories
                                + ",pace=" + pace + ",stepCount=" + stepCount + ",count=" + count + ",distance=" + distance + ",durationTime=" + durationTime
                    )
                }
            }
        }catch (e:Exception){

        }
    }

    override fun OnResultCustomTestStatus(
        result: Boolean,
        status: Int,
        info: CustomTestStatusInfo
    ) {
        try {
            LogUtils.d(
                TAG,
                "  result =$result,status =$status"
            )
            if (status == ICallbackStatus.SET_CUSTOM_TEST_STATUS_OK) { //测试状态，CustomTestStatusInfo.OPEN为开，CustomTestStatusInfo.CLOSE为关
                val heartRateStatus = info.heartRateStatus
                val bpStatus = info.bpStatus
                val oxygenStatus = info.oxygenStatus
                val bodyTemperatureStatus = info.bodyTemperatureStatus
                LogUtils.d(
                    TAG,
                    "  heartRateStatus =$heartRateStatus, bpStatus =$bpStatus, oxygenStatus =$oxygenStatus, bodyTemperatureStatus =$bodyTemperatureStatus"
                )
            } else if (status == ICallbackStatus.CUSTOM_TEST_RESULT_OK) { //测试结果
                val functionType = info.functionType
                val calendar = info.calendar
                val currentMinute = info.currentMinute
                val hour = currentMinute / 60
                val minute = currentMinute % 60
                val timeString = "$hour:$minute"
                LogUtils.d(
                    TAG,
                    "  calendar =$calendar, timeString =$timeString"
                )
                if (functionType == CustomTestStatusInfo.TYPE_HEART_RATE) {
                    val heartRateValue = info.heartRateValue
                    LogUtils.d(
                        TAG,
                        "  heartRateValue =$heartRateValue"
                    )
                } else if (functionType == CustomTestStatusInfo.TYPE_BP) {
                    val bpHighValue = info.bpHighValue
                    val bpLowValue = info.bpLowValue
                    LogUtils.d(
                        TAG,
                        "  bpHighValue =$bpHighValue, bpLowValue =$bpLowValue"
                    )
                } else if (functionType == CustomTestStatusInfo.TYPE_OXYGEN) {
                    val oxygenValue = info.oxygenValue
                    LogUtils.d(
                        TAG,
                        "  oxygenValue =$oxygenValue"
                    )
                } else if (functionType == CustomTestStatusInfo.TYPE_BODY_TEMPERATURE) {
                    val bodySurfaceTemperature = info.bodySurfaceTemperature
                    val bodyTemperature = info.bodyTemperature
                    LogUtils.d(
                        TAG,
                        "  bodySurfaceTemperature =$bodySurfaceTemperature, bodyTemperature =$bodyTemperature"
                    )
                }
            } else if (status == ICallbackStatus.QUERY_CUSTOM_TEST_STATUS_OK) { //查询返回的测试状态，CustomTestStatusInfo.OPEN为开，CustomTestStatusInfo.CLOSE为关
                val heartRateStatus = info.heartRateStatus
                val bpStatus = info.bpStatus
                val oxygenStatus = info.oxygenStatus
                val bodyTemperatureStatus = info.bodyTemperatureStatus
                LogUtils.d(
                    TAG,
                    " heartRateStatus =$heartRateStatus, bpStatus =$bpStatus, oxygenStatus =$oxygenStatus, bodyTemperatureStatus =$bodyTemperatureStatus"
                )
            }
        }catch (e:Exception){

        }
    }

    override fun onRateCalibrationStatus(status: Int) {

/*		status: 0----校准完成
		        1----校准开始
		        253---清除校准参数完成
		        校准开始后，应用端自己做超时，10秒钟没收到校准完成0，则需主动调用停止校准stopRateCalibration()*/
        LogUtils.d(TAG, "心率校准 status:$status")
    }

    override fun onTurnWristCalibrationStatus(status: Int) {
        LogUtils.d(TAG, "翻腕校准 status:$status")
        /*status: 0----校准完成
                  1----校准开始
                  255----校准失败
                  253---清除校准参数完成*/
    }

    override fun onTestResult(info: TemperatureInfo) { //单次测试结果
        LogUtils.d(
            TAG,
            "calendar =" + info.calendar + ",startDate =" + info.startDate + ",secondTime =" + info.secondTime
                    + ",bodyTemperature =" + info.bodyTemperature
        )
    }

    override fun onSamplingResult(info: TemperatureInfo) {
        try {
//        info.getType();以下三种类型
//        TemperatureUtil.TYPE_NOT_SUPPORT_SAMPLING ;//不支持体温原始数据采样
//        TemperatureUtil.TYPE_SUPPORT_SAMPLING_MODE_1;//支持体温原始数据采样,格式一
//        TemperatureUtil.TYPE_SUPPORT_SAMPLING_MODE_2;//支持体温原始数据采样,格式二
            LogUtils.d(
                TAG,
                "type =" + info.type + ",calendar =" + info.calendar + ",startDate =" + info.startDate + ",secondTime =" + info.secondTime
                        + ",bodyTemperature =" + info.bodyTemperature + ",bodySurfaceTemperature =" + info.bodySurfaceTemperature
                        + ",ambientTemperature =" + info.ambientTemperature
            )
            activity?.runOnUiThread {
                try {
                    TempInsert(info)
                    temp.text = String.format("%.1f", info.bodyTemperature)
                    Temp = String.format("%.1f", info.bodyTemperature).toDouble()
                    setTempData()
                    onConnected()
                }catch (e:Exception) {

                }
            }
        }catch (e:Exception){

        }

    }

    override fun onTestResult(status: Int, info: OxygenInfo) {
        try {
            val message = Message()
            if (status == OxygenUtil.OXYGEN_TEST_START_HAS_VALUE) { //start has oxygen value
                message.what = status
                message.obj = info
            } else if (status == OxygenUtil.OXYGEN_TEST_START_NO_VALUE) { //start has no oxygen value
                message.what = status
            } else if (status == OxygenUtil.OXYGEN_TEST_STOP_HAS_VALUE) { //stop has oxygen value
                message.what = status
                message.obj = info //oxygen value is "info.getOxygenValue()"
            } else if (status == OxygenUtil.OXYGEN_TEST_STOP_NO_VALUE) { //stop has no oxygen value
                message.what = status
            } else if (status == OxygenUtil.OXYGEN_TEST_TIME_OUT) { //Test time out
                message.what = status
            }
            OxygenUtil.LogI("onTestResult status =$status,info =$info")
            mHandler.sendMessage(message)
        } catch (e: Exception) {

        }
    }

  
}