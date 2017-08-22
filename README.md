# Litifer Android SDK Integration

### Requirements

* Min Android SDK 14

### Installing the Library

#### JCenter

The easiest way to get Litifer into your Android project is to use the JCenter Maven repository. Just add the following line to the `dependencies` section of your module's `build.gradle` file:

```
compile 'litifer.com.sdk:1.2'
```

### Intializing the SDK
The Litifer SDK MUST be initialized inside the Application base class `onCreate` method or Main Activity `onCreate` method. If your Android application doesn't already have an Application base class, follow these instructions to create one.
If your Android application doesn't already have an Application base class and want to create one, follow [these](https://developer.android.com/reference/android/app/Application.html) instructions to create one.

Create an instance variable of Litifer here :
```
private Litifer litifer;
```

Add the following snippet to your Application's onCreate or Main Activity's onCreate method:

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


                    litifer.addDevice()
                            .setFCMToken(FirebaseInstanceId.getInstance().getToken());

                    litifer.addCustomer()
                            .setAge(23)
                            .setName("dipu")
                            .setEmail("rit2012015dipu@gmail.com")
                            .setGender("male")
                            .setPhone_no("9818252130");

                    litifer.config()
                            .setApplicationID("592ff8126acd4a048a5deaa4")
                            .setApplicationSecret("946ef7fe-fc99-43a2-87ce-1d526cbc2978")
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

                            stringOfWifiDetails+=" "+message+"\n";
                            loadtext();
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

Add [this]() Location Services file in your project to enable Geofence pings: 




