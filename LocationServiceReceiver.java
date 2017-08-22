package litifer.com.litiferdemoapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import litifer.com.sdk.presentation.WifiService;

/**
 * Created by Mehsaan on 02-08-2017.
 */

public class LocationServiceReciever extends BroadcastReceiver{
    private String TAG="geofenceReciever";
    public LocationServiceReciever(){

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.d(TAG,"Received null intent");
            return;
        }

        Log.d(TAG,"Intent Received"+intent.getAction());

        String intentAction = intent.getAction();
        if (intentAction == null) {
            Log.d(TAG, "Received intent without intent action string");
            return;
        }
        final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Log.d(TAG,"no gps");

            return;
        }

        context.startService(new Intent(context,LocationService.class));

    }
}
