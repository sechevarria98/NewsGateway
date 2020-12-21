package com.example.gateway;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class StoriesService extends Service {

    private Intent intent;
    private String source;
    private boolean running = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("SOURCE")) {
            source = (String) intent.getSerializableExtra("SOURCE");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(running) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendSource(source);
                }
                sendMessage("Service Thread Stopped");
            }
        }).start();

        return Service.START_NOT_STICKY;
    }

    private void sendSource(String source_id) {
        Intent intent = new Intent();
        intent.setAction(MainActivity.SOURCE_FROM_SERVICE);
        intent.putExtra(MainActivity.SOURCE_DATA, source_id);
        sendBroadcast(intent);
    }

    private void sendMessage(String msg) {
        Intent intent = new Intent();
        intent.setAction(MainActivity.MESSAGE_FROM_SERVICE);
        intent.putExtra(MainActivity.MESSAGE_DATA, msg);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        sendMessage("Service Destroyed");
        running = false;
        super.onDestroy();
    }
}
