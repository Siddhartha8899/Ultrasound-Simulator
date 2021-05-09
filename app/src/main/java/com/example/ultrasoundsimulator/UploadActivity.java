package com.example.ultrasoundsimulator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_VIDEO = 100;

    Button upload;
    ArrayList<String> list;
    ArrayAdapter adapter;
    ListView listView;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        listView =  findViewById(R.id.listView);
        upload = findViewById(R.id.uploadButton);
        list = new ArrayList<String>();
        videoView = findViewById(R.id.videoView);
        Field[] fields = R.raw.class.getFields();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UploadActivity.this,"Clicked" , Toast.LENGTH_SHORT).show();
                openGallery();
            }
        });


        for (int i = 0; i < fields.length ; i++){
            list.add(fields[i].getName());
        }

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                int resId = getResources().getIdentifier(list.get(i), "raw", getPackageName());
                Intent intent=new Intent();
                intent.putExtra("resId", resId);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });



    }
    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery,PICK_VIDEO);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO)
        {
            Uri videoUri = data.getData();
            Intent intent = new Intent();
            intent.putExtra("videoUri", videoUri.toString());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}