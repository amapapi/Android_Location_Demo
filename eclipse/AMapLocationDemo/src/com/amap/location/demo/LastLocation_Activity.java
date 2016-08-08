/**
 * 
 */
package com.amap.location.demo;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 最后位置功能演示
 * @创建时间：2016年1月7日 下午4:36:22
 * @项目名称： AMapLocationDemo2.x 
 * @author hongming.wang
 * @文件名称：LastLocation_Activity.java
 * @类型名称：LastLocation_Activity
 * @since 2.3.0
 */
public class LastLocation_Activity extends CheckPermissionsActivity implements OnClickListener{
	private Button btnLastLoc;
	private TextView tvReult;
	private AMapLocationClient locationClient = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lastlocation);
		setTitle(R.string.title_lastLocation);
		tvReult = (TextView) findViewById(R.id.tv_result);
		btnLastLoc = (Button) findViewById(R.id.bt_lastLoc);
		btnLastLoc.setOnClickListener(this);
		locationClient = new AMapLocationClient(this.getApplicationContext());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != locationClient) {
			locationClient.onDestroy();
			locationClient = null;
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.bt_lastLoc){
			AMapLocation location = locationClient.getLastKnownLocation();
			String result = Utils.getLocationStr(location);
			tvReult.setText(result);
		}
	}
}
