package com.amap.location.demo;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 后台唤醒定位
 * @创建时间：2016年1月8日 上午11:25:01
 * @项目名称： AMapLocationDemo2.x 
 * @author hongming.wang
 * @文件名称：Alarm_Location_Activity.java
 * @类型名称：Alarm_Location_Activity
 * @since 2.3.0
 */
public class Alarm_Location_Activity extends CheckPermissionsActivity implements
		 OnClickListener, AMapLocationListener {
	private EditText etInterval;
	private EditText etAlarm;
	private CheckBox cbAddress;
	private CheckBox cbGpsFirst;
	private TextView tvReult;
	private Button btLocation;

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	
	private Intent alarmIntent = null;
	private PendingIntent alarmPi = null;
	private AlarmManager alarm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_location);
		setTitle(R.string.title_alarmCPU);

		etInterval = (EditText) findViewById(R.id.et_interval);
		etAlarm = (EditText) findViewById(R.id.et_alarm);
		cbAddress = (CheckBox) findViewById(R.id.cb_needAddress);
		cbGpsFirst = (CheckBox) findViewById(R.id.cb_gpsFirst);
		tvReult = (TextView) findViewById(R.id.tv_result);
		btLocation = (Button) findViewById(R.id.bt_location);

		btLocation.setOnClickListener(this);

		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式
		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置定位监听
		locationClient.setLocationListener(this);
		
		// 创建Intent对象，action为LOCATION
		alarmIntent = new Intent();
		alarmIntent.setAction("LOCATION");
		IntentFilter ift = new IntentFilter();

		// 定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
		// 也就是发送了action 为"LOCATION"的intent
		alarmPi = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
		// AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		//动态注册一个广播
		IntentFilter filter = new IntentFilter();
		filter.addAction("LOCATION");
		registerReceiver(alarmReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != locationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
		
		if(null != alarmReceiver){
			unregisterReceiver(alarmReceiver);
			alarmReceiver = null;
		}
	}

	/**
	 * 设置控件的可用状态
	 */
	private void setViewEnable(boolean isEnable) {
		etInterval.setEnabled(isEnable);
		etAlarm.setEnabled(isEnable);
		cbAddress.setEnabled(isEnable);
		cbGpsFirst.setEnabled(isEnable);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_location) {
			if (btLocation.getText().equals(
					getResources().getString(R.string.startLocation))) {
				setViewEnable(false);
				initOption();
				int alarmInterval = 5;
				String str = etAlarm.getText().toString();
				if(!TextUtils.isEmpty(str)){
					alarmInterval = Integer.parseInt(str);
				}
				
				btLocation.setText(getResources().getString(
						R.string.stopLocation));
				// 设置定位参数
				locationClient.setLocationOption(locationOption);
				// 启动定位
				locationClient.startLocation();
				mHandler.sendEmptyMessage(Utils.MSG_LOCATION_START);
				
				if(null != alarm){
					//设置一个闹钟，2秒之后每隔一段时间执行启动一次定位程序
					alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 2*1000,
							alarmInterval * 1000, alarmPi);
				}
			} else {
				setViewEnable(true);
				btLocation.setText(getResources().getString(
						R.string.startLocation));
				// 停止定位
				locationClient.stopLocation();
				mHandler.sendEmptyMessage(Utils.MSG_LOCATION_STOP);
				
				//停止定位的时候取消闹钟
				if(null != alarm){
					alarm.cancel(alarmPi);
				}
			}
		}
	}

	// 根据控件的选择，重新设置定位参数
	private void initOption() {
		// 设置是否需要显示地址信息
		locationOption.setNeedAddress(cbAddress.isChecked());
		/**
		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
		 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
		 */
		locationOption.setGpsFirst(cbGpsFirst.isChecked());
		String strInterval = etInterval.getText().toString();
		if (!TextUtils.isEmpty(strInterval)) {
			// 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
			locationOption.setInterval(Long.valueOf(strInterval));
		}

	}

	Handler mHandler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			//开始定位
			case Utils.MSG_LOCATION_START:
				tvReult.setText("正在定位...");
				break;
			// 定位完成
			case Utils.MSG_LOCATION_FINISH:
				AMapLocation loc = (AMapLocation) msg.obj;
				String result = Utils.getLocationStr(loc);
				tvReult.setText(result);
				break;
			//停止定位
			case Utils.MSG_LOCATION_STOP:
				tvReult.setText("定位停止");
				break;
			default:
				break;
			}
		};
	};
	
	// 定位监听
	@Override
	public void onLocationChanged(AMapLocation loc) {
		if (null != loc) {
			Message msg = mHandler.obtainMessage();
			msg.obj = loc;
			msg.what = Utils.MSG_LOCATION_FINISH;
			mHandler.sendMessage(msg);
		}
	}
	
	
	private BroadcastReceiver alarmReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("LOCATION")){
				if(null != locationClient){
					locationClient.startLocation();
				}
			}
		}
	};
	
}
