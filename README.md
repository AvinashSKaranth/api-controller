# api-controller
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-api--controller-green.svg?style=true)](https://android-arsenal.com/details/1/3401)

Run GET,POST and DOWNLOAD API on Android. Library takes care of Cookie Management and http caching.

GRADLE LINK
```xml
compile 'in.nashapp.apicontroller:apicontroller:1.0.8'
```

ANDROID MANIFEST
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

```java
ApiController APIc = new ApiController(this);//Calling Library

//GET : parmeters need to be only string
String response  = APIc.GetRequest(String URL,HashMap<String,String> parameters);

//POST : parmeters can be a string or a file, see example below
String response  = APIc.PostRequest(String URL,HashMap<String,String> parameters);

//DOWNLOAD BACKGROUND
String destination  = APIc.DownloadFile(String URL,String AbsoluteDestination);
//If you dont add the extension in AbsoluteDestination then extension will added based in MimeType of the downloaded file
//return the downloaded path, creates the folder if does not exist.

//DOWNLOAD WITH NOTIFICATION
String destination  = APIc.DownloadFileNotify(String URL,String AbsoluteDestination);
//Shows progress in notification bar and then closes it on finishing
//return the downloaded path, creates the folder if does not exist.
```




**GET**
```java
ApiController APIc = new ApiController(this);
new Thread( new Runnable() {
  @Override
  public void run() {
      //Network Operation
      HashMap<String,String> params = new HashMap<String, String>();
      params.put("get_request1", "1");
      params.put("get_request2", "2");
      params.put("get_request3", "3");
      final String result  = APIc.GetRequest("http://nashapp.in/request.php",params);
      new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
              //UI Operation using network data
              alertDialog("Result", result);
          }
      });
  }
}).start();
```

**POST**

You can post files and variables together.
When posting files give the value as 
```java
"file://"+file.getAbsolutePath()
```
You cannot POST multiple files with the same key as described in http://php.net/manual/en/features.file-upload.multiple.php
```java
ApiController APIc = new ApiController(this);
new Thread( new Runnable() {
  @Override
  public void run() {
      //Network Operation
      HashMap<String,String> params = new HashMap<String, String>();
      params.put("post_message1", "1");
      params.put("post_message2", "2");
      params.put("post_message3", "3");
      params.put("post_file", "file:///storage/emulated/0/Pictures/Screenshots/Screenshot_2016-04-01-22-13-25.png");
      params.put("post_file1", "file:///storage/emulated/0/Pictures/Screenshots/Screenshot_2016-04-01-22-13-25.png");
      params.put("post_message4", "4");
      params.put("post_message5", "5");
      params.put("post_message6", "6");
      final String result  = APIc.PostRequest("http://nashapp.in/request.php",params);
      new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
              //UI Operation using network data
              alertDialog("Result", result);
          }
      });
  }
}).start();
```

**DOWNLOAD BACKGROUND**
```java
ApiController APIc = new ApiController(this);
 new Thread( new Runnable() {
  @Override
  public void run() {
      final String result  = APIc.DownloadFile("http://nashapp.in/socialsignin.png", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/socialsignin.png");
      new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
             //UI Operation using network data
              alertDialog("Result", result);
          }
      });
  }
}).start();
```

**DOWNLOAD NOTIFY**
```java
ApiController APIc = new ApiController(this);
new Thread( new Runnable() {
  @Override
  public void run() {
      final String result  = APIc.DownloadFileNotify("http://nashapp.in/test.txt", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/test.txt");
      new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
              //UI Operation using network data
              alertDialog("Result", result);
          }
      });
  }
}).start();
```

You can use http://nashapp.in/request.php to verify validity of the request variables.

**ALERT DIALOG** if you need to verify values before parsing
```java
public void alertDialog(String title,String Message){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(Message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
```
**License**

Copyright 2016 Avinash S Karanth

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
