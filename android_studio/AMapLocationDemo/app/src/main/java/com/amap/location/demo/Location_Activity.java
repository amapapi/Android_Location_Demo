package com.amap.location.demo;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * 高精度定位模式功能演示
 *
 * @创建时间： 2015年11月24日 下午5:22:42
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: Hight_Accuracy_Activity.java
 * @类型名称: Hight_Accuracy_Activity
 */
public class Location_Activity extends CheckPermissionsActivity
		implements
			OnCheckedChangeListener,
			OnClickListener,
			CompoundButton.OnCheckedChangeListener {
	private RadioGroup rgLocationMode;
	private EditText etInterval;
	private EditText etHttpTimeout;
	private CheckBox cbOnceLocation;
	private CheckBox cbAddress;
	private CheckBox cbGpsFirst;
	private CheckBox cbCacheAble;
	private CheckBox cbOnceLastest;
	private CheckBox cbSensorAble;
	private TextView tvResult;
	private Button btLocation;

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = new AMapLocationClientOption();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		setTitle(R.string.title_location);
		
		initView();
		
		//初始化定位
		initLocation();
	}
	
	//初始化控件
	private void initView(){
		rgLocationMode = (RadioGroup) findViewById(R.id.rg_locationMode);
		
		etInterval = (EditText) findViewById(R.id.et_interval);
		etHttpTimeout = (EditText) findViewById(R.id.et_httpTimeout);
		
		cbOnceLocation = (CheckBox)findViewById(R.id.cb_onceLocation);
		cbGpsFirst = (CheckBox) findViewById(R.id.cb_gpsFirst);
		cbAddress = (CheckBox) findViewById(R.id.cb_needAddress);
		cbCacheAble = (CheckBox) findViewById(R.id.cb_cacheAble);
		cbOnceLastest = (CheckBox) findViewById(R.id.cb_onceLastest);
		cbSensorAble = (CheckBox)findViewById(R.id.cb_sensorAble);

		tvResult = (TextView) findViewById(R.id.tv_result);
		btLocation = (Button) findViewById(R.id.bt_location);
		
		rgLocationMode.setOnCheckedChangeListener(this);
		cbAddress.setOnCheckedChangeListener(this);
		cbCacheAble.setOnCheckedChangeListener(this);
		btLocation.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyLocation();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (null == locationOption) {
			locationOption = new AMapLocationClientOption();
		}
		switch (checkedId) {
			case R.id.rb_batterySaving :
				locationOption.setLocationMode(AMapLocationMode.Battery_Saving);
				break;
			case R.id.rb_deviceSensors :
				locationOption.setLocationMode(AMapLocationMode.Device_Sensors);
				break;
			case R.id.rb_hightAccuracy :
				locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
				break;
			default :
				break;
		}
	}

	/**
	 * 设置控件的可用状态
	 */
	private void setViewEnable(boolean isEnable) {
		for(int i=0; i<rgLocationMode.getChildCount(); i++){
			rgLocationMode.getChildAt(i).setEnabled(isEnable);
		}
		etInterval.setEnabled(isEnable);
		etHttpTimeout.setEnabled(isEnable);
		cbOnceLocation.setEnabled(isEnable);
		cbGpsFirst.setEnabled(isEnable);
		cbAddress.setEnabled(isEnable);
		cbCacheAble.setEnabled(isEnable);
		cbOnceLastest.setEnabled(isEnable);
		cbSensorAble.setEnabled(isEnable);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_location) {
			if (btLocation.getText().equals(
					getResources().getString(R.string.startLocation))) {
				setViewEnable(false);
				btLocation.setText(getResources().getString(
						R.string.stopLocation));
				tvResult.setText("正在定位...");
				startLocation();
			} else {
				setViewEnable(true);
				btLocation.setText(getResources().getString(
						R.string.startLocation));
				stopLocation();
				tvResult.setText("定位停止");
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(null == locationOption){
			locationOption = new AMapLocationClientOption();
		}
		switch (buttonView.getId()) {
			case R.id.cb_needAddress :
				if(null != locationOption){
					locationOption.setNeedAddress(isChecked);
				}
				break;
			case R.id.cb_cacheAble :
				if(null != locationOption){
					locationOption.setLocationCacheEnable(isChecked);
				}
				break;
			default :
				break;
		}
		if(null != locationClient){
			locationClient.setLocationOption(locationOption);
		}
	}
	
	/**
	 * 初始化定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void initLocation(){
		//初始化client
		locationClient = new AMapLocationClient(this.getApplicationContext());
		//设置定位参数
		locationClient.setLocationOption(getDefaultOption());
		// 设置定位监听
		locationClient.setLocationListener(locationListener);
	}
	
	/**
	 * 默认的定位参数
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption(){
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
		mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是ture
		mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
		return mOption;
	}
	
	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation loc) {
			if (null != loc) {
				//解析定位结果
				String result = Utils.getLocationStr(loc);
				tvResult.setText(result);
			} else {
				tvResult.setText("定位失败，loc is null");
			}
		}
	};
	
	// 根据控件的选择，重新设置定位参数
	private void resetOption() {
		// 设置是否需要显示地址信息
		locationOption.setNeedAddress(cbAddress.isChecked());
		/**
		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
		 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
		 */
		locationOption.setGpsFirst(cbGpsFirst.isChecked());
		// 设置是否开启缓存
		locationOption.setLocationCacheEnable(cbCacheAble.isChecked());
		//设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
		locationOption.setOnceLocationLatest(cbOnceLastest.isChecked());
		//设置是否使用传感器
		locationOption.setSensorEnable(cbSensorAble.isChecked());
		String strInterval = etInterval.getText().toString();
		if (!TextUtils.isEmpty(strInterval)) {
			try{
				// 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
				locationOption.setInterval(Long.valueOf(strInterval));
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
		
		String strTimeout = etHttpTimeout.getText().toString();
		if(!TextUtils.isEmpty(strTimeout)){
			try{
				// 设置网络请求超时时间
			     locationOption.setHttpTimeOut(Long.valueOf(strTimeout));
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * 开始定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void startLocation(){
		//根据控件的选择，重新设置定位参数
		resetOption();
		// 设置定位参数
		locationClient.setLocationOption(locationOption);
		// 启动定位
		locationClient.startLocation();
	}
	
	/**
	 * 停止定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void stopLocation(){
		// 停止定位
		locationClient.stopLocation();
	}
	
	/**
	 * 销毁定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void destroyLocation(){
		if (null != locationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
	}
}
