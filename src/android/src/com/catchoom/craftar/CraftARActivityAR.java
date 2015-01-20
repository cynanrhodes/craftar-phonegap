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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.craftar.CraftARItem;
import com.craftar.CraftARItemAR;
import com.craftar.CraftARActivity;
import com.craftar.CraftARCamera;
import com.craftar.CraftARCameraView;
import com.craftar.CraftARCloudRecognition;
import com.craftar.CraftARCloudRecognitionError;
import com.craftar.CraftARImageHandler;
import com.craftar.CraftARResponseHandler;
import com.craftar.CraftARSDK;
import com.craftar.CraftARTracking;
import com.craftar.CraftARImage;
import com.catchoom.craftar.FakeR;






import org.apache.cordova.*;

public class CraftARActivityAR extends CraftARActivity implements CraftARResponseHandler, CraftARImageHandler, CordovaInterface {
 
	public static final String ARTAG = "CraftARctivityAR";
	// Result code for an activity error response	 	 
	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
	
			
	// The token of the image collection to search
	protected String token;	
	
	// The hint to show at the top of the screen
	protected String hint;	
			
	// The custom connect URL
	protected String connectUrl;
	
	// The custom search URL
	protected String searchUrl;
	
	// The CraftAR cloud recognition service
	CraftARCloudRecognition cloudRecognition;	
	
	// The CraftAR tracking module
	CraftARTracking tracking;

	// Whether we're currently tracking
	boolean isTracking = false;
	
	private CordovaWebView cwv;
	
	private CraftARCamera mcamera; 
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 
		// Extract options from the intent that started the activity
		Intent intent = getIntent();		
		token       = intent.getStringExtra(CraftARIntent.EXTRA_TOKEN);
		hint        = intent.getStringExtra(CraftARIntent.EXTRA_HINT);		
		connectUrl  = intent.getStringExtra(CraftARIntent.EXTRA_CONNECT_URL);
		searchUrl   = intent.getStringExtra(CraftARIntent.EXTRA_SEARCH_URL);
		
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
				
		if (!TextUtils.isEmpty(hint)) {
			int hintId = fakeR.getId("id", "craftar_hint");
			TextView hintView = (TextView) layout.findViewById(hintId);
			hintView.setText(hint);
			hintView.setVisibility(View.VISIBLE);
		}
		
		int cwvId = fakeR.getId("id", "OverlayView");
		cwv = new CordovaWebView(this);
		
		CordovaWebViewClient cwvc; 
		
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
	           cwvc = new CordovaWebViewClient(this, cwv);
	        } else {
	           cwvc = new IceCreamCordovaWebViewClient(this, cwv);
	        }
		
	    cwv.setWebViewClient(cwvc);
        cwv.setWebChromeClient(new CordovaChromeClient(this, cwv));
       
		FrameLayout wvLayout = (FrameLayout) layout.findViewById(cwvId);
		wvLayout.addView(cwv);
	    Config.init(this);
	    	    
	    cwv.loadUrl("file:///android_asset/www/ar_overlay.html");
	    cwv.setBackgroundColor(Color.TRANSPARENT);
        
		setContentView(layout);
		
		 		
		// Initialize the Cloud Image Recognition service
		cloudRecognition = CraftARSDK.getCloudRecognition();
		cloudRecognition.setResponseHandler(this);
		cloudRecognition.setCollectionToken(token);
		
		if (!TextUtils.isEmpty(connectUrl)) {
			cloudRecognition.setConnectUrl(connectUrl);
		}
		
		if (!TextUtils.isEmpty(searchUrl)) {
			cloudRecognition.setSearchUrl(searchUrl);
		}
		
		// Obtain camera
		mcamera = CraftARSDK.getCamera();
		mcamera.setImageHandler(this);
		
		
		// Obtain the Tracking module
		tracking = CraftARSDK.getTracking();
		
		// Start finder mode
		cloudRecognition.startFinding();
				  
		
	}
	

	@Override
	public void connectCompleted() {}	

	/**
	 * 
	 * Image search success handler	 
	 * 
	 * @param items
	 *     The matched items.
	 */
	@Override
	public void searchCompleted(ArrayList<CraftARItem> items) {
		
		// Only add items if we're not already tracking.
		/* 
		 * This check is needed because stopFinding() is asynchronous, 
		 * and thus another call to searchCompleted() may be made after
		 * stopFinding() has been called. All calls to searchCompleted()
		 * are made from the same thread, so no further synchronization
		 * is required. 
		 */
		if (!isTracking) {
			
			boolean haveContent = false;		
			for (CraftARItem item : items) {
				 
		        // Setup the AR experience with content provided in the response
		        if (item.isAR()) {
		        	
		        	// Cast the found item to an AR item
		            CraftARItemAR itemAR = (CraftARItemAR) item;
		 
		            // If the item has contents, add them to the AR experience
		            if (itemAR.getContents().size() > 0) {
		            	try {
		                	tracking.addItem(itemAR);
		            	} catch (Exception e) {
		            		e.printStackTrace();
		            	}
		                haveContent = true;
		            }
		            
		        }
		    }
			
			if (haveContent) {
	        	// Stop finding 
				cwv.loadUrl("javascript:msg(true)"); 
	        	cloudRecognition.stopFinding();        	
	        	// Start tracking
	        	tracking.startTracking();
	        	isTracking = true;
			}
			else{
				cwv.loadUrl("javascript:msg(false)"); 

			}

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
		
		// Ignore "Image lacks distinguishable details" error in finder 
		// mode because this may be caused by covering the camera.
		int errorCode = error.getErrorCode();		
		if (errorCode != CraftARCloudRecognitionError.ErrorCodes.IMAGE_NO_DETAILS) {
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
	

	//Callback received for SINGLE-SHOT only (after takePicture).
	@Override
	public void requestImageReceived(com.craftar.CraftARImage image) {
		cloudRecognition.searchWithImage(token,image);
	}

	@Override
	public void requestImageError(String error) {
		//Take picture failed
		Toast.makeText(null, "There was an error with the picture", Toast.LENGTH_SHORT).show();
		mcamera.restartCameraPreview();
	}

}
