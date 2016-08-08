package com.amap.location.demo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

/**
 * 地理围栏功能演示
 *
 * @创建时间： 2015年11月24日 下午5:49:52
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: GeoFence_Activity.java
 * @类型名称: GeoFence_Activity
 */
public class GeoFence_Activity extends CheckPermissionsActivity implements OnClickListener,
		AMapLocationListener {
	private EditText etRadius;
	private TextView tvReult;
	private CheckBox cbAlertIn;
	private CheckBox cbAlertOut;
	private Button btFence;

	// 声明一个单次定位的客户端，获取当前位置的坐标，用于设置围栏的中心点坐标
	private AMapLocationClient onceClient = null;
	// 声明一个持续定位的客户端，用于添加地理围栏
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;

	// 用于接收地理围栏提醒的pendingIntent
	private PendingIntent mPendingIntent = null;
	public static final String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geofence);
		setTitle(R.string.title_geoFenceAlert);

		etRadius = (EditText) findViewById(R.id.et_radius);
		cbAlertIn = (CheckBox) findViewById(R.id.cb_alertIn);
		cbAlertOut = (CheckBox) findViewById(R.id.cb_alertOut);
		tvReult = (TextView) findViewById(R.id.tv_result);
		btFence = (Button) findViewById(R.id.bt_fence);

		btFence.setOnClickListener(this);

		IntentFilter fliter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		fliter.addAction(GEOFENCE_BROADCAST_ACTION);
		registerReceiver(mGeoFenceReceiver, fliter);
		Intent intent = new Intent(GEOFENCE_BROADCAST_ACTION);
		mPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
				intent, 0);
		onceClient = new AMapLocationClient(getApplicationContext());
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = new AMapLocationClientOption();

		// 设置定位模式高精度，添加地理围栏最好设置成高精度模式
		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置定位监听
		locationClient.setLocationListener(this);

		AMapLocationClientOption onceOption = new AMapLocationClientOption();
		onceOption.setOnceLocation(true);
		onceClient.setLocationOption(onceOption);
		onceClient.setLocationListener(new AMapLocationListener() {
			@Override
			public void onLocationChanged(AMapLocation loc) {
				if (loc != null) {
					if (loc.getErrorCode() == 0) {
						if (null != locationClient) {
							float radius = 1000;
							String strRadius = etRadius.getText().toString();
							if (!TextUtils.isEmpty(strRadius)) {
								radius = Float.valueOf(strRadius);
							}
							// 添加地理围栏，
							// 第一个参数：围栏ID,可以自定义ID,示例中为了方便只使用一个ID;第二个：纬度；第三个：精度；
							// 第四个：半径；第五个：过期时间，单位毫秒，-1代表不过期；第六个：接收触发消息的PendingIntent
							locationClient.addGeoFenceAlert("fenceId",
									loc.getLatitude(), loc.getLongitude(),
									radius, -1, mPendingIntent);
						}
					} else {
						Toast.makeText(getApplicationContext(), "获取当前位置失败!",
								Toast.LENGTH_SHORT).show();

						Message msg = mHandler.obtainMessage();
						msg.obj = loc;
						msg.what = -1;
						mHandler.sendMessage(msg);
					}
				}
			}
		});
	}

	Handler mHandler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				tvReult.setText("进入围栏区域");
				break;
			case 2:
				tvReult.setText("离开围栏区域");
				break;
			case -1:
				// 获取当前位置失败
				AMapLocation loc = (AMapLocation) msg.obj;
				tvReult.setText(Utils.getLocationStr(loc));
				btFence.setText(getResources().getString(R.string.addFence));
				break;
			default:
				break;
			}
		};
	};

	private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收广播
			if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
				Bundle bundle = intent.getExtras();
				// 根据广播的event来确定是在区域内还是在区域外
				int status = bundle.getInt("event");
				String geoFenceId = bundle.getString("fenceid");
				if (status == 1) {
					// 进入围栏区域
					// 可以自定义提醒方式,示例中使用的是文字方式
					if (cbAlertIn.isChecked()) {
						mHandler.sendEmptyMessage(1);
					}
				} else if (status == 2) {
					// 离开围栏区域
					// 可以自定义提醒方式,示例中使用的是文字方式
					if (cbAlertOut.isChecked()) {
						mHandler.sendEmptyMessage(2);
					}
				}
			}
		}
	};

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

		if (null != onceClient) {
			onceClient.onDestroy();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_fence) {
			if (btFence.getText().equals(
					getResources().getString(R.string.addFence))) {
				btFence.setText(getResources().getString(R.string.removeFence));
				// 启动单次定位，获取当前位置
				onceClient.startLocation();

				// 设置定位参数
				locationClient.setLocationOption(locationOption);
				// 启动定位,地理围栏依赖于持续定位
				locationClient.startLocation();

			} else {
				tvReult.setText("");
				btFence.setText(getResources().getString(R.string.addFence));
				// 移除围栏
				locationClient.removeGeoFenceAlert(mPendingIntent);
			}
		}
	}

	// 定位监听
	@Override
	public void onLocationChanged(AMapLocation loc) {
	}
}
