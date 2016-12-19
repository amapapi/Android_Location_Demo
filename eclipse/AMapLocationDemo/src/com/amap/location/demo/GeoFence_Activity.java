package com.amap.location.demo;

import com.amap.location.demo.view.FeatureView;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 地理围栏功能演示
 *
 * @创建时间： 2015年11月24日 下午5:49:52
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: GeoFence_Activity.java
 * @类型名称: GeoFence_Activity
 */
public class GeoFence_Activity extends ListActivity {
	private static class DemoDetails {
		private final int titleId;
		private final int descriptionId;
		private final Class<? extends android.app.Activity> activityClass;
		public DemoDetails(int titleId, int descriptionId,
				Class<? extends android.app.Activity> activityClass) {
			super();
			this.titleId = titleId;
			this.descriptionId = descriptionId;
			this.activityClass = activityClass;
		}
	}

	private static class CustomArrayAdapter extends ArrayAdapter<DemoDetails> {
		public CustomArrayAdapter(Context context, DemoDetails[] demos) {
			super(context, R.layout.feature, R.id.title, demos);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FeatureView featureView;
			if (convertView instanceof FeatureView) {
				featureView = (FeatureView) convertView;
			} else {
				featureView = new FeatureView(getContext());
			}
			DemoDetails demo = getItem(position);
			featureView.setTitleId(demo.titleId);
			featureView.setDescriptionId(demo.descriptionId);
			return featureView;
		}
	}

	private static final DemoDetails[] demos = {
			new DemoDetails(R.string.roundGeoFence,
					R.string.roundGeoFence_dec, GeoFence_Round_Activity.class),
			new DemoDetails(R.string.polygonGeoFence,
					R.string.polygonGeoFence_dec, GeoFence_Polygon_Activity.class),
			new DemoDetails(R.string.keywordGeoFence,
					R.string.keywordGeoFence_dec, GeoFence_Keyword_Activity.class),
			new DemoDetails(R.string.nearbyGeoFence,
					R.string.nearbyGeoFence_dec, GeoFence_Nearby_Activity.class),
			new DemoDetails(R.string.districtGeoFence,
					R.string.districtGeoFence_dec, GeoFence_District_Activity.class),
			new DemoDetails(R.string.multipleGeoFence, R.string.multipleGeoFence_dec,
					GeoFence_Multiple_Activity.class),
			new DemoDetails(R.string.oldGeoFence, R.string.oldGeoFence_dec,
					GeoFence_Old_Activity.class),
			};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geofence);
		setTitle(R.string.title_main);
		ListAdapter adapter = new CustomArrayAdapter(
				this.getApplicationContext(), demos);
		setListAdapter(adapter);
//		permChecker = new PermissionsChecker(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		System.exit(0);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		DemoDetails demo = (DemoDetails) getListAdapter().getItem(position);
		startActivity(
				new Intent(this.getApplicationContext(), demo.activityClass));
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
