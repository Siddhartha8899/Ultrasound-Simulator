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
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /* Bluetooth Functionality varibale declaration */
    BluetoothAdapter bluetoothAdapter;
    SendReceive sendReceive;
    ServerClass serverClass;
    TextView connectionStatus;
    private static final String APP_NAME = "Ultrasound Simulator";
    private static final UUID MY_UUID=UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");
    private static final String TAG = "MainActivity";

    /* Video Functionality variable declaration */
    HashMap<Integer,Integer> hashMap;
    VideoView videoView;
    Button uploadButton;
    MediaController mediaController;
    Integer stopPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendReceive = null;
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        connectionStatus = findViewById(R.id.connectionStatus);
        videoView = findViewById(R.id.videoView);
        uploadButton = findViewById(R.id.upload);
        hashMap = new HashMap<Integer, Integer>();

        /* Bluetooth Functionality */
        /* Turns the bluetooth on, if off. */
        enableBluetooth();

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUploadActivity();
            }
        });
    }

    private void enableBluetooth() {
        if(!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 2);
        } else {
            serverSocketStart();
        }
    }

    //Start an intent to the upload Activity
    public void openUploadActivity() {
        Intent intent = new Intent(this,UploadActivity.class);
        startActivityForResult(intent,1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == RESULT_CANCELED) {
            return;
        }
        if(requestCode == 2) {
            serverSocketStart();
        } else {
            Bundle extras = data.getExtras();
            Integer ID = extras.getInt("ID");
            runVideo(ID.toString());
        }
    }

    private void runVideo(String tempMsg) {
        if(MyApplication.getApplication().storage.hashMap.containsKey(Integer.parseInt(tempMsg))) {
            Uri uri = Uri.parse(MyApplication.getApplication().storage.hashMap.get(Integer.parseInt(tempMsg)).videoUri);
            videoView.setVideoURI(uri);

            mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);

            videoView.start();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });
        } else {
            Toast.makeText(this, "Video Does not exist", Toast.LENGTH_LONG).show();
        }
    }

    private void serverSocketStart() {

        serverClass=new ServerClass();
        serverClass.start();
    }

    private void close() {
        Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
        System.exit(0);
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

                    } else if(tempMsg.equals("pause")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoView.pause();
                                stopPosition = videoView.getCurrentPosition();
                            }
                        });
                    } else if(tempMsg.equals("resume")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoView.start();
                                videoView.seekTo(stopPosition);
                                stopPosition = 0;
                            }
                        });

                    }
                    else {
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
}