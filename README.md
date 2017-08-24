# Litifer Android SDK Integration

### Requirements

* Min Android SDK 14

### Installing the Library

#### JCenter

The easiest way to get Litifer into your Android project is to use the JCenter Maven repository.

Add the following line to the `dependencies` section of your project's `build.gradle` file:
```
maven {
    url  "http://dl.bintray.com/litifer17/LitiferSDK"
}
```

Add the following line to the `dependencies` section of your module's `build.gradle` file:

```
compile 'com.litifer.sdk:location-analytics:1.2.3'
```

### Add `uses-feature` tag to app manifest:

```
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
### In `dependencies` section of `app` level `build.gradle` add :
```
compile 'com.google.android.gms:play-services-location:10.0.1'
```

and in last line add :
```
apply plugin: 'com.google.gms.google-services'
```
### Intializing the SDK
The Litifer SDK MUST be initialized inside the Application base class `onCreate` method or Main Activity `onCreate` method. If your Android application doesn't already have an Application base class, follow these instructions to create one.
If your Android application doesn't already have an Application base class and want to create one, follow [these](https://developer.android.com/reference/android/app/Application.html) instructions to create one.

Create an instance variable of Litifer here :
```
private Litifer litifer;
```

Add the following snippet to your Application's onCreate or Main Activity's onCreate method. Kindly replace *YOUR_APPLICATION_ID* and *YOUR_APPLICATION_SECRET* with yours:

```
litifer = Litifer.init(this);

new Thread(){

            @Override
            public void run() {
            super.run();
            try {
                    AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this);
                    litifer.addDevice()
                            .setAdvertisementID(info.getId())
                            .setmAdTrackEnabled(info.isLimitAdTrackingEnabled());


                    litifer.addCustomer()
                            .setAge(23)
                            .setGender("male");

                    litifer.config()
                            .setApplicationID("YOUR_APPLICATION_ID")
                            .setApplicationSecret("YOUR_APPLICATION_SECRET")
                            .enableBackGroundMonitoring();



                    litifer.saveConfig(new Litifer.SaveConfigListener() {
                        @Override
                        public void onSuccess(String message) {


                        }

                        @Override
                        public void onFailure(String errorMessage) {

                        }
                    });


                    startLitiferMonitoring(new Litifer.MonitoringListener() {
                        @Override
                        public void onSuccess(String message) {

                        }

                        @Override
                        public void onFailure() {

                        }
                    });


                startService(new Intent(getApplicationContext(),LocationService.class));


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }catch (NullPointerException e){
                    e.printStackTrace();
            }
                }
            }.start();

```

and add a `startLitiferMonitoring` function in the same Application's or MainActivity's file : 

```
    public void startLitiferMonitoring(Litifer.MonitoringListener monitoringListener) {
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            } else {
                litifer.startMonitoring(monitoringListener);
            }
        } else {
            // Pre-Marshmallow
            litifer.startMonitoring(monitoringListener);
        }
    }
    
 ```
 
Android 6 onwards requires runtime permission for Location.  Please add this function, if not already present :

```
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                litifer.startMonitoring(null);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
```


finally close the litifer instance :

```
litifer.close();
```

Add [LitiferService.java](https://github.com/kishlayk/LitiferAndroidSdk/blob/master/LocationService.java) and [GeofenceTransitionService.java](https://github.com/kishlayk/LitiferAndroidSdk/blob/master/GeofenceTransitionsIntentService.java) file in your project to enable Geofence pings.

Register these services in `AndroidManifest.xml`.

```
        <service
            android:name=".GeofenceTransitionsIntentService"
            android:enabled="true"
            android:exported="true" />
        <service
            android:name=".LocationService"
            android:enabled="true"
            android:exported="true" />
```

Register [LocationReceiver](https://github.com/kishlayk/LitiferAndroidSdk/blob/master/LocationServiceReceiver.java) in `AndroidManifest.xml`

```
        <receiver
            android:name=".LocationServiceReciever">

            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
                <action android:name="android.intent.action.REBOOT" />
                <action android:name="android.intent.action.USER_PRESENT"/>
                <action android:name="android.location.PROVIDERS_CHANGED" />
                <category android:name="android.intent.category.DEFAULT" />

            </intent-filter>
        </receiver>
```




