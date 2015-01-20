/* 
 * Copyright 2014 Niels Snoeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.catchoom.craftar;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.craftar.CraftARActivity;
import com.craftar.CraftARCamera;
import com.craftar.CraftARCameraView;
import com.craftar.CraftARCloudRecognition;
import com.craftar.CraftARCloudRecognitionError;
import com.craftar.CraftARItem;
import com.craftar.CraftARImage;
import com.craftar.CraftARImageHandler;
import com.craftar.CraftARResponseHandler;
import com.craftar.CraftARSDK;
import com.catchoom.craftar.FakeR;

public class CraftARActivityIR extends CraftARActivity implements CraftARResponseHandler, CraftARImageHandler , CordovaInterface {
 
	// Result code for an activity error response	 	 
	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
	
	// Error code for image capture error, extending
	// {@link com.catchoom.api.CraftARErrorResponseItem.ErrorCodes}
	public static final int CAPTURE_ERROR = 15; 
	
	
	// The CraftAR cloud recognition service
	CraftARCloudRecognition cloudRecognition;	
	
	// The CraftAR camera
	protected CraftARCamera camera;	
	
	// The token of the image collection to search
	protected String token;	
	
	// The hint to show at the top of the screen
	protected String hint;	
	
	// Whether recognition should be performed in single shot mode
	protected boolean singleShot;
	
	// Whether bounding boxes should be included in the results
	protected boolean bboxes;
	
	// Whether custom data should be included in the results
	protected boolean embedCustom;
	
	// The custom connect URL
	protected String connectUrl;
	
	// The custom search URL
	protected String searchUrl;
	
	// The hint view
	protected TextView hintView;
	
	// The button view
	protected ImageButton buttonView;
	
	// The spinner view
	protected ProgressBar progressView;
	
	private CordovaWebView cwv;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 
		// Extract options from the intent that started the activity
		Intent intent = getIntent();		
		token       = intent.getStringExtra(CraftARIntent.EXTRA_TOKEN);
		hint        = intent.getStringExtra(CraftARIntent.EXTRA_HINT);
		singleShot  = intent.getBooleanExtra(CraftARIntent.EXTRA_SINGLE_SHOT, false);
		bboxes      = intent.getBooleanExtra(CraftARIntent.EXTRA_BBOXES, false);
		embedCustom = intent.getBooleanExtra(CraftARIntent.EXTRA_EMBED_CUSTOM, false);
		connectUrl  = intent.getStringExtra(CraftARIntent.EXTRA_CONNECT_URL);
		searchUrl   = intent.getStringExtra(CraftARIntent.EXTRA_SEARCH_URL);
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
				
		if (!TextUtils.isEmpty(hint)) {
			int hintId = fakeR.getId("id", "craftar_hint");
			hintView = (TextView) layout.findViewById(hintId);
			hintView.setText(hint);
			hintView.setVisibility(View.VISIBLE);
		}
		
		if (singleShot) {
			int buttonId = fakeR.getId("id", "craftar_button");		
			buttonView = (ImageButton) layout.findViewById(buttonId);
			buttonView.setVisibility(View.VISIBLE);
			
			int progressId = fakeR.getId("id", "craftar_progress");
			progressView = (ProgressBar) layout.findViewById(progressId);			
		}
		
		int cwvId = fakeR.getId("id", "OverlayView");
		
	/*	cwv = (CordovaWebView) layout.findViewById(cwvId);
	    Config.init(this);
	    
	    cwv.getSettings().setJavaScriptEnabled(true);
	    cwv.addJavascriptInterface(new CraftARJSInterface(this),"craftar");
	    cwv.loadUrl("file:///android_asset/www/ar_overlay.html");
	        
	    cwv.setBackgroundColor(Color.TRANSPARENT);*/
		
		setContentView(layout);
		
		// Initialize the SDK
		CraftARSDK.init(getApplicationContext(), this);
		 		
		// Initialize the Cloud Image Recognition service
		cloudRecognition = CraftARSDK.getCloudRecognition();
		cloudRecognition.setResponseHandler(this);
		cloudRecognition.setRequestBBoxes(bboxes);
		cloudRecognition.setEmbedCustom(embedCustom);
		
		if (!TextUtils.isEmpty(connectUrl)) {
			cloudRecognition.setConnectUrl(connectUrl);
		}
		
		if (!TextUtils.isEmpty(searchUrl)) {
			cloudRecognition.setSearchUrl(searchUrl);
		}
		
		// Start single shot or finder mode
		if (singleShot) {
		
			// Initialize image handling on takePicture()
			camera = (CraftARCamera) CraftARSDK.getCamera();
	    	camera.setImageHandler(this);
	    
	    	// Connecting isn't strictly necessary at this point, but enables
	    	// the activity to fail fast on connection and token errors. 
	    	cloudRecognition.connect(token);
	    	
		} else {
			
			// Set the collection token
			cloudRecognition.setCollectionToken(token);
			
			// Start finder mode
			cloudRecognition.startFinding();
			
		}
	    
	}

	@Override
	public void connectCompleted() {}

	/**
	 * 
	 * Image capture button click handler. 
	 * 
	 * @param view
	 *     The button view.
	 */
	public void onTakePicture(View view) {
		
		// Capture the image
		camera.takePicture();
		
		// Show the spinner
		if (hintView != null) {
			hintView.setVisibility(View.GONE);
		}
		if (buttonView != null) {
			buttonView.setVisibility(View.GONE);
		}
		if (progressView != null) {
			progressView.setVisibility(View.VISIBLE);
		}
		
	}
	
	/**
	 * 
	 * Image capture success handler
	 * 
	 * @param image
	 *     The received image.
	 */
	@Override
	public void requestImageReceived(CraftARImage image) {
		cloudRecognition.searchWithImage(token, image);		
	}
	
	/**
	 * 
	 * Image capture error handler
	 * 
	 * @param error
	 *     The error message.
	 */
	@Override
	public void requestImageError(String error) {
		finishWithError(CAPTURE_ERROR, error);		

	}

	/**
	 * 
	 * Image search success handler
	 * 
	 * @param items
	 *     The matched items.
	 */
	@Override
	public void searchCompleted(ArrayList<CraftARItem> items) {
		
		// In finder mode this method is called repeatedly, also when nothing 
		// is recognized. So don't finish unless at least one item is found.
		// In single shot mode, allow returning zero results.  
		if (singleShot || items.size() > 0) {
		
			// Set the activity result to the recognized items
			Intent resultIntent = new Intent();
			resultIntent.putParcelableArrayListExtra(CraftARIntent.EXTRA_ITEMS, items);
			setResult(Activity.RESULT_OK, resultIntent);
				
			// Finish the activity
			finish();
			
		}
		
	}
	
	/**
	 * 
	 * Cloud Image Recognition service error handler
	 * 
	 * @param requestCode
	 *     The request code.
	 * @param error
	 *     The error that occurred.
	 */
	@Override
	public void requestFailedResponse(int requestCode, CraftARCloudRecognitionError error) {
		
		/*
		 * Ignore "Image lacks distinguishable details" error in finder 
		 * mode because this may be caused by covering the camera.
		 */		
		int errorCode = error.getErrorCode();		
		if (singleShot || errorCode != CraftARCloudRecognitionError.ErrorCodes.IMAGE_NO_DETAILS) {
			finishWithError(errorCode, error.getErrorMessage());
		}
		
	}
	
	/**
	 * 
	 * Finish the activity with an error
	 * 
	 * @param errorCode
	 *     The error code. One of 
	 *     {@link com.catchoom.api.CraftARCloudRecognitionError.ErrorCodes} or
	 *     {@value #CAPTURE_ERROR}
	 * @param errorMessage
	 *     The error message.
	 */
	protected void finishWithError(int errorCode, String errorMessage) {
		
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
