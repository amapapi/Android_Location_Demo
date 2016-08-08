/**
 * 
 */
package com.amap.location.demo;


import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

/**
 * 错误码说明
 * @创建时间：2016年5月31日 上午11:22:06 
 * @项目名称： AMapLocationDemo 
 * @author hongming.wang
 * @文件名称：ErrorCodeInfo.java
 * @类型名称：ErrorCodeInfo
 * @since 2.5.0
 */
public class ErrorCode_Activity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_errcode);
		setTitle(R.string.title_errorCode);
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
