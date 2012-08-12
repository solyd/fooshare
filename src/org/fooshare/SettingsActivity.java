package org.fooshare;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	boolean mBound = false;
	//SimpleService mService;
	private static final String TAG = "SettingsActivity";

	/*
	private ServiceConnection mConnection = new ServiceConnection() {
	       // Called when the connection with the service is established
	       public void onServiceConnected(ComponentName className, IBinder service) {
	    	   Log.e(TAG, " onServiceConnected is running");
	           // Because we have bound to an explicit
	           // service that is running in our own process, we can
	           // cast its IBinder to a concrete class and directly access it.
	           SimpleBinder binder = (SimpleBinder) service;
	           mService = binder.getService();
	           mBound = true;
	       }

	       // Called when the connection with the service disconnects unexpectedly
	       public void onServiceDisconnected(ComponentName className) {
	           Log.e(TAG, "onServiceDisconnected");
	           mService = null;
	           mBound = false;
	       }
	   };
	   */

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        TextView textview = (TextView)this.findViewById(R.id.settings_activity_label);
        textview.setText("This is the Settings tab");

        textview = (TextView)this.findViewById(R.id.count_label);
        textview.setText("Count hasn't started yet");

    }

    protected void onPause(){
    	super.onPause();
    	Log.d(TAG, "onPause is running");
    	 if (mBound){
  		   //getApplicationContext().unbindService(mConnection);
  		   TextView textView = (TextView)this.findViewById(R.id.count_label);
  		   textView.setText("Is now disconnected");
  		   mBound = false;
    	 }

    }

    /*
   public void bindToService(View btn){
	   Log.d(TAG, "bindToService is running");
	   TextView textView = (TextView)this.findViewById(R.id.count_label);

	   if (mBound == false){
		   Intent intent = new Intent(this, SimpleService.class);
		   boolean res = getApplicationContext().bindService(intent, mConnection, BIND_AUTO_CREATE);
		   Log.d(TAG, "res = "+String.valueOf(res)+" and therefor:");
		   if (res==true)
			   textView.setText("Is now connected");
		   else
			   textView.setText("The connection wasn't made");
	   }else{

		   textView.setText("The service is already connected");
	   }

   }


   public void unbindFromService(View btn){
	   Log.d(TAG, "unbindFromService is running");
	   TextView textView = (TextView)this.findViewById(R.id.count_label);

	   if (mBound){
		   getApplicationContext().unbindService(mConnection);
		   textView.setText("Is now disconnected");
		   mBound = false;
	   }else{
		    textView.setText("The service is not connected");
	   }

	}


   public void incCount(View btn){
	   Log.d(TAG, "incCount is running");
	   TextView textView = (TextView)this.findViewById(R.id.count_label);
	   if (mBound){
		   Log.d(TAG, "increasing");
		   mService.incCount();
		   textView.setText(String.valueOf( mService.getCount()));
	   }else {
		   textView.setText("You are not connected to a service");
	   }
	}

   public void decCount(View btn){
	   Log.d(TAG, "decCount is running");
	   TextView textView = (TextView)this.findViewById(R.id.count_label);
	   if (mBound){
		   Log.d(TAG, "decreasing");
		   mService.decCount();
		   textView.setText(String.valueOf( mService.getCount()));
	   }else {
		   textView.setText("You are not connected to a service");
	   }
	}
	*/
}


