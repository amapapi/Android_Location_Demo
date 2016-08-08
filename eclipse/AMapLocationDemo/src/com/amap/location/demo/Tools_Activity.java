package com.amap.location.demo;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.CoordinateConverter.CoordType;
import com.amap.api.location.DPoint;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 高精度定位模式功能演示
 *
 * @创建时间： 2015年11月24日 下午5:22:42 
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: Hight_Accuracy_Activity.java
 * @类型名称: Hight_Accuracy_Activity
 */
public class Tools_Activity extends Activity
		implements OnClickListener {
	private TextView tvConvertReult;
	private TextView tvCheckReult;

	private Button btConvert;
	private Button btCheck;
	//构造一个示例坐标，第一个参数是纬度，第二个参数是经度
	DPoint examplePoint = new DPoint(39.911127, 116.433608);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tools);
		setTitle(R.string.title_tools);
		
		initView();
	}

	// 初始化控件
	private void initView() {

		tvConvertReult = (TextView) findViewById(R.id.tv_convertResult);
		tvCheckReult = (TextView) findViewById(R.id.tv_checkResult);

		btConvert = (Button) findViewById(R.id.bt_covert);
		btCheck = (Button) findViewById(R.id.bt_check);

		btConvert.setOnClickListener(this);
		btCheck.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_covert :
				convert();
				break;
			case R.id.bt_check :
				checkIsAvaliable();
				break;
		}
	}
	
	//坐标转换
	private void convert() {
		try{
			//初始化坐标转换类
			CoordinateConverter converter = new CoordinateConverter(
					getApplicationContext());
			/**
			 * 设置坐标来源,这里使用百度坐标作为示例
			 * 可选的来源包括：
			 * <li>CoordType.BAIDU ： 百度坐标
			 * <li>CoordType.MAPBAR ： 图吧坐标
			 * <li>CoordType.MAPABC ： 图盟坐标
			 * <li>CoordType.SOSOMAP ： 搜搜坐标
			 * <li>CoordType.ALIYUN ： 阿里云坐标
			 * <li>CoordType.GOOGLE ： 谷歌坐标
			 * <li>CoordType.GPS ： GPS坐标
			 */
			converter.from(CoordType.BAIDU);
			//设置需要转换的坐标
			converter.coord(examplePoint);
			//转换成高德坐标
			DPoint destPoint = converter.convert();
			if(null != destPoint){
				tvConvertReult.setText("转换后坐标(经度、纬度):" + destPoint.getLongitude() + "," + destPoint.getLatitude());
			} else {
				Toast.makeText(getApplicationContext(), "坐标转换失败", Toast.LENGTH_SHORT).show();
			}
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), "坐标转换失败", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	//判断坐标是否高德地图可用
	private void checkIsAvaliable(){
		//初始化坐标工具类
		CoordinateConverter converter = new CoordinateConverter(
				getApplicationContext());
		//判断是否高德地图可用的坐标
		boolean result = converter.isAMapDataAvailable(examplePoint.getLatitude(), examplePoint.getLongitude());
		if(result){
			tvCheckReult.setText("该坐标是高德地图可用坐标");
		} else {
			tvCheckReult.setText("该坐标不能用于高德地图");
		}
	}
}
