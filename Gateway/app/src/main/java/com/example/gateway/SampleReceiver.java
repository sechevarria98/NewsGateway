package com.example.gateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class SampleReceiver extends BroadcastReceiver {
    private MainActivity mainActivity;
    private SourceLoaderRunnable sourceLoaderRunnable;
    private static final String TAG = "SampleReceiver";

    public SampleReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action == null)
            return;

        switch (action) {
            case MainActivity.SOURCE_FROM_SERVICE:
                String id = null;
                if(intent.hasExtra(MainActivity.SOURCE_DATA))
                    id = (String) intent.getSerializableExtra(MainActivity.SOURCE_DATA);
                sourceLoaderRunnable = new SourceLoaderRunnable(this, id);
                new Thread(sourceLoaderRunnable).start();
                break;
            case MainActivity.MESSAGE_FROM_SERVICE:
                String msg = null;
                if (intent.hasExtra(MainActivity.MESSAGE_DATA))
                    msg = (String) intent.getSerializableExtra(MainActivity.MESSAGE_DATA);

                break;
            default:
                Log.d(TAG, "onReceive: Unknown Broadcast Receiver");
        }
    }

    public void receiveList(final ArrayList<Source> stories) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setUpStories(stories);
            }
        });
    }
}
