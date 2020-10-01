package com.example.stmappvod;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<Object> {

    ArrayList<Object> objects;
    Context context;
    int resource;

    public CustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Object> objects) {
        super(context, resource, objects);

        this.objects = objects;
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_layout, null, true);
        }
        Object object = getItem(position);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.thumb);
        Picasso.get().load(object.getThumbnail()).into(imageView);

        TextView channelName = (TextView) convertView.findViewById(R.id.channeltitle);
        channelName.setText(object.getChannelName());

        TextView channelID = (TextView) convertView.findViewById(R.id.channelid);
        channelID.setText(object.getChannelID());

        return convertView;
    }
}
