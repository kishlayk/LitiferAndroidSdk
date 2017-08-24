package litifer.com.litiferdemoapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
//import com.litifer.sdk.notifications.Notify;

import java.util.ArrayList;
import java.util.List;

import litifer.com.sdk.data.model.EventTypeEntity;
import litifer.com.sdk.domain.executor.impl.JobExecutor;
import litifer.com.sdk.presentation.presenters.GeofenceRequestPresenter;
import litifer.com.sdk.presentation.subscriber.thread.UIThread;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this

    private GeofenceRequestPresenter geofenceRequestPresenter;
    private String TAG = "Geofence Errors";
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        geofenceRequestPresenter = new GeofenceRequestPresenter(new JobExecutor(),UIThread.getThread(),this);
        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG," GeofenceTransitionsIntentService triggered!!");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {

//           String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
//            Log.e(TAG, errorMessage);
            Log.e(TAG,geofencingEvent.getErrorCode() + " Error in GeofenceTransitionsIntentService.java");

            //I have commented here(dipu)
            //this.stopService(new Intent(this, LocationService.class));
//                System.out.println("Error in GeofenceTransitionsIntentService.java");
             return;
        }else {

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                System.out.println("triggeringGeofences" + triggeringGeofences.get(0).getRequestId());

                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        this,
                        geofenceTransition,
                        triggeringGeofences
                );
                for(Geofence i :triggeringGeofences){
                    if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER){
                        geofenceRequestPresenter.Event(i.getRequestId(),EventTypeEntity.GEOFENCE_ENTER.toString());
                    }
                    else{
                        geofenceRequestPresenter.Event(i.getRequestId(), EventTypeEntity.GEOFENCE_EXIT.toString());
                    }
                }
                //FirebaseInstanceId firebaseInstanceId = FirebaseInstanceId.getInstance();
                //dipu I commented here because Notify class is not founding
               // Notify.init().sendNotification(triggeringGeofences.get(0).getRequestId(),null,this);
                    Log.i("details", geofenceTransitionDetails);
            }
            else {
               //Log the error.
                 Log.e(TAG,"invalid transition");
            }
        }
    }

    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + " : " + triggeringGeofencesIdsString;
    }


        @Override
        public void onDestroy() {
        //     Log.d(Tag, "onDestroy()");
        System.out.println("GeofenceTransaction onDestroy is called.");

        super.onDestroy();
    }
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "transition - enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "transition - exit";
            default:
                return "unknown transition";
        }
    }
}
