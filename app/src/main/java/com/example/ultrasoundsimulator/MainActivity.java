package com.example.ultrasoundsimulator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    SendReceive sendReceive;
    ServerClass serverClass;
    TextView connectionStatus;
    VideoView videoView;


    private static final String APP_NAME = "Ultrasound Simulator";
    private static final UUID MY_UUID=UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");
    private static final String TAG = "MainActivity";
    HashMap<Integer,Integer> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendReceive = null;
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        connectionStatus = findViewById(R.id.connectionStatus);
        videoView = findViewById(R.id.videoView);
        hashMap = new HashMap<Integer, Integer>();
        serverSocketStart();

        hashMap.put(0,R.raw.aefastlunginteriasliding);
        hashMap.put(1,R.raw.eefastnormalpelvistransverse);
        hashMap.put(2,R.raw.normalpelvislongitudinal);
    }

    private void serverSocketStart() {

        serverClass=new ServerClass();
        serverClass.start();
    }

    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            try {
                serverSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket=null;

            while (socket==null)
            {
                try {
                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(socket!=null)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectionStatus.setText("Connected");
                            connectionStatus.setBackgroundColor(Color.parseColor("#50C878"));
                        }
                    });

                    sendReceive=new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    private class SendReceive extends Thread
    {
        private BluetoothSocket bluetoothSocket;
        private InputStream inputStream;

        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
        }

        public void run()
        {

            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try {
                    bytes=inputStream.read(buffer);
                    String tempMsg=new String(buffer,0,bytes);
                    if(tempMsg.equals( "restart simulator")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectionStatus.setText("Not Connected");
                                connectionStatus.setBackgroundColor(Color.parseColor("#800000"));
                                close();
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runVideo(tempMsg);
                            }
                        });

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runVideo(String tempMsg) {
        String path = "android.resource://" + getPackageName() + "/" + hashMap.get(Integer.parseInt(tempMsg));
        Uri uri = Uri.parse(path);
        videoView.setVideoURI(uri);

        // Create a new MediaController, which handles Play/Pause/Rewind/Fast Forward/Loop/etc
        MediaController mediaController = new MediaController(this);

        // Link the video and the MediaController together
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);


        // Start loading the aefastlunginteriasliding video in the background
        videoView.start();

        // Set up a callback. This callback will run when the aefastlunginteriasliding video has finished loading from the hard drive into RAM
        // It's like opening a video file on your PC. The window shows, but it has a loading icon until the video has fully opened.
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            // When the aefastlunginteriasliding video has been loaded, this function will run
            public void onPrepared(MediaPlayer mp) {

                // Tell the MediaPlayer that we want this video to loop
                mp.setLooping(true);
            }
        });
    }

    private void close() {
        Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
        System.exit(0);
    }

}