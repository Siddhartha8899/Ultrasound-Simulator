package com.example.ultrasoundsimulator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.MyViewHolder> {

    Context mCtx;
    Vector<videoDetails> vector;
    public ListViewAdapter(Context context, Storage s) {
        mCtx = context;
        vector = new Vector<>();
        vector.addAll(s.vector);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.item_card, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String name = vector.get(position).videoName;
        String Id = vector.get(position).videoID.toString();

        if(name.length() > 35) {
            name =  name.substring(0,35) + "...";
        }
        holder._name.setText(name);
        holder._id.setText(Id);
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((UploadActivity) mCtx).videoClicked(vector.get(position).videoID);
            }
        });

        holder._delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((UploadActivity) mCtx).deleteVideo(vector.get(position).videoID);
            }
        });


    }

    @Override
    public int getItemCount() {
        return vector.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView _name;
        TextView _id;
        Button _delete;
        RelativeLayout relativeLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            _name = itemView.findViewById(R.id.name);
            _id = itemView.findViewById(R.id.ID);
            relativeLayout = itemView.findViewById(R.id.relative);
            _delete = itemView.findViewById(R.id.delete);
        }
    }
}
