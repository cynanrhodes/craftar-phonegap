<!-- 
  
  Copyright 2014 Niels Snoeck
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

-->

# com.catchoom.craftar

This plugin provides image recognition and augmented reality capabilities via the 
[Catchoom Cloud Image Recognition Service (CRS)](http://catchoom.com/product/cloud-image-recognition-api).

## Installation

Via plugman: 
    plugman install com.catchoom.craftar

    
## Supported Platforms

- Android
- iOS

This plugin allows you to create Image Recognition and Augmented Reality experiences in your Cordova application.
It offers a full javascript interface to the Catchoom SDK so you can develop your application without adding a line of native code.

To use this plugin you need to download the Catchoom SDK for the platforms you want to support after installing the plugin:
* Android SDK: 
  1. Install the plugin
  2. Download the Catchoom SDK for Android (http://catchoom.com/product/mobile-sdk/#download-mobile-sdk)
  3. Extract the SDK and copy the contents of libs into platforms/android/libs in your project.

* iOS SDK:
  1. Install the plugin
  2. Download the Catchoom SDK for iOS (http://catchoom.com/product/mobile-sdk/#download-mobile-sdk)
  3. open the xCode project for the platform ($> open platforms/ios/YOUR_PROJECT.xcodeproj)
  3. Drag the .framework and .bundle files into the project.

To get started with the plugin we recommend you try the example app provided with it. The example can be found in the 
platform www folder:
  * For iOS:
  ```
      platforms/ios/www/catchoom_example
  ```

  * For Android:
  ```
      platforms/android/assets/www/catchoom_example
  ```
You just need to replace the project www contents with the example folder contents.
