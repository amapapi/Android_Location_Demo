
package com.amap.location.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolygonOptions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 行政区划地理围栏
 * 
 * @author hongming.wang
 * @since 3.2.0
 */
public class GeoFence_District_Activity extends CheckPermissionsActivity
		implements
			OnClickListener,
			GeoFenceListener,
			LocationSource,
			AMapLocationListener,
			OnCheckedChangeListener {

	private View lyOption;
	private TextView tvResult;

	private EditText etCustomerId;
	private EditText etKeyword;

	private CheckBox cbAlertIn;
	private CheckBox cbAlertOut;
	private CheckBox cbAldertStated;

	private Button btAddFence;

	/**
	 * 用于显示当前的位置
	 * <p>
	 * 示例中是为了显示当前的位置，在实际使用中，单独的地理围栏可以不使用定位接口
	 * </p>
	 */
	private AMapLocationClient mlocationClient;
	private OnLocationChangedListener mListener;
	private AMapLocationClientOption mLocationOption;

	private MapView mMapView;
	private AMap mAMap;

	// 记录已经添加成功的围栏
	private HashMap<String, GeoFence> fenceMap = new HashMap<String, GeoFence>();
	// 当前的坐标点集合，主要用于进行地图的可视区域的缩放
	private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

	// 地理围栏客户端
	GeoFenceClient fenceClient = null;

	// 触发地理围栏的行为，默认为进入提醒
	int activatesAction = GeoFenceClient.GEOFENCE_IN;
	// 地理围栏的广播action
	static final String GEOFENCE_BROADCAST_ACTION = "com.example.geofence.district";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geofence_new);
		// 初始化地理围栏
		fenceClient = new GeoFenceClient(getApplicationContext());

		lyOption = findViewById(R.id.ly_option);
		btAddFence = (Button) findViewById(R.id.bt_addFence);
		tvResult = (TextView) findViewById(R.id.tv_result);
		tvResult.setVisibility(View.GONE);

		etCustomerId = (EditText) findViewById(R.id.et_customerId);
		etKeyword = (EditText) findViewById(R.id.et_keyword);

		cbAlertIn = (CheckBox) findViewById(R.id.cb_alertIn);
		cbAlertOut = (CheckBox) findViewById(R.id.cb_alertOut);
		cbAldertStated = (CheckBox) findViewById(R.id.cb_alertStated);

		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		init();
	}

	void init() {
		if (mAMap == null) {
			mAMap = mMapView.getMap();
			mAMap.getUiSettings().setRotateGesturesEnabled(false);
			mAMap.moveCamera(CameraUpdateFactory.zoomBy(6));
			setUpMap();
		}

		resetView();
		resetView_district();

		btAddFence.setOnClickListener(this);
		cbAlertIn.setOnCheckedChangeListener(this);
		cbAlertOut.setOnCheckedChangeListener(this);
		cbAldertStated.setOnCheckedChangeListener(this);

		IntentFilter fliter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		fliter.addAction(GEOFENCE_BROADCAST_ACTION);
		registerReceiver(mGeoFenceReceiver, fliter);

		fenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
		fenceClient.setGeoFenceListener(this);
		fenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN);
	}

	private void resetView() {
		lyOption.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		mAMap.setLocationSource(this);// 设置定位监听
		mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		mAMap.setMyLocationStyle(
				new MyLocationStyle().radiusFillColor(Color.argb(0, 0, 0, 0))
						.strokeColor(Color.argb(0, 0, 0, 0)));
		mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
		deactivate();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		try {
			unregisterReceiver(mGeoFenceReceiver);
		} catch (Throwable e) {
		}

		if (null != fenceClient) {
			fenceClient.removeGeoFence();
		}
		if (null != mlocationClient) {
			mlocationClient.onDestroy();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_addFence :
				addFence();
				break;
		}
	}

	private void drawFence(GeoFence fence) {
		switch (fence.getType()) {
			case GeoFence.TYPE_ROUND :
			case GeoFence.TYPE_AMAPPOI :
				drawCircle(fence);
				break;
			case GeoFence.TYPE_POLYGON :
			case GeoFence.TYPE_DISTRICT :
				drawPolygon(fence);
				break;
			default :
				break;
		}

		// 设置所有maker显示在当前可视区域地图中
		LatLngBounds bounds = boundsBuilder.build();
		mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
	}

	private void drawCircle(GeoFence fence) {
		LatLng center = new LatLng(fence.getCenter().getLatitude(),
				fence.getCenter().getLongitude());
		// 绘制一个圆形
		mAMap.addCircle(new CircleOptions().center(center)
				.radius(fence.getRadius()).strokeColor(Color.BLUE)
				.fillColor(Color.argb(1, 1, 1, 1)).strokeWidth(10));
		boundsBuilder.include(center);
	}

	private void drawPolygon(GeoFence fence) {
		final List<List<DPoint>> pointList = fence.getPointList();
		if (null == pointList || pointList.isEmpty()) {
			return;
		}
		for (List<DPoint> subList : pointList) {
			List<LatLng> lst = new ArrayList<LatLng>();

			PolygonOptions polygonOption = new PolygonOptions();
			for (DPoint point : subList) {
				lst.add(new LatLng(point.getLatitude(), point.getLongitude()));
				boundsBuilder.include(
						new LatLng(point.getLatitude(), point.getLongitude()));
			}
			polygonOption.addAll(lst);

			polygonOption.strokeColor(Color.BLUE).strokeWidth(10)
					.fillColor(Color.argb(1, 1, 1, 1));
			mAMap.addPolygon(polygonOption);
		}
	}

	Object lock = new Object();
	void drawFence2Map() {
		new Thread() {
			@Override
			public void run() {
				try {
					synchronized (lock) {
						if (null == fenceList || fenceList.isEmpty()) {
							return;
						}
						for (GeoFence fence : fenceList) {
							if (fenceMap.containsKey(fence.getFenceId())) {
								continue;
							}
							drawFence(fence);
							fenceMap.put(fence.getFenceId(), fence);
						}
					}
				} catch (Throwable e) {

				}
			}
		}.start();
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0 :
					Toast.makeText(getApplicationContext(), "添加围栏成功",
							Toast.LENGTH_SHORT).show();
					drawFence2Map();
					break;
				case 1 :
					int errorCode = msg.arg1;
					Toast.makeText(getApplicationContext(),
							"添加围栏失败 " + errorCode, Toast.LENGTH_SHORT).show();
					break;
				case 2 :
					String statusStr = (String) msg.obj;
					tvResult.setVisibility(View.VISIBLE);
					tvResult.append(statusStr + "\n");
					break;
				default :
					break;
			}
		}
	};

	List<GeoFence> fenceList = new ArrayList<GeoFence>();
	@Override
	public void onGeoFenceCreateFinished(final List<GeoFence> geoFenceList,
			int errorCode) {
		if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
			fenceList = geoFenceList;
			handler.sendEmptyMessage(0);
			Log.e("WHM", "添加围栏成功！！");
		} else {
			Log.e("WHM", "添加围栏失败！！！！ errorCode: " + errorCode);
			Message msg = Message.obtain();
			msg.arg1 = errorCode;
			msg.what = 1;
			handler.sendMessage(msg);
		}
	}

	private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收广播
			if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
				Bundle bundle = intent.getExtras();
				String customerId = bundle
						.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
				int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
				StringBuffer sb = new StringBuffer();
				switch (status) {
					case GeoFence.STATUS_LOCFAIL :
						sb.append("定位失败");
						break;
					case GeoFence.STATUS_IN :
						sb.append("进入围栏 ").append(customerId);
						break;
					case GeoFence.STATUS_OUT :
						sb.append("离开围栏 ").append(customerId);
						break;
					case GeoFence.STATUS_STAYED :
						sb.append("停留在围栏内 ").append(customerId);
						break;
					default :
						break;
				}
				String str = sb.toString();
				Message msg = Message.obtain();
				msg.obj = str;
				msg.what = 2;
				handler.sendMessage(msg);
			}
		}
	};

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null && amapLocation.getErrorCode() == 0) {
				tvResult.setVisibility(View.GONE);
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode() + ": "
						+ amapLocation.getErrorInfo();
				Log.e("AmapErr", errText);
				tvResult.setVisibility(View.VISIBLE);
				tvResult.setText(errText);
			}
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			// 设置定位监听
			mlocationClient.setLocationListener(this);
			// 设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			// 只是为了获取当前位置，所以设置为单次定位
			mLocationOption.setOnceLocation(true);
			// 设置定位参数
			mlocationClient.setLocationOption(mLocationOption);
			mlocationClient.startLocation();
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.cb_alertIn :
				if (isChecked) {
					activatesAction |= GeoFenceClient.GEOFENCE_IN;
				} else {
					activatesAction = activatesAction
							& (GeoFenceClient.GEOFENCE_OUT
									| GeoFenceClient.GEOFENCE_STAYED);
				}
				break;
			case R.id.cb_alertOut :
				if (isChecked) {
					activatesAction |= GeoFenceClient.GEOFENCE_OUT;
				} else {
					activatesAction = activatesAction
							& (GeoFenceClient.GEOFENCE_IN
									| GeoFenceClient.GEOFENCE_STAYED);
				}
				break;
			case R.id.cb_alertStated :
				if (isChecked) {
					activatesAction |= GeoFenceClient.GEOFENCE_STAYED;
				} else {
					activatesAction = activatesAction
							& (GeoFenceClient.GEOFENCE_IN
									| GeoFenceClient.GEOFENCE_OUT);
				}
				break;
			default :
				break;
		}
		if (null != fenceClient) {
			fenceClient.setActivateAction(activatesAction);
		}
	}

	private void resetView_district() {
		etKeyword.setVisibility(View.VISIBLE);
	}

	/**
	 * 添加围栏
	 * 
	 * @since 3.2.0
	 * @author hongming.wang
	 *
	 */
	private void addFence() {
		addDistrictFence();
	}

	/**
	 * 添加行政区划围栏
	 * 
	 * @since 3.2.0
	 * @author hongming.wang
	 *
	 */
	private void addDistrictFence() {
		String keyword = etKeyword.getText().toString();
		String customerId = etCustomerId.getText().toString();
		if (TextUtils.isEmpty(keyword) || TextUtils.isEmpty(customerId)) {
			Toast.makeText(getApplicationContext(), "参数不全", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		fenceClient.addGeoFence(keyword, customerId);
	}
}
