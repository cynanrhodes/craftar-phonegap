package com.catchoom.craftar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.apache.cordova.engine.SystemWebChromeClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.craftar.CraftARActivity;
import com.craftar.CraftARCamera;
import com.craftar.CraftARCameraView;
import com.craftar.CraftARCloudRecognition;
import com.craftar.CraftARSDK;
import com.craftar.CraftARTracking;

public class CraftARCordovaActivity extends CraftARActivity implements CordovaInterface {
	public static final String ARTAG = "CraftARCordovaActivity";
	// Result code for an activity error response	 	 
	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;

	public SystemWebView cwv;

	private CraftARJSInterface craftARInterface; 

	public String loadUrl;


	@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Intent intent = getIntent();		
			loadUrl  = intent.getStringExtra(CraftARIntent.EXTRA_CONNECT_URL);

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

	@Override
		public void onPostCreate() {

			// Initialize the UI		
			FakeR fakeR = new FakeR(this);

			int layoutId = fakeR.getId("layout", "craftar_camera"); 
			View layout = (View) getLayoutInflater().inflate(layoutId, null);

			int previewId = fakeR.getId("id", "craftar_preview");
			CraftARCameraView cameraView = (CraftARCameraView) layout.findViewById(previewId);		
			super.setCameraView(cameraView);

			int cwvId = fakeR.getId("id", "OverlayView");
			cwv = new SystemWebView(this);

			WebViewClient cwvc; 
			SystemWebViewEngine engine;

			engine = new SystemWebViewEngine(cwv);
			cwvc = new SystemWebViewClient(engine);

			cwv.setWebViewClient(cwvc);
			cwv.setWebChromeClient(new SystemWebChromeClient(engine));

			FrameLayout wvLayout = (FrameLayout) layout.findViewById(cwvId);
			wvLayout.addView(cwv);
			Config.init(this);

			craftARInterface = new CraftARJSInterface(this);
			cwv.addJavascriptInterface(craftARInterface,"craftarjs");

			if (!TextUtils.isEmpty(loadUrl)) {
				cwv.loadUrl("file:///android_asset/www/" + loadUrl);
			}


			cwv.setBackgroundColor(Color.TRANSPARENT);

			setContentView(layout);
		}


	public void finishWithError(int errorCode, String errorMessage) {

		// Set the activity result to an error
		Intent resultIntent  = new Intent();
		resultIntent.putExtra(CraftARIntent.EXTRA_ERROR_CODE, errorCode);		
		resultIntent.putExtra(CraftARIntent.EXTRA_ERROR_MESSAGE, errorMessage);
		setResult(RESULT_ERROR, resultIntent);

		// Finish the activity
		finish();

	}

	@Override
		public void startActivityForResult(CordovaPlugin command, Intent intent,
				int requestCode) {		
		}

	@Override
		public void setActivityResultCallback(CordovaPlugin plugin) {

		}

	@Override
		public Activity getActivity() {
			return this;
		}

	@Override
		public Object onMessage(String id, Object data) {
			return null;
		}

	@Override
		public ExecutorService getThreadPool() {
			return Executors.newCachedThreadPool();
		}

}
