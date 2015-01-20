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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.craftar.CraftARActivity;
import com.craftar.CraftARItem;

/**
 * 
 * Cordova plugin class for recognizing images with the CraftAR API
 */
public class CraftARPlugin extends CordovaPlugin {
	
	/**
	 * 
	 * Activity request codes
	 */
	protected static final int CV_REQUEST = 1;
		
	/**
	 * 
	 * The JavaScript context from which the plugin was invoked
	 */
	protected CallbackContext callbackContext;
	
	/**
	 * 
	 * The application context in which we're running
	 */
	protected Context context;
		
	

	@Override
	public boolean execute(String action, JSONArray args, 
			CallbackContext callbackContext) throws JSONException {
				
		// Store our contexts
		this.callbackContext = callbackContext;
		this.context = cordova.getActivity().getApplicationContext();
		
		// Extract options from arguments 
		String  loadUrl  = null;
		
		JSONObject options = args.optJSONObject(0);
		if (options != null) {
			loadUrl  = options.optString("loadUrl", loadUrl);		
		}
		// Perform the requested action		
		if ("startView".equals(action)) {
			startView(loadUrl);
		}
		else {
			return false;			
		}
		
		return true;		
	}
	
	/**
	 * 
	 * Start craftar camera view
	 * 
	 * @param loadUrl
	 *     Overlay url.
	 */
	protected void startView(String loadUrl) {
		
		Intent intent = new Intent(context, CraftARCordovaActivity.class);	
		intent.putExtra(CraftARIntent.EXTRA_CONNECT_URL, loadUrl);
		cordova.startActivityForResult(this, intent, CV_REQUEST);
	}
	
	
	/**
	 * 
	 * Callback on a CraftAR activity exit
	 * 
	 * @param requestCode
	 *     The activity request code.
	 * @param resultCode
	 *     The result code returned by the CraftAR activity.
	 * @param resultIntent
	 *     The data returned by the CraftAR activity.
	 */
	public void onActivityResult(int requestCode, int resultCode, 
			Intent resultIntent) {
		
		// Handle image recognition activity result
		if (requestCode == CV_REQUEST) {
			callbackContext.success();
		}
		else
		{
			callbackContext.error(-1);
		}
		
	}
	
}