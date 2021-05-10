package com.example.ultrasoundsimulator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import java.util.List;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_VIDEO = 1;

    FloatingActionButton upload;
    RecyclerView recyclerView;
    ListViewAdapter listViewAdapter;
    VideoView videoView;
    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        recyclerView =  findViewById(R.id.videos);
        upload = findViewById(R.id.uploadButton);
        back = findViewById(R.id.back);
        videoView = findViewById(R.id.videoView);
        allListeners();

        listViewAdapter = new ListViewAdapter(this, MyApplication.getApplication().storage);
        recyclerView.setAdapter(listViewAdapter);
    }

    private void allListeners() {
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("video/mp4");
        startActivityForResult(intent, PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK)
        {
            ClipData clipData = data.getClipData();
            if(clipData != null) {
                for(int i=0;i<clipData.getItemCount();i++) {
                    ClipData.Item videoItem = clipData.getItemAt(i);
                    Uri uri = videoItem.getUri();
                    MyApplication.getApplication().storage.add(new videoDetails(getFileName(uri), uri.toString()));
                }
            }
            else {
                Uri videoURI = data.getData();
                MyApplication.getApplication().storage.add(new videoDetails(getFileName(videoURI), videoURI.toString()));
            }
            reloadRecyclerView();
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void videoClicked(Integer videoID) {
        Intent intent=new Intent();
        intent.putExtra("ID", videoID);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }


    private void reloadRecyclerView() {
        listViewAdapter.vector.clear();
        listViewAdapter.vector.addAll( MyApplication.getApplication().storage.vector);
        listViewAdapter.notifyDataSetChanged();
    }

    public void deleteVideo(Integer videoID) {
        MyApplication.getApplication().storage.delete(videoID);
        reloadRecyclerView();
    }
}