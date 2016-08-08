package com.amap.location.demo;

import com.amap.api.location.AMapLocationClient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * H5辅助定位模式功能演示
 *
 * @创建时间： 2015年11月24日 下午4:24:07 
 * @项目名称： AMapLocationDemo2.x
 * 
 * @author hongming.wang
 * @文件名称: Battery_Saving_Activity.java
 * @类型名称: Battery_Saving_Activity
 */
public class Assistant_Location_Activity extends CheckPermissionsActivity implements OnClickListener {
	private Button btAssistant;
	private Button btLocation;

	private AMapLocationClient locationClient = null;

	private WebView webView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_assistant_location);
		setTitle(R.string.title_assistantLocation);

		btAssistant = (Button) findViewById(R.id.bt_assistant);
		btLocation = (Button) findViewById(R.id.bt_location);

		locationClient = new AMapLocationClient(getApplicationContext());
		btAssistant.setOnClickListener(this);
		btLocation.setOnClickListener(this);

		initWebView();
	}

	private void initWebView(){
		webView = (WebView) findViewById(R.id.webView);
		
		WebSettings webSettings = webView.getSettings();
		// 允许webview执行javaScript脚本
		webSettings.setJavaScriptEnabled(true);
		// 设置是否允许定位，这里为了使用H5辅助定位，设置为false。
	    //设置为true不一定会进行H5辅助定位，设置为true时只有H5定位失败后才会进行辅助定位
		webSettings.setGeolocationEnabled(false);
		
		
		webView.setWebViewClient(new WebViewClient() {
			
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}

			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {
			// 处理javascript中的alert
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
				return true;
			};

			// 处理javascript中的confirm
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result) {
				return true;
			};

			// 处理定位权限请求
			@Override
			public void onGeolocationPermissionsShowPrompt(String origin,
					Callback callback) {
				callback.invoke(origin, true, false); 
				super.onGeolocationPermissionsShowPrompt(origin, callback);
			}
			@Override
			// 设置网页加载的进度条
			public void onProgressChanged(WebView view, int newProgress) {
				Assistant_Location_Activity.this.getWindow().setFeatureInt(
						Window.FEATURE_PROGRESS, newProgress * 100);
				super.onProgressChanged(view, newProgress);
			}

			// 设置应用程序的标题title
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != locationClient) {
			locationClient.stopAssistantLocation();
			locationClient.onDestroy();
		}
		if(null != webView){
			webView.destroy();
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_assistant) {
			if (btAssistant.getText().equals(getResources().getString(R.string.startAssistantLocation))) {
				btAssistant.setText(getResources().getString(R.string.stopAssistantLocation));
				locationClient.startAssistantLocation();
				btLocation.setEnabled(true);
			} else {
				btAssistant.setText(getResources().getString(R.string.startAssistantLocation));
				locationClient.stopAssistantLocation();
				btLocation.setEnabled(false);
				webView.setVisibility(View.GONE);
			}
		} else if (v.getId() == R.id.bt_location) {
			webView.setVisibility(View.VISIBLE);
			//浏览器定位，加载预置的H5定位页面
			webView.loadUrl(Utils.URL_H5LOCATION);
		}
	}
}
