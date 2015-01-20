# craftar-phonegap
Craftar phonegap plugin repository

 This plugin allows you to create Image Recognition and Augmented Reality experiences in your Cordova application. It offers a full javascript interface to the CraftAR SDK so you can develop your application without adding a line of native code. For more information about the CraftAR service visit http://catchoom.com/product/craftar/augmented-reality-and-image-recognition/

To use this plugin you need to download the CraftAR Augmented Reality SDK for the platforms you want to support.
* Android SDK:
 1. Install the plugin
 2. Download the CraftAR Augmented Reality SDK for Android (http://catchoom.com/product/mobile-sdk/#download-mobile-sdk)
 3. Extract the SDK and copy the contents of libs into platforms/android/libs in your project.
* iOS SDK:
 1. Install the plugin
 2. Download the CraftAR Augmented Reality SDK for iOS (http://catchoom.com/product/mobile-sdk/#download-mobile-sdk)
 3. open the xCode project for the platform ($> open platforms/ios/YOUR_PROJECT.xcodeproj)
 4. Drag the CraftARSDK.framework and Pods.framework into the "Link with libraries" section of the project's main target
 5. Drag the CraftARSDK.bundle into the project.

In order to get started with the plugin, we recommend you try the example app provided with it. The example can be found in the
www folder corresponfing to each platform:
 * For iOS: platforms/ios/www/craftar_example
 * For Android platforms/android/assets/www/craftar_example

You just need to replace the contents of the project www folder with those from that example folder.
