package com.amap.location.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;

/**
 * 高精度定位模式功能演示
 *
 * @创建时间： 2015年11月24日 下午5:22:42
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: Hight_Accuracy_Activity.java
 * @类型名称: Hight_Accuracy_Activity
 */
public class Mulity_LocationClient_Activity extends CheckPermissionsActivity
		implements
			OnClickListener{
	private Button btClient1;
	private Button btClient2;
	private TextView tvResult1;
	private TextView tvResult2;

	private AMapLocationClient locationClient1 = null;
	private AMapLocationClient locationClient2 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mulity_client);
		setTitle(R.string.title_mulityClient);

		btClient1 = (Button)findViewById(R.id.bt_startClient1);
		btClient2 = (Button)findViewById(R.id.bt_startClient2);

		tvResult1 = (TextView)findViewById(R.id.tv_result1);
		tvResult2 = (TextView)findViewById(R.id.tv_result2);

		btClient1.setOnClickListener(this);
		btClient2.setOnClickListener(this);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(null != locationClient1){
			locationClient1.onDestroy();
			locationClient1 = null;
		}
		if(null != locationClient2){
			locationClient2.onDestroy();
			locationClient2 = null;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_startClient1) {
			if (btClient1.getText().equals(
					getResources().getString(R.string.startLocation))) {
				handler1.sendEmptyMessage(0);
			} else {
				handler1.sendEmptyMessage(1);
			}
		}

		if (v.getId() == R.id.bt_startClient2) {
			if (btClient2.getText().equals(
					getResources().getString(R.string.startLocation))) {
				handler2.sendEmptyMessage(0);
			} else {
				handler2.sendEmptyMessage(1);
			}
		}
	}



	/**
	 * 启动第一个客户端定位
	 */
	void startLocation1(){
		if(null == locationClient1){
			locationClient1 = new AMapLocationClient(this.getApplicationContext());
		}
		//使用单次定位
		AMapLocationClientOption option = new AMapLocationClientOption();
		option.setOnceLocation(true);
		locationClient1.setLocationOption(option);
		locationClient1.setLocationListener(locationListener1);
		locationClient1.startLocation();
	}

	/**
	 * 停止第一个客户端
	 */
	void stopLocation1(){
		if(null != locationClient1){
			locationClient1.stopLocation();
		}
	}

	/**
	 * 启动第二个客户端定位
	 */
	void startLocation2(){
		if(null == locationClient2){
			locationClient2 = new AMapLocationClient(this.getApplicationContext());
		}
		//使用默认的定位方式
		locationClient1.setLocationOption(new AMapLocationClientOption());
		locationClient2.setLocationListener(locationListener2);
		locationClient2.startLocation();
	}

	/**
	 * 停止第二个客户端
	 */
	void stopLocation2(){
		if(null != locationClient2){
			locationClient2.stopLocation();
		}
	}

	/**
	 * 第一个客户端的定位监听
	 */
	AMapLocationListener locationListener1 = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation loc) {
			Message msg = handler1.obtainMessage();
			msg.what = 2;
			Bundle bundle = new Bundle();
			bundle.putLong("callbackTime", System.currentTimeMillis());
			bundle.putParcelable("loc", loc);
			msg.setData(bundle);
			handler1.sendMessage(msg);
		}
	};

	/**
	 * 第二个客户端的定位监听
	 */
	AMapLocationListener locationListener2 = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation loc) {
			Message msg = handler2.obtainMessage();
			msg.what = 2;
			Bundle bundle = new Bundle();
			bundle.putLong("callbackTime", System.currentTimeMillis());
			bundle.putParcelable("loc", loc);
			msg.setData(bundle);
			handler2.sendMessage(msg);
		}
	};

	Handler handler1 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 0:
					btClient1.setText(getString(R.string.stopLocation));
					tvResult1.setText("客户端1正在定位...");
					startLocation1();
					break;
				case 1:
					stopLocation1();
					btClient1.setText(getString(R.string.startLocation));
					tvResult1.setText("客户端1定位停止");
					break;
				case 2:
					Bundle bd = msg.getData();
					long callBackTime = bd.getLong("callbackTime");
					AMapLocation location = bd.getParcelable("loc");
					StringBuffer sb = new StringBuffer();
					sb.append("客户端1定位完成\n");
					sb.append("回调时间: " + Utils.formatUTC(callBackTime, null) + "\n");
					if(null == location){
						sb.append("定位失败：location is null!!!!!!!");
					} else {
						if(location.getErrorCode() == 0) {
							sb.append("定位成功\n");
							sb.append("经纬度：(" + location.getLongitude() + "," + location.getLatitude

									() + ") \n");
							sb.append("定位时间: " + Utils.formatUTC(location.getTime(), null));
						} else {
							sb.append("定位失败!!!\n");
							sb.append("错误码：" + location.getErrorCode() + "\n");
							sb.append("错误信息：" + location.getErrorInfo() + "\n");
							sb.append("详细信息：" + location.getLocationDetail() + "\n");
						}
					}
					tvResult1.setText(sb.toString());
					break;
			}
		}
	};

	Handler handler2 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 0:
					btClient2.setText(getString(R.string.stopLocation));
					tvResult2.setText("客户端2正在定位...");
					startLocation2();
					break;
				case 1:
					stopLocation2();
					btClient2.setText(getString(R.string.startLocation));
					tvResult2.setText("客户端2定位停止");
					break;
				case 2:
					Bundle bd = msg.getData();
					long callBackTime = bd.getLong("callbackTime");
					AMapLocation location = bd.getParcelable("loc");
					StringBuffer sb = new StringBuffer();
					sb.append("客户端2定位完成\n");
					sb.append("回调时间: " + Utils.formatUTC(callBackTime, null) + "\n");
					if(null == location){
						sb.append("定位失败：location is null!!!!!!!");
					} else {
						if(location.getErrorCode() == 0) {
							sb.append("定位成功\n");
							sb.append("经纬度：(" + location.getLongitude() + "," + location.getLatitude

									() + ") \n");
							sb.append("定位时间: " + Utils.formatUTC(location.getTime(), null));
						} else {
							sb.append("定位失败!!!\n");
							sb.append("错误码：" + location.getErrorCode() + "\n");
							sb.append("错误信息：" + location.getErrorInfo() + "\n");
							sb.append("详细信息：" + location.getLocationDetail() + "\n");
						}
					}
					tvResult2.setText(sb.toString());
					break;
			}
		}
	};
}
