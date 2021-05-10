package com.example.ultrasoundsimulator;

import java.util.HashMap;
import java.util.Vector;

public class Storage {

    public HashMap<Integer, videoDetails> hashMap;
    Vector<videoDetails> vector;
    public Integer newVideoID;

    Storage() {
        hashMap = new HashMap<>();
        vector = new Vector<>();
        newVideoID = 0;
    }
    public void add(videoDetails vd) {
        vd.videoID = newVideoID;
        hashMap.put(newVideoID, vd);
        vector.add(vd);
        newVideoID++;
        MyApplication.getApplication().saveData();
    }

    public void delete(Integer videoID) {
        vector.clear();
        hashMap.remove(videoID);
        vector.addAll(hashMap.values());
        MyApplication.getApplication().saveData();
    }
}
