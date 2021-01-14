package com.bornintelligence.cellc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class LocationAdapter extends ArrayAdapter<Location> {
    public LocationAdapter(Context context, ArrayList<Location> locationList){
        super(context, 0, locationList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initParentView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.location_drop_down_item, parent, false
            );
        }
        TextView nameView = convertView.findViewById(R.id.location_name);
        Location currentItem = getItem(position);
        if(currentItem != null)
            nameView.setText(currentItem.getName());

        return convertView;
    }


    private View initParentView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.simple_spinner_item, parent, false
            );
        }
        TextView nameView = convertView.findViewById(R.id.location_name);
        Location currentItem = getItem(position);
        if(currentItem != null)
            nameView.setText(currentItem.getName());

        return convertView;
    }
}

