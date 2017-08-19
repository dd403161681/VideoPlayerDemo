package io.vec.demo.mediacodec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartAppAtBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Intent myIntent = new Intent(context, DecodeActivity.class);
//		Intent myIntent = new Intent(context, StartControllerService.class);
		Intent myIntent = new Intent(context, VideoPlayer.class);
		myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(myIntent);
//		context.startService(myIntent);

	}

}
