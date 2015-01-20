package com.catchoom.craftar;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.craftar.CraftARItem;
import com.craftar.CraftARItemAR;
import com.craftar.CraftARCamera;
import com.craftar.CraftARCloudRecognition;
import com.craftar.CraftARCloudRecognitionError;
import com.craftar.CraftARImage;
import com.craftar.CraftARImageHandler;
import com.craftar.CraftARResponseHandler;
import com.craftar.CraftARSDK;
import com.craftar.CraftARTracking;

public class CraftARJSInterface implements CraftARResponseHandler, CraftARImageHandler{
	private String ARTAG = "CraftARJSInterface";
	private CraftARCordovaActivity activity;
	protected CraftARCloudRecognition cloudRecognition;	
	protected CraftARCamera mcamera;
	protected CraftARTracking mtracking;
	protected String token;
	public boolean automaticAR = true; 
	boolean isTracking = false;


		
	public CraftARJSInterface(CraftARCordovaActivity act){
		activity = act;
		CraftARSDK.init(act.getApplicationContext(), act);
		cloudRecognition = CraftARSDK.getCloudRecognition();
		cloudRecognition.setResponseHandler(this);
		mcamera = CraftARSDK.getCamera();
		mcamera.setImageHandler(this);
		mtracking = CraftARSDK.getTracking();
	}
	
	
	private void searchCompletedIR(ArrayList<CraftARItem> items) {
		
		JSONArray results = new JSONArray();				
		for (CraftARItem item : items) {
			JSONObject jitem = item.getJson();
			jitem.remove("tracking");
			results.put(jitem);
		}
		activity.cwv.loadUrl("javascript:searchCompletedIR("+results+")");
	}
	
	private void searchCompletedIA(ArrayList<CraftARItem> items) {
		if(!automaticAR && !isTracking) return;
			
			boolean haveContent = false;		
			for (CraftARItem item : items) {
				 
		        if (item.isAR()) {
		        	
		            CraftARItemAR itemAR = (CraftARItemAR) item;
		 
		            if (itemAR.getContents().size() > 0) {
		            	try {
		                	mtracking.addItem(itemAR);
		            	} catch (Exception e) {
		            		e.printStackTrace();
		            	}
		                haveContent = true;
		            }
		            
		        }
		    }
			if (haveContent) {
				activity.cwv.loadUrl("javascript:ARFound()"); 
	        	cloudRecognition.stopFinding();  
	        	startTracking();
			}
	}

	
	@JavascriptInterface
	public void startFinding(){
		Log.d(ARTAG, "StartFinding");
		cloudRecognition.startFinding();
	}
	@JavascriptInterface
	public void stopFinding(){
		Log.d(ARTAG, "StopFinding");
		cloudRecognition.stopFinding();
	}
	
	@JavascriptInterface
	public boolean startTracking(){
		Log.d(ARTAG, "StartTracking");
    	isTracking = true;
		return mtracking.startTracking();
	}
	@JavascriptInterface
	public boolean stopTracking(){
		Log.d(ARTAG, "StopTracking");
    	isTracking = false;
		return mtracking.stopTracking();
	}
	
	
	@JavascriptInterface
	public void restartCamera(){
		Log.d(ARTAG, "restartCamera");
		mcamera.restartCameraPreview();
	}
	
	@JavascriptInterface
	public boolean takePictureAndSearch(){
		Log.d(ARTAG, "takePictureAndSearch");
		return mcamera.takePicture();
	}
	
	@JavascriptInterface
	public void triggerFocus(){
		if(mcamera!=null){
			Log.d(ARTAG, "triggerFocus");
			mcamera.triggerFocus();
		}else{
			Log.d(ARTAG, "triggerFocus failed (camera not ready)");
		}
	}
	
	@JavascriptInterface
	public void setToken(String tkn){
		Log.d(ARTAG, "setToken");
		token = tkn;
		cloudRecognition.setCollectionToken(token);
	}
	
	@JavascriptInterface
	public void setAutomaticAR(boolean auto){
		Log.d(ARTAG, "automaticAR");
		automaticAR = auto;
	}
	
	@JavascriptInterface
	public void setConnectUrl(String connectUrl)
	{
	   Log.d(ARTAG, "setConnectUrl");
	   cloudRecognition.setConnectUrl(connectUrl);
	}

    @JavascriptInterface
    public void setSearchUrl(String searchUrl)
    {
       Log.d(ARTAG, "setSearchUrl");
	   cloudRecognition.setSearchUrl(searchUrl);
    }
	
	@JavascriptInterface
	public void stopAR(){
		Log.d(ARTAG, "stopAR");
		stopTracking();
		mtracking.removeAllItems();
		mcamera.restartCameraPreview();
	}
	
	@JavascriptInterface
	public void close(){
		Log.d(ARTAG, "close");
		activity.finish();
	}

	
	//Interfaces
	
	@Override
	public void requestImageError(String error) {
		activity.cwv.loadUrl("javascript:requestImageError("+error+")"); 
		if(automaticAR) 
			mcamera.restartCameraPreview();
	}


	@Override
	public void requestImageReceived(CraftARImage img) {
		cloudRecognition.searchWithImage(token,img);
		if(automaticAR) 
			mcamera.restartCameraPreview();
	}


	@Override
	public void connectCompleted() {
		//activity.cwv.loadUrl("javascript:connectCompleted()"); 
	}


	@Override
	public void requestFailedResponse(int requestCode,
			CraftARCloudRecognitionError error) {
		// Ignore "Image lacks distinguishable details" error in finder 
		// mode because this may be caused by covering the camera.
		int errorCode = error.getErrorCode();	
		if (errorCode != CraftARCloudRecognitionError.ErrorCodes.IMAGE_NO_DETAILS) {
			activity.cwv.loadUrl("javascript:requestFailedResponse("+errorCode+",\""+error.getErrorMessage()+"\")");
		}
	}


	@Override
	public void searchCompleted(ArrayList<CraftARItem> items) {
		ArrayList<CraftARItem> itemsir = new ArrayList<CraftARItem>();
		ArrayList<CraftARItem> itemsar = new ArrayList<CraftARItem>();
		for (CraftARItem item : items) {
			if(item.isAR())
				itemsar.add(item);
			else
				itemsir.add(item);
		}
		searchCompletedIR(itemsir);
		searchCompletedIA(itemsar);
	}
	
	
}
